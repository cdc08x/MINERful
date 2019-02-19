package trashbin.minerful;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import minerful.checking.ConstraintsFitnessEvaluator;
import minerful.checking.ProcessSpecificationFitnessEvaluator;
import minerful.checking.relevance.dao.ConstraintsFitnessEvaluationsMap;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapReaderWriter;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.utils.MessagePrinter;

public class MinerFulVacuityChecker2 {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulVacuityChecker2.class);
			
	/**
	 * Task place-holders to be used as parameters for the constraint templates to check.
	 */
    public static TaskChar
		a = new TaskChar('A'),
		b = new TaskChar('B'),
		c = new TaskChar('C'),
		x = new TaskChar('X'),
		y = new TaskChar('Y');

	/**
	 * Constraint templates to be checked.
	 */
	 // Toggle the comment to add/remove the template from the set of checked ones.
	 public static Constraint[] parametricConstraints =
		new Constraint[]{
//			new SequenceResponse21(a,b,x),
//			new SequenceResponse22(a,b,x,y),
//			new SequenceResponse32(a,b,c,x,y),
			new Participation(a),	// a.k.a. Existence(1, a)
			new AtMostOne(a),	// a.k.a. Absence(2, a)
			new Init(a),
			new End(a),
			new RespondedExistence(a,b),
			new RespondedExistence(x,y),
			new Response(a, b),
			new AlternateResponse(a,b),
			new ChainResponse(a,b),
			new Precedence(a,b),
			new AlternatePrecedence(a,b),
			new ChainPrecedence(a,b),
			new CoExistence(a,b),
			new Succession(a,b),
			new AlternateSuccession(a, b),
			new ChainSuccession(a, b),
			new NotChainSuccession(a, b),
			new NotSuccession(a, b),
			new NotCoExistence(a, b),
    };

	public static void main(String[] args) throws Exception {
		System.err.println(
				"#### WARNING"
				+ "\n" +
				"This class is not yet part of the MINERful framework. It is meant to be the proof-of-concept software for the paper entitled "
				+ "\"On the Relevance of a Business Constraint to an Event Log\", authored by C. Di Ciccio, F.M. Maggi, M. Montali, and J. Mendling (DOI: https://doi.org/10.1016/j.is.2018.01.011). "
				+ "Please use it for testing purposes only."
				+ "\n\n" +
		
				"#### USAGE"
				+ "\n" +
				"Usage: java " + MinerFulVacuityChecker2.class.getCanonicalName() + " <XES-log-file-path> [threshold] [Declare-map-output-file-path]."
				+ "\n" +
				"Param:    <XES-log-file-path>: the path to a XES event log file (mandatory)"
				+ "\n" +
				"Param:    [threshold]: the ratio of traces in which the constraints have to be non-vacuously satisfied, from 0.0 to 1.0 (default: " + ConstraintsFitnessEvaluator.DEFAULT_FITNESS_THRESHOLD + ") (optional)"
				+ "\n" +
				"Param:    [Declare-map-output-file-path]: the path of the file in which the returned constraints are stored as a Declare Map XML file (by default, no Declare Map XML file is saved) (optional)"
				+ "\n\n" +

				"#### OUTPUT"
				+ "\n" +
				"To customise the constraint templates to be checked, please change the code of this class (" + MinerFulVacuityChecker2.class.getCanonicalName() + ") in the specified point and recompile."
				+ "\n" +
				"The printed output is a CSV-encoding of constraints that are non-vacuously satisfied in the given log. The output can be also saved as a Declare Map XML file by specifying the third optional command parameter (for standard Declare constraints only) -- see above: [Declare-map-output-file-path]."
				+ "\n\n" +
				
				"Press any key to continue..."
		);
		
		System.in.read();

		MessagePrinter.configureLogging(DebugLevel.all);

		LogParser loPar = null;
		try {
			loPar = new XesLogParser(new File(args[0]), ClassificationType.LOG_SPECIFIED);
		} catch (Exception e) {
			MessagePrinter.printlnOut(args[0] + " is not an XES file");
			loPar = new StringLogParser(new File(args[0]), ClassificationType.NAME);
		}

		TaskCharFactory tChFactory = new TaskCharFactory();
		TaskChar
			accAssgnd = tChFactory.makeTaskChar("Accepted+Assigned"),
			accInProg = tChFactory.makeTaskChar("Accepted+In Progress"),
			accptWait = tChFactory.makeTaskChar("Accepted+Wait"),
			complClos = tChFactory.makeTaskChar("Completed+Closed"),
			qAwaAssgn = tChFactory.makeTaskChar("Queued+Awaiting Assignment"),
			
			nabellOff = tChFactory.makeTaskChar("W_Nabellen offertes"),
			oocreated = tChFactory.makeTaskChar("O_CREATED");
		
		Constraint[] toBeChecked = new Constraint[] {
				new Response(
						tChFactory.makeTaskChar("Accepted+In Progress"),
						tChFactory.makeTaskChar("Completed+Closed")),
				new Response(
						tChFactory.makeTaskChar("Queued+Awaiting Assignment"),
						tChFactory.makeTaskChar("Completed+Closed")),
				new CoExistence(
						tChFactory.makeTaskChar("W_Nabellen offertes"),
						tChFactory.makeTaskChar("O_CREATED")),
				new AlternatePrecedence(
						tChFactory.makeTaskChar("Accepted+In Progress"),
						tChFactory.makeTaskChar("Completed+Closed")),
				new NotChainSuccession(
						tChFactory.makeTaskChar("Completed+Closed"),
						tChFactory.makeTaskChar("Accepted+Assigned")),
				new NotChainSuccession(
						tChFactory.makeTaskChar("Completed+Closed"),
						tChFactory.makeTaskChar("Accepted+Wait")),
				new RespondedExistence(
						tChFactory.makeTaskChar("Accepted+Assigned"), 
						tChFactory.makeTaskChar("Accepted+In Progress")),
				new RespondedExistence(
						tChFactory.makeTaskChar("Accepted+Wait"), 
						tChFactory.makeTaskChar("Accepted+In Progress")),
				
		};
		Constraint[] toBeCheckock = new Constraint[] {
				new Response(
						accInProg,
						complClos),
				new Response(
						qAwaAssgn,
						complClos),
				new CoExistence(
						nabellOff,
						oocreated),
				new AlternatePrecedence(
						accInProg,
						complClos),
				new NotChainSuccession(
						complClos,
						accAssgnd),
				new NotChainSuccession(
						complClos,
						accptWait),
				new RespondedExistence(
						accAssgnd, 
						accInProg),
				new RespondedExistence(
						accptWait, 
						accInProg),
		};
		
		ConstraintsBag bag = new ConstraintsBag();
		for (Constraint con : toBeChecked) {
			bag.add(con);
		}
		ProcessModel checkSpec = new ProcessModel(bag, "Test spec");
		ProcessSpecificationFitnessEvaluator relEvalor = 
				new ProcessSpecificationFitnessEvaluator(loPar.getEventEncoderDecoder(),checkSpec);
/*		
		ConstraintsFitnessEvaluator relEvalor = 
				new ConstraintsFitnessEvaluator(
						loPar.getEventEncoderDecoder(),
						toBeCheckock);
//						loPar.getTaskCharArchive(),
//						Arrays.asList(parametricConstraints));
*/

		
		
		ConstraintsFitnessEvaluationsMap evalon = null;
		ModelFitnessEvaluation fitEval = null;
		Iterator<LogTraceParser> trItator = loPar.traceIterator();
		evalon = relEvalor.runOnTrace(trItator.next());
		fitEval = relEvalor.evaluateOnTrace(trItator.next());
		/*
		if (args.length > 1) {
			evalon = relEvalor.runOnLog(loPar,Double.valueOf(args[1]));
		} else {
			evalon = relEvalor.runOnLog(loPar);
		}
		*/

		MessagePrinter.printlnOut(evalon.printCSV());
		MessagePrinter.printlnOut(fitEval.printCSV());

		if (args.length > 2) {
			logger.debug("Storing constraints as a Declare map on " + args[2]);

			Collection<Constraint> nuStandardConstraints = new ArrayList<Constraint>();
			Double fitnessThreshold = Double.valueOf(args[1]);

			for (Constraint con : relEvalor.getCheckedConstraints()) {
				if (con.getFamily() != null && con.getFitness() >= fitnessThreshold) {
					nuStandardConstraints.add(con);
				}
			}

			ConstraintsBag coBag = new ConstraintsBag(loPar.getTaskCharArchive().getTaskChars(), nuStandardConstraints);
			ProcessModel model = new ProcessModel(loPar.getTaskCharArchive(), coBag);
			DeclareMapReaderWriter.marshal(args[2], new DeclareMapEncoderDecoder(model).createDeclareMap());
			
			logger.debug("Done.");
		}
	}
}