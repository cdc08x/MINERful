package minerful;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import minerful.concept.constraint.existence.AtMost1;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.AtLeast1;
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
import minerful.io.ConstraintsPrinter;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapReaderWriter;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.relevance.test.constraint.SequenceResponse21;
import minerful.relevance.test.constraint.SequenceResponse22;
import minerful.relevance.test.constraint.SequenceResponse32;
import minerful.utils.MessagePrinter;

public class MinerFulVacuityChecker {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulVacuityChecker.class);
			
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
		new Constraint[] {
			new SequenceResponse21(a,b,x),
//			new SequenceResponse22(a,b,x,y),
//			new SequenceResponse32(a,b,c,x,y),
			new AtLeast1(a),	// a.k.a. Existence(1, a)
//			new AtMostOne(a),	// a.k.a. Absence(2, a)
			new Init(a),
			new End(a),
//			new RespondedExistence(a,b),
//			new Response(a, b),
//			new AlternateResponse(a,b),
//			new ChainResponse(a,b),
//			new Precedence(a,b),
//			new AlternatePrecedence(a,b),
//			new ChainPrecedence(a,b),
//			new CoExistence(a,b),
//			new Succession(a,b),
//			new AlternateSuccession(a, b),
//			new ChainSuccession(a, b),
//			new NotChainSuccession(a, b),
//			new NotSuccession(a, b),
//			new NotCoExistence(a, b),
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
				"Usage: java " + MinerFulVacuityChecker.class.getCanonicalName() + " <XES-log-file-path> [threshold] [Declare-map-output-file-path]."
				+ "\n" +
				"Param:    <XES-log-file-path>: the path to a XES event log file (mandatory)"
				+ "\n" +
				"Param:    [threshold]: the ratio of traces in which the constraints have to be non-vacuously satisfied, from 0.0 to 1.0 (default: " + ConstraintsFitnessEvaluator.DEFAULT_FITNESS_THRESHOLD + ") (optional)"
				+ "\n" +
				"Param:    [Declare-map-output-file-path]: the path of the file in which the returned constraints are stored as a Declare Map XML file (by default, no Declare Map XML file is saved) (optional)"
				+ "\n\n" +

				"#### OUTPUT"
				+ "\n" +
				"To customise the constraint templates to be checked, please change the code of this class (" + MinerFulVacuityChecker.class.getCanonicalName() + ") in the specified point and recompile."
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

		ConstraintsFitnessEvaluator evalor = 
				new ConstraintsFitnessEvaluator(
						loPar.getEventEncoderDecoder(),
						loPar.getTaskCharArchive(),
						Arrays.asList(parametricConstraints));
		ConstraintsFitnessEvaluationsMap evalon = null;

		if (args.length > 1) {
			evalon = evalor.runOnLog(loPar, Double.valueOf(args[1]));
		} else {
			evalon = evalor.runOnLog(loPar);
		}

		MessagePrinter.printlnOut(evalon.printCSV());

		if (args.length > 2) {
			logger.debug("Storing fully-supported default-Declare constraints as a Declare map on " + args[2]);

			Collection<Constraint> nuStandardConstraints = new ArrayList<Constraint>();
			Double fitnessThreshold = Double.valueOf(args[1]);

			for (Constraint con : evalor.getCheckedConstraints()) {
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