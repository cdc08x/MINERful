package minerful.examples.api.logmaking;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.*;
import minerful.concept.constraint.relation.*;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;
import minerful.logparser.StringTaskClass;

import org.deckfour.xes.model.XLog;

/**
 * This usage example class demonstrates how to generate XES logs from a declarative process model created on the fly.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromStringsProcessModelToLog {

	public static Integer minEventsPerTrace = 5;
	public static Integer maxEventsPerTrace = 45;
	public static Long tracesInLog = (long)50;
	public static File outputLog = new File("/home/claudio/Desktop/test-log-output/out.xes");

	public static void main(String[] args) throws IOException {
//////////////////////////////////////////////////////////////////
// Creation of the process model...
//////////////////////////////////////////////////////////////////
		// Create the task factory (which automatically associates character IDs to tasks)
		TaskCharFactory tChFactory = new TaskCharFactory();
		
		// Create the tasks to be used to model the process
		TaskChar a0 = tChFactory.makeTaskChar("A0");
		TaskChar a0a1 = tChFactory.makeTaskChar("A0A1");
		TaskChar b0b1b2b0 = tChFactory.makeTaskChar("B0B1B2_BO");
		TaskChar b0b1b2b0b3 = tChFactory.makeTaskChar("B0B1B2_BOB1B2B3");

		// Create the tasks archive to store the "process alphabet"
		TaskCharArchive taChaAr = new TaskCharArchive(
				a0, a0a1, b0b1b2b0, b0b1b2b0b3
				);

		// Initialise the manager class of the bag of constraints constituting the declarative process model.
		// Notice that it requires the set of tasks as input, to know what the process alphabet is. 
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		
		// Add new constraints to the bag. The first one is a target-branched constraint:
		// it has two tasks assigned to the first parameter, instead of one as usual!
		bag.add(new AlternatePrecedence(new TaskCharSet(a0, a0a1), new TaskCharSet(b0b1b2b0)));
		bag.add(new Participation(b0b1b2b0));

		// Create the process model on the basis of the archive of tasks, and the constraints expressed thereupon
		ProcessModel proMod = new ProcessModel(taChaAr, bag);
		
//////////////////////////////////////////////////////////////////
// Creation of the log...
//////////////////////////////////////////////////////////////////
		// Initialise the parameters to creat the log
		LogMakerParameters logMakParameters =
				new LogMakerParameters(
						minEventsPerTrace, maxEventsPerTrace, tracesInLog);
		
		// Instantiate the class to make event logs, based on the parameters defined above
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		// Create the event log
		XLog log = logMak.createLog(proMod);

		// Store the log
		logMakParameters.outputEncoding = Encoding.xes;
		logMakParameters.outputLogFile = outputLog;
		logMak.storeLog();
	}
}