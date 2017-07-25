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
import minerful.logmaker.params.LogMakerCmdParameters;
import minerful.logmaker.params.LogMakerCmdParameters.Encoding;
import minerful.logparser.StringTaskClass;

import org.deckfour.xes.model.XLog;

public class FromStringsProcessModelToLog {

	public static Integer minEventsPerTrace = 5;
	public static Integer maxEventsPerTrace = 45;
	public static Long tracesInLog = (long)50;
	public static File outputLog = new File("/home/claudio/Desktop/Temp-MINERful/test-log-output/out.xes");

	public static void main(String[] args) throws IOException {
		TaskCharEncoderDecoder tChEncDec = new TaskCharEncoderDecoder();
		TaskCharFactory tChFactory = new TaskCharFactory(tChEncDec);
		
		TaskChar a0 = tChFactory.makeTaskChar(new StringTaskClass("A0"));
		TaskChar a0a1 = tChFactory.makeTaskChar(new StringTaskClass("A0A1"));
		TaskChar b0b1b2b0 = tChFactory.makeTaskChar(new StringTaskClass("B0B1B2_BO"));
		TaskChar b0b1b2b0b3 = tChFactory.makeTaskChar(new StringTaskClass("B0B1B2_BOB1B2B3"));

		TaskCharArchive taChaAr = new TaskCharArchive(
				a0, a0a1, b0b1b2b0, b0b1b2b0b3
				);

		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());
		
		bag.add(new AlternatePrecedence(new TaskCharSet(a0, a0a1), new TaskCharSet(b0b1b2b0)));
		bag.add(new Participation(b0b1b2b0));

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