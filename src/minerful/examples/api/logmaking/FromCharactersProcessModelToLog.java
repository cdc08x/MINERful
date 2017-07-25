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
import minerful.logmaker.params.LogMakerCmdParameters;
import minerful.logmaker.params.LogMakerCmdParameters.Encoding;

import org.deckfour.xes.model.XLog;

/**
 * This usage example class generates XES logs starting from the definitions of constraints exerted on activities identified by single characters.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromCharactersProcessModelToLog {
	public static Integer minEventsPerTrace = 0;
	public static Integer maxEventsPerTrace = 5;
	public static Long tracesInLog = (long)50;
	public static File outputLog = new File("/home/claudio/Desktop/Temp-MINERful/test-log-output/out.xes");

	public static void main(String[] args) throws IOException {
		TaskCharEncoderDecoder tChEncDec = new TaskCharEncoderDecoder();
		
		TaskChar
			a = new TaskChar('a'),
			b = new TaskChar('b'),
			c = new TaskChar('c'),
			d = new TaskChar('d'),
			e = new TaskChar('e');

		TaskCharArchive taChaAr = new TaskCharArchive(
				a,b,c,d,e
				);

		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		
		bag.add(new Precedence(new TaskCharSet(a, b), new TaskCharSet(c)));
		bag.add(new Init(new TaskCharSet(a,b)));
		bag.add(new Participation(new TaskCharSet(b,c)));
		bag.add(new End(new TaskCharSet(d,e)));

		ProcessModel proMod = new ProcessModel(taChaAr, bag);
		
		LogMakerCmdParameters logMakParameters =
				new LogMakerCmdParameters(
						minEventsPerTrace, maxEventsPerTrace, tracesInLog);
		
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		XLog log = logMak.createLog(proMod);

		logMakParameters.outputEncoding = Encoding.xes;
		logMakParameters.outputLogFile = outputLog;
		logMak.storeLog();
	}
}