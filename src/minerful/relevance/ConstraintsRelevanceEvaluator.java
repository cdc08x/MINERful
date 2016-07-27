package minerful.relevance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.utils.MessagePrinter;

/**
 * Return the relevant constraints out of the log. Please beware, that the discrimination is solely based on the support.
 * @author Claudio Di Ciccio
 */
public class ConstraintsRelevanceEvaluator {
	public static final String CSV_PRE_HEADER = "Template;Constraint;Support;VacuousSupport;";
	public static final double DEFAULT_SATISFACTION_THRESHOLD = 0.5;
	public static final double NO_SATISFACTION_THRESHOLD = 0;
		
	public static MessagePrinter logger = MessagePrinter.getInstance(ConstraintsRelevanceEvaluator.class);
			
	private RelevanceAutomatonMultiWalker[] texasRangers;
	private Map<Constraint, RelevanceEvaluationOnLog> evaluationsOnLog;
	private TaskCharEncoderDecoder taChaEncoDeco;
	private TaskCharArchive tChArchive;
	private VacuityAwareWildcardAutomaton[] vacuAwAutos;
	private List<Constraint> nuConstraints;
	private LogParser logParser;
	private double satisfactionThreshold;

	/**
	 * Constructor of this class.
	 * @param logParser A parser of the log
	 * @param parametricConstraints Constraints for which constrained tasks will be used as place-holders for the event classes in the log
	 */
	public ConstraintsRelevanceEvaluator(LogParser logParser, Constraint[] parametricConstraints) {
		this(logParser, parametricConstraints, DEFAULT_SATISFACTION_THRESHOLD);
	}

	/**
	 * Constructor of this class.
	 * @param logParser A parser of the log
	 * @param parametricConstraints Constraints for which constrained tasks will be used as place-holders for the event classes in the log
	 * @param satisfactionThreshold Threshold below which the constraint will not be considered as satisfied
	 */
	public ConstraintsRelevanceEvaluator(LogParser logParser, Constraint[] parametricConstraints, double satisfactionThreshold) {
		MessagePrinter.configureLogging(DebugLevel.all);

        long from = 0, to = 0;
        
        logger.debug("Preparing the data structures...");

        from = System.currentTimeMillis();

        this.satisfactionThreshold = satisfactionThreshold;

        this.logParser = logParser;
        this.taChaEncoDeco = logParser.getEventEncoderDecoder();
		this.tChArchive = logParser.getTaskCharArchive();

		this.vacuAwAutos = new VacuityAwareWildcardAutomaton[parametricConstraints.length];

		int numOfGeneratedAutomata = setupAutomata(parametricConstraints);

		this.texasRangers = new RelevanceAutomatonMultiWalker[parametricConstraints.length];
		this.evaluationsOnLog = new TreeMap<Constraint, RelevanceEvaluationOnLog>();//(numOfGeneratedConstraints, (float)1.0);
		this.nuConstraints = new ArrayList<Constraint>();

		logger.debug("Preparing the automata walkers...");

		int numOfGeneratedConstraints = setupAutomataWalkers(parametricConstraints);

		to = System.currentTimeMillis();

		logger.debug("Data structures prepared. Time in msec: " + (to - from));
	}

	private int setupAutomataWalkers(Constraint[] parametricConstraints) {
		int
			numOfGeneratedConstraints = 0,
			constraintIndex = 0,
			paramsIndex = 0;
		List<TaskChar> constraintParams = null;
		TaskChar[] nuConstraintParams = null;
		Constraint nuConstraint = null;

		for (Constraint paraCon : parametricConstraints) {
			constraintParams = new ArrayList<TaskChar>();
			for(TaskCharSet tChSet : parametricConstraints[constraintIndex].getParameters()) {
				// FIXME Relevance check to be added for branched Declare
				constraintParams.addAll(tChSet.getTaskCharsList());
			}

			texasRangers[constraintIndex] =
					new RelevanceAutomatonMultiWalker(vacuAwAutos[constraintIndex], taChaEncoDeco.getTranslationMap());

			for (RelevanceAutomatonWalker walker : texasRangers[constraintIndex].getWalkers()) {
				paramsIndex = 0;
				nuConstraintParams = new TaskChar[constraintParams.size()];
				for (TaskChar param : constraintParams){
					nuConstraintParams[paramsIndex++] = tChArchive.getTaskChar(walker.decode(param.identifier));
				}
				nuConstraint = paraCon.copy(nuConstraintParams);
				numOfGeneratedConstraints++;
				this.nuConstraints.add(nuConstraint);
				this.evaluationsOnLog.put(nuConstraint, new RelevanceEvaluationOnLog());
			}
			constraintIndex++;
		}

		logger.debug(numOfGeneratedConstraints + " automata walkers set up.");
		
		return numOfGeneratedConstraints;
	}

	private int setupAutomata(Constraint[] parametricConstraints) {
		int
			automatonIndex = 0,
			numOfGeneratedConstraints = 0;
		for (Constraint paraCon : parametricConstraints) {
			if (paraCon.isBranched()) {
				// FIXME Relevance check to be added for branched Declare
				throw new UnsupportedOperationException("Branched Declare not yet considered");
			}
			vacuAwAutos[automatonIndex++] =
					paraCon.getCheckAutomaton();
//					new VacuityAwareWildcardAutomaton(
//							paraCon.getRegularExpression(),
//					TaskCharEncoderDecoder.getTranslationMap(paraCon.getInvolvedTaskChars()));
		}
		
		logger.debug(automatonIndex + " automata set up.");
		return numOfGeneratedConstraints;
	}

	public void runOnTheLog() {
		logger.debug("Running on the log");
		
		long from = 0, to = 0;
		
		Iterator<LogTraceParser> logPar = logParser.traceIterator();
		int nuConstraintIndex = 0;
		LogTraceParser loTraParse = null;
		Constraint constraintUnderAnalysis = null;
		ArrayList<RelevanceAutomatonWalker> walkersToRemove = new ArrayList<RelevanceAutomatonWalker>();
		RelevanceEvaluationOnLog eval = null;

		int
			traceCount = 0,
			barCount = 0;
		MessagePrinter.printOut("Parsing log: ");
		
		from = System.currentTimeMillis();
		
		while (logPar.hasNext()) {
			nuConstraintIndex = 0;
			loTraParse = logPar.next();
			for (RelevanceAutomatonMultiWalker texasRanger : texasRangers) {
				loTraParse.init();
				texasRanger.run(loTraParse);
				for (RelevanceAutomatonWalker walker : texasRanger.getWalkers()) {
					constraintUnderAnalysis = this.nuConstraints.get(nuConstraintIndex);
					
//					if (this.isThresholdOnSatisfaction() && !walker.getTraceEvaluation().equals(TraceEvaluation.SATISFACTION)) {
//						this.nuConstraints.remove(nuConstraintIndex);
//						walkersToRemove.add(walker);
//						this.evaluationsOnLog.remove(constraintUnderAnalysis);
//					} else {
					eval = this.evaluationsOnLog.get(constraintUnderAnalysis);
					eval.increment(walker.getTraceEvaluation());
//					}

//					System.out.println("MERDACCIA " +
//							this.nuConstraints.get(nuConstraintIndex) +
//							" ha dato " +
//							walker.getTraceEvaluation());

					if (this.isThresholdOnSatisfaction() && isSupportInsufficient(eval)) {
						this.nuConstraints.remove(nuConstraintIndex);
						walkersToRemove.add(walker);
						this.evaluationsOnLog.remove(constraintUnderAnalysis);
					} else {
						nuConstraintIndex++;
					}
				}
				
				for (RelevanceAutomatonWalker walkerToRemove : walkersToRemove) {
					texasRanger.remove(walkerToRemove);
				}
				walkersToRemove = new ArrayList<RelevanceAutomatonWalker>();
			}
			if (barCount > logParser.length() / 80) {
				barCount = 0;
				MessagePrinter.printOut("|");
			}
			traceCount++;
			barCount++;
		}
		
		updateNuConstraintsSupport();

		to = System.currentTimeMillis();

		MessagePrinter.printlnOut("\nDone.");
		logger.debug(traceCount + " traces evaluated on the log.");
		logger.debug("Evaluation done. Time in msec: " + (to - from));
	}

	private void updateNuConstraintsSupport() {
		for (Constraint con : this.nuConstraints) {
			con.setSupport(this.computeSupport(this.evaluationsOnLog.get(con)));
		}
	}

	private double computeVacuousSupport(RelevanceEvaluationOnLog eval) {
		return (1.0 * eval.numberOfVacuouslySatisfyingOrSatisfyingTraces() / this.logParser.length());
	}
	
	private double computeSupport(RelevanceEvaluationOnLog eval) {
		return (1.0 * eval.numberOfRelevantlySatisfyingTraces() / this.logParser.length());
	}
	
	private boolean isSupportInsufficient(RelevanceEvaluationOnLog eval) {
		return eval.numberOfViolatingOrVacuouslySatisfyingTraces() > (this.logParser.length() - this.satisfactionThreshold * this.logParser.length());
	}

	private boolean isThresholdOnSatisfaction() {
		return this.satisfactionThreshold > NO_SATISFACTION_THRESHOLD;
	}

	public ConstraintsRelevanceEvaluator(LogParser logParser, Constraint parametricConstraint) {
		this(logParser, new Constraint[]{parametricConstraint});
	}

	public String printEvaluationsCSV() {
		StringBuilder sBuil = new StringBuilder();
		
		sBuil.append(ConstraintsRelevanceEvaluator.CSV_PRE_HEADER);
		sBuil.append(RelevanceEvaluationOnLog.CSV_HEADER);
		sBuil.append("\n");
		
		TreeSet<Constraint> constraints = new TreeSet<Constraint>(evaluationsOnLog.keySet());
		
		for (Constraint con : constraints) {
			sBuil.append(con.getName());
			sBuil.append(';');
			sBuil.append(con);
			sBuil.append(';');
			sBuil.append(con.getSupport());
			sBuil.append(';');
			sBuil.append(this.computeVacuousSupport(this.evaluationsOnLog.get(con)));
			sBuil.append(';');
			sBuil.append(this.evaluationsOnLog.get(con).printCSV());
			sBuil.append('\n');
		}
		
		return sBuil.toString();
	}
	
	public List<Constraint> getNuConstraints() {
		return nuConstraints;
	}
}