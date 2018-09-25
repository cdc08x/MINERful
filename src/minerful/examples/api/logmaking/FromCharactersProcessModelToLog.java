package minerful.examples.api.logmaking;

import java.io.File;
import java.io.IOException;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.Precedence;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;

import org.deckfour.xes.model.XLog;

/**
 * This usage example class demonstrates how to generate XES logs starting from the definitions of constraints exerted on activities identified by single characters.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromCharactersProcessModelToLog {
	public static Integer minEventsPerTrace = 0;
	public static Integer maxEventsPerTrace = 5;
	public static Long tracesInLog = (long)50;
	public static File outputLog = new File("/home/claudio/Desktop/Temp-MINERful/test-log-output/out.xes");

	public static void main(String[] args) throws IOException {
//////////////////////////////////////////////////////////////////
//Creation of the process model...
//////////////////////////////////////////////////////////////////
		// Create the tasks to be used to model the process
		TaskChar
			a = new TaskChar('a'),
			b = new TaskChar('b'),
			c = new TaskChar('c'),
			d = new TaskChar('d'),
			e = new TaskChar('e');

		// Create the task factory (which automatically associates character IDs to tasks)
		TaskCharArchive taChaAr = new TaskCharArchive(
				a,b,c,d,e
				);

		// Initialise the manager class of the bag of constraints constituting the declarative process model.
		// Notice that it requires the set of tasks as input, to know what the process alphabet is. 
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		
		// Add new constraints to the bag. The first one is a target-branched constraint:
		// it has two tasks assigned to the first parameter, instead of one as usual!
		bag.add(new Precedence(new TaskCharSet(a, b), new TaskCharSet(c)));
		bag.add(new Init(a));
		bag.add(new Participation(b));
		bag.add(new End(e));

		// Create the process model on the basis of the archive of tasks, and the constraints expressed thereupon
		ProcessModel proMod = new ProcessModel(taChaAr, bag);
		
//////////////////////////////////////////////////////////////////
//Creation of the log...
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