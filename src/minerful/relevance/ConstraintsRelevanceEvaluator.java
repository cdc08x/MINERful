package minerful.relevance;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.AbstractMinerFulStarter;
import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.utils.MessagePrinter;

import org.apache.log4j.Logger;

public class ConstraintsRelevanceEvaluator {
	public static final double DEFAULT_SATISFACTION_THRESHOLD = 0.5;
	public static final double NO_SATISFACTION_THRESHOLD = 0;
	
	private RelevanceAutomatonMultiWalker[] texasRangers;
	private Map<Constraint, RelevanceEvaluationOnLog> evaluationsOnLog;
	private TaskCharEncoderDecoder taChaEncoDeco;
	private TaskCharArchive tChArchive;
	private VacuityAwareWildcardAutomaton[] vacuAwAutos;
	private List<Constraint> nuConstraints;
	private LogParser logParser;
	private double satisfactionThreshold;
	
	protected static Logger logger;

	public ConstraintsRelevanceEvaluator(LogParser logParser, Constraint[] parametricConstraints) {
		this(logParser, parametricConstraints, DEFAULT_SATISFACTION_THRESHOLD);
	}

	public ConstraintsRelevanceEvaluator(LogParser logParser, Constraint[] parametricConstraints, double satisfactionThreshold) {
		AbstractMinerFulStarter.configureLogging(DebugLevel.all);
        if (logger == null)
        	logger = Logger.getLogger(ConstraintsRelevanceEvaluator.class.getCanonicalName());

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
			con.support = this.computeSupport(this.evaluationsOnLog.get(con));
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
		
		sBuil.append("Template;Constraint;Support;VacuousSupport;");
		sBuil.append(RelevanceEvaluationOnLog.CSV_HEADER);
		sBuil.append(";\n");
		
		TreeSet<Constraint> constraints = new TreeSet<Constraint>(evaluationsOnLog.keySet());
		
		for (Constraint con : constraints) {
			sBuil.append(con.getName());
			sBuil.append(';');
			sBuil.append(con);
			sBuil.append(';');
			sBuil.append(con.support);
			sBuil.append(';');
			sBuil.append(this.computeVacuousSupport(this.evaluationsOnLog.get(con)));
			sBuil.append(';');
			sBuil.append(this.evaluationsOnLog.get(con).printCSV());
			sBuil.append(';');
			sBuil.append('\n');
		}
		
		return sBuil.toString();
	}
	
	public List<Constraint> getNuConstraints() {
		return nuConstraints;
	}

	public static void main(String[] args) throws Exception {
		TaskChar a = new TaskChar('A');
		TaskChar b = new TaskChar('B');
		TaskChar c = new TaskChar('C');
		TaskChar x = new TaskChar('X');
		TaskChar y = new TaskChar('Y');
		Constraint[] parametricConstraints =
			new Constraint[] {
//				new Response(a,b),
//				new SequenceResponse21(a,b,x),
//				new SequenceResponse22(a,b,x,y),
//				new SequenceResponse32(a,b,c,x,y),
//				new RespondedExistence(a,b),
//				new AlternateResponse(a,b),
//				new Precedence(a,b),
				new Participation(a),
				new AlternatePrecedence(a,b),
				new CoExistence(a,b),
				new ChainPrecedence(a,b),
				new NotChainSuccession(a, b),
				new ChainResponse(a, b)
		};
//
//		
//		for (Constraint paraCon : parametricConstraints) {
//			System.out.println(paraCon);
//			System.out.println(paraCon.getRegularExpression());
//			System.out.println(paraCon.getCheckAutomaton().toDot());
//		}

//		System.out.println("MERDACCIA " + parametricConstraints[1].getRegularExpression());
//		System.out.println("MERDACCIA " + parametricConstraints[1].getCheckAutomaton().toDot());
//		System.out.println("MERDACCIA " + parametricConstraints[2].getRegularExpression());
//		System.out.println("MERDACCIA " + parametricConstraints[2].getCheckAutomaton().toDot());
//		System.exit(0);
		
		LogParser loPar = null;
		try {
			loPar = new XesLogParser(new File(args[0]), ClassificationType.LOG_SPECIFIED);
		} catch (Exception e) {
			MessagePrinter.printlnOut(args[0] + " is not an XES file");
			loPar = new StringLogParser(new File(args[0]), ClassificationType.NAME);
		}
		
		ConstraintsRelevanceEvaluator evalon = null;
		
		if (args.length > 1) {
			evalon = new ConstraintsRelevanceEvaluator(loPar, parametricConstraints, Double.valueOf(args[1]));
		} else {
			evalon = new ConstraintsRelevanceEvaluator(loPar, parametricConstraints);
		}
		evalon.runOnTheLog();
		
//		ConstraintsRelevanceEvaluator evalon = new ConstraintsRelevanceEvaluator(loPar, new Constraint[]{con});
		
		System.out.println(evalon.printEvaluationsCSV());
		
		if (args.length > 2) {
			logger.debug("Storing fully-supported default-Declare constraints as a Declare map on " + args[2]);
			
			Collection<Constraint> nuStandardConstraints = new ArrayList<Constraint>();
			Double supportThreshold = Double.valueOf(args[1]);

			for (Constraint con : evalon.getNuConstraints()) {
				if (con.getFamily() != null && con.support >= supportThreshold) {
					nuStandardConstraints.add(con);
				}
			}
			
			ConstraintsBag coBag = new ConstraintsBag(loPar.getTaskCharArchive().getTaskChars(), nuStandardConstraints);
			ProcessModel model = new ProcessModel(loPar.getTaskCharArchive(), coBag);
			new DeclareMapEncoderDecoder(model).marshal(args[2]);
			
			logger.debug("Done.");
		}
	}
}