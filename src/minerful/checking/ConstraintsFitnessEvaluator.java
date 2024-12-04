package minerful.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.checking.relevance.dao.ConstraintFitnessEvaluation;
import minerful.checking.relevance.dao.ConstraintsFitnessEvaluationsMap;
import minerful.checking.relevance.dao.TraceEvaluation;
import minerful.checking.relevance.walkers.RelevanceAutomatonMultiWalker;
import minerful.checking.relevance.walkers.RelevanceAutomatonWalker;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.index.comparator.allinone.TemplateAndParametersBasedComparator;
import minerful.index.comparator.allinone.TemplateBasedComparator;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.utils.MessagePrinter;

/**
 * Return the relevant constraints out of the log.
 * @author Claudio Di Ciccio
 */
public class ConstraintsFitnessEvaluator {
	private static MessagePrinter logger = MessagePrinter.getInstance(ConstraintsFitnessEvaluator.class);

	public static final Double DEFAULT_FITNESS_THRESHOLD = (Double)0.5;
	public static final Double NO_FITNESS_THRESHOLD = (Double)0.0;

	protected RelevanceAutomatonMultiWalker[] texasMultiRangers;
	protected TaskCharEncoderDecoder taChaEncoDeco;
	protected TaskCharArchive tChArchive;
	protected Map<Constraint, VacuityAwareWildcardAutomaton> vacuAwAutos;
	protected List<Constraint> checkedConstraints;

	/**
	 * Constructor of this class. Notice that the
	 * {@link TaskChar TaskChar}
	 * elements in the given process specification are re-encoded according to the encoding of the event log.
	 * Notice that it does so as a side effect on the original constraints passed in input and on the
	 * {@link TaskChar TaskChar} elements themselves.
	 * @param taChaEncoDeco An encoder/decoder for tasks of constraints
	 * @param constraints Constraints to be evaluated
	 */
	public ConstraintsFitnessEvaluator(TaskCharEncoderDecoder taChaEncoDeco, Constraint... constraints) {
		this.init(taChaEncoDeco, constraints);
	}
	
	/**
	 * Constructor of this class.
	 * @param constraints Constraints to be evaluated
	 */
	public ConstraintsFitnessEvaluator(Constraint... constraints) {
		this.init(new TaskCharEncoderDecoder(), constraints);
	}
	
	protected void init(TaskCharEncoderDecoder taChaEncoDeco, Constraint... constraints) {
		Arrays.sort(constraints, new TemplateAndParametersBasedComparator());
		this.checkedConstraints = new ArrayList<Constraint>(Arrays.asList(constraints));
		this.taChaEncoDeco = taChaEncoDeco;
		this.taChaEncoDeco.mergeWithConstraintsAndUpdateTheirParameters(constraints);

		// Extract templates from constraints. A template is taken from every constraint by invoking getSymbolic() on it
		Collection<Constraint> templates = identifyTemplates(this.checkedConstraints);
		
		this.initStructures(taChaEncoDeco, null, templates);
		
		this.setupAutomataWalkers(templates, constraints);
	}

	/**
	 * Constructor of this class.
	 * @param taChaEncoDeco An encoder/decoder for tasks of constraints.
	 * @param tCharArchive An archive of all tasks to be checked against the given templates.
	 * @param parametricConstraints Constraints for which permutations of tasks from <code>tCharArchive</code> will be used as actual parameters for the templates in <code>parametricConstraints</code>.
	 */
	public ConstraintsFitnessEvaluator(TaskCharEncoderDecoder taChaEncoDeco, TaskCharArchive tCharArchive, Collection<Constraint> parametricConstraints) {
		MessagePrinter.configureLogging(DebugLevel.all);
		
		this.checkedConstraints = new ArrayList<Constraint>();
		
		Collection<Constraint> templates = identifyTemplates(parametricConstraints);

        long from = 0, to = 0;
        
        logger.debug("Preparing the data structures...");

        from = System.currentTimeMillis();

        this.initStructures(taChaEncoDeco, tCharArchive, templates);
		
		int numOfGeneratedConstraints = setupAutomataWalkersByTaskPermutations(templates);

		to = System.currentTimeMillis();

		logger.debug(
				String.format("Automata walkers prepared (for %d constraints). Time in msec: %d.",
						numOfGeneratedConstraints, (to - from)));
	}

	protected Collection<Constraint> identifyTemplates(Collection<Constraint> constraints) {
		// In case we have, e.g., Response(a,b) and Response(c,d), they are instances of the same template, so we do not want duplicates
		Collection<Constraint> templates = null;
		templates = new TreeSet<Constraint>(new TemplateBasedComparator());
		for (Constraint constraint : constraints) {
			templates.add(constraint.getSymbolic());
		}
		return templates;
	}

	protected void initStructures(TaskCharEncoderDecoder taChaEncoDeco, TaskCharArchive tCharArchive, Collection<Constraint> templates) {
        this.taChaEncoDeco = taChaEncoDeco;
		this.tChArchive = tCharArchive;

		this.vacuAwAutos = new TreeMap<Constraint,VacuityAwareWildcardAutomaton>(new TemplateBasedComparator());

		int numOfGeneratedAutomata = this.setupCheckAutomata(templates);
		logger.debug(String.format("Automata prepared (%d).)", numOfGeneratedAutomata));
		
		this.texasMultiRangers = new RelevanceAutomatonMultiWalker[templates.size()];
	}

	protected void setupAutomataWalkers(Collection<Constraint> templates, Constraint[] constraints) {
		/* For every constraint, this data structure considers the list of actual parameters in the form of characters.
		 * We have thus a list (one for each constraint)
		 * of lists (one for each parameter of that constraint) 
		 * of collections (the parameter's characters; can be more than one, due to branching): 
		 */
		Map<Constraint, List<List<Collection<Character>>>> actualParametersPerTemplate =
				new TreeMap<Constraint, List<List<Collection<Character>>>>(new TemplateBasedComparator());
		List<Collection<Character>> charParameters = null; // A tuple of parameters
		for (Constraint constraint : constraints) {
			logger.debug("Checking constraint " + constraint.toString());
			charParameters = new ArrayList<Collection<Character>>(constraint.getParameters().size());
			if (!actualParametersPerTemplate.containsKey(constraint)) {
				actualParametersPerTemplate.put(constraint, new ArrayList<List<Collection<Character>>>());
			}
			for (TaskCharSet param : constraint.getParameters()) {
				charParameters.add(param.getListOfIdentifiers());
			}
			actualParametersPerTemplate.get(constraint).add(charParameters);

			logger.debug("parametersPerTemplate.get(constraint): " + actualParametersPerTemplate.get(constraint));
		}
		int templateIndex = 0;
		int templateParamsNum = 0;
		
		for (Constraint template : templates) {
			templateParamsNum = template.getParameters().size();
			Character[] formalParameters = new Character[templateParamsNum];
			int i = 0;
			for (TaskCharSet parameter : template.getParameters()) {
				// Notice that we assume formal parameters to be individual characters here, so iterator().next() will only get the first in the list
				formalParameters[i++] = parameter.getListOfIdentifiers().iterator().next();
			}
			// Instantiate a RelevanceAutomatonMultiWalker for every template
			texasMultiRangers[templateIndex++] =
					new RelevanceAutomatonMultiWalker(
							template.type, // The template type will become part of the name of the RelevanceAutomatonMultiWalker
							vacuAwAutos.get(template),
							// The translation map associates every Character to a specific AbstractTaskClass of the event log
							taChaEncoDeco.getTranslationMap(),
							// Get all the actual parameters that belong to the instantiation of that template
							actualParametersPerTemplate.get(template),
							// The formal parameters of the template
							formalParameters);
		}
	}

	protected int setupAutomataWalkersByTaskPermutations(Collection<Constraint> templates) {
		int
			numOfGeneratedConstraints = 0,
			templateIndex = 0,
			paramsIndex = 0;
		List<TaskChar> constraintParams = null;
		TaskChar[] nuConstraintParams = null;
		Constraint nuConstraint = null;

		for (Constraint template : templates) {
			constraintParams = new ArrayList<TaskChar>();
			for(TaskCharSet tChSet : template.getParameters()) {
				// FIXME Relevance check to be added for branched Declare
				constraintParams.addAll(tChSet.getTaskCharsList());
			}

			texasMultiRangers[templateIndex] =
					new RelevanceAutomatonMultiWalker(
							template.type,vacuAwAutos.get(template), taChaEncoDeco.getTranslationMap());

			for (RelevanceAutomatonWalker walker : texasMultiRangers[templateIndex].getWalkers()) {
				paramsIndex = 0;
				nuConstraintParams = new TaskChar[constraintParams.size()];
				for (TaskChar param : constraintParams){
					nuConstraintParams[paramsIndex++] = tChArchive.getTaskChar(taChaEncoDeco.decode(param.identifier));
				}
				nuConstraint = template.copy(nuConstraintParams);
				numOfGeneratedConstraints++;
				this.checkedConstraints.add(nuConstraint);
			}
			templateIndex++;
		}

		logger.debug(numOfGeneratedConstraints + " automata walkers set up.");
		
		return numOfGeneratedConstraints;
	}

	protected int setupCheckAutomata(Collection<Constraint> constraints) {
		int
			automatonIndex = 0;
		for (Constraint con : constraints) {
			// The symbolic parameters can be just always the same, say A and B!
			// FIXME To be put where the automaton was first created, not here!!
			vacuAwAutos.put(con, con.getSymbolic().getCheckAutomaton());
			automatonIndex++;
		}
		
		return automatonIndex;
	}
	
	public ConstraintsFitnessEvaluationsMap runOnLog(LogParser loPar) {
		return runOnLog(loPar, null);
	}

	/**
	 * Evaluates relevance of constraints on the event log passed to the constructor.
	 * @param logParser Parser of the event log to replay
	 */
	public ConstraintsFitnessEvaluationsMap runOnLog(LogParser logParser, Double fitnessThreshold) {
		logger.debug("Running on the log");
		
		long from = 0, to = 0;
		
		Iterator<LogTraceParser> logParIter = logParser.traceIterator();
		int constraintIndex = 0;
		LogTraceParser loTraParse = null;
		Constraint constraintUnderAnalysis = null;
		ArrayList<RelevanceAutomatonWalker> walkersToBeRemoved = new ArrayList<RelevanceAutomatonWalker>();
		ConstraintFitnessEvaluation eval = null;
		
		ConstraintsFitnessEvaluationsMap logEvalsMap = 
				new ConstraintsFitnessEvaluationsMap(checkedConstraints);

		int
			traceCount = 0,
			barCount = 0,
			traceNum = 0;
		boolean
			traceIsFitting = true;
		MessagePrinter.printOut("Parsing log: ");
		
		from = System.currentTimeMillis();
		
		// For every trace
		while (logParIter.hasNext()) {
			constraintIndex = 0;
			traceNum++;
			loTraParse = logParIter.next();
			// We assume the trace fits, by default
			traceIsFitting = true;
			// For parametric automata associated to constraint templates
			for (RelevanceAutomatonMultiWalker texasMultiRanger : texasMultiRangers) {
				loTraParse.init();
				// Run the cursors on the parametric automata
				texasMultiRanger.run(loTraParse);
				// For every run
				for (RelevanceAutomatonWalker walker : texasMultiRanger.getWalkers()) {
					// Get the corresponding checked constraint
					constraintUnderAnalysis = this.checkedConstraints.get(constraintIndex);
					
					// Retrieve the results of the verification of the trace by replay 
					eval = logEvalsMap.increment(constraintUnderAnalysis, walker.getTraceEvaluation());
					// If the trace violates the corresponding constraint
					if (walker.getTraceEvaluation().equals(TraceEvaluation.VIOLATION)) {
						logger.trace("Trace " + loTraParse.printStringTrace() + " (num " + traceNum + ", " + loTraParse.getName() + ") violates " + constraintUnderAnalysis);
						traceIsFitting = traceIsFitting & false;
					}

					// This condition is activated only when fitness is used for mining -- to save memory by removing those constraints that for sure will not make it to have a sufficient fitness at this stage already
					if (fitnessThreshold != null && isFitnessInsufficient(fitnessThreshold, eval, logParser)) {
						this.checkedConstraints.remove(constraintIndex);
						walkersToBeRemoved.add(walker);
						logEvalsMap.remove(constraintUnderAnalysis);
					} else {
						constraintIndex++;
					}
				}
				
				// This loop is run only when fitness is used for mining -- to save memory by removing those constraints that for sure will not make it to have a sufficient fitness at this stage already
				for (RelevanceAutomatonWalker walkerToRemove : walkersToBeRemoved) {
					texasMultiRanger.remove(walkerToRemove);
				}
				walkersToBeRemoved = new ArrayList<RelevanceAutomatonWalker>();
			}
			barCount = displayAdvancementBars(logParser.length(), barCount);
			traceCount++;
			barCount++;
			if (traceIsFitting) {
				logEvalsMap.incrementFittingTracesCount();
			} else {
				logEvalsMap.incrementNonFittingTracesCount();
			}
		}
		
		this.updateConstraintsFitness(logEvalsMap, logParser);

		to = System.currentTimeMillis();

		MessagePrinter.printlnOut("\nDone.");
		logger.debug(traceCount + " traces evaluated on the log.");
		logger.debug("Evaluation done. Time in msec: " + (to - from));
		
		return logEvalsMap;
	}

	private static int displayAdvancementBars(int logParserLength, int barCount) {
		if (barCount > logParserLength / 80) {
			barCount = 0;
			MessagePrinter.printOut("|");
		}
		return barCount;
	}
	
	public ConstraintsFitnessEvaluationsMap runOnTrace(LogTraceParser loTraParser) {
		ConstraintsFitnessEvaluationsMap logEvalsMap =  new ConstraintsFitnessEvaluationsMap(checkedConstraints);
		Constraint constraintUnderAnalysis = null;
		int constraintIndex = 0;
		logger.debug(String.format("Checking %s (encoded as %s)...",
				loTraParser.printStringTrace(),
				loTraParser.encodeTrace()));
		for (RelevanceAutomatonMultiWalker texasMultiRanger : texasMultiRangers) {
			loTraParser.init();
			texasMultiRanger.run(loTraParser);
			for (RelevanceAutomatonWalker walker : texasMultiRanger.getWalkers()) {
				constraintUnderAnalysis = this.checkedConstraints.get(constraintIndex++);
				
				logEvalsMap.increment(constraintUnderAnalysis, walker.getTraceEvaluation());
			}
		}
		updateConstraintsFitness(logEvalsMap, loTraParser);
		return logEvalsMap;
	}

	private void updateConstraintsFitness(ConstraintsFitnessEvaluationsMap logEvalsMap, LogTraceParser loTraParser) {
		for (Constraint con : this.checkedConstraints) {
			con.getTraceBasedMeasures().setFitness(computeFitness(logEvalsMap.evaluationsOnLog.get(con), loTraParser));
		}
	}

	public void updateConstraintsFitness(ConstraintsFitnessEvaluationsMap logEvalsMap, LogParser logParser) {
		for (Constraint con : this.checkedConstraints) {
			con.getTraceBasedMeasures().setFitness(computeFitness(logEvalsMap.evaluationsOnLog.get(con), logParser));
		}
	}
	
	public List<Constraint> getCheckedConstraints() {
		return checkedConstraints;
	}

	public static double computeVacuousFitness(ConstraintFitnessEvaluation eval, LogParser logParser) {
		return (1.0 * eval.numberOfNonViolatingTraces() / logParser.length());
	}
	
	public static boolean isFitnessInsufficient(Double fitnessThreshold, ConstraintFitnessEvaluation eval, LogParser logParser) {
		return
			eval.numberOfViolatingTraces > (logParser.length() - fitnessThreshold * logParser.length());
	}

	public static double computeFitness(ConstraintFitnessEvaluation eval, LogParser logParser) {
		return (1.0 * eval.numberOfNonViolatingTraces() / logParser.length());
	}

	public static double computeFitness(ConstraintFitnessEvaluation eval, LogTraceParser loTraParser) {
		return (1.0 * eval.numberOfNonViolatingTraces());
	}
}