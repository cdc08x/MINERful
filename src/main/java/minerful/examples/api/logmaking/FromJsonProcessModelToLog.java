package minerful.examples.api.logmaking;

import java.io.File;
import java.io.IOException;

import minerful.concept.ProcessModel;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;

import org.deckfour.xes.model.XLog;

/**
 * This usage exmaple class demonstrates how to generate XES logs starting with the definitions of constraints specified with JSON objects.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromJsonProcessModelToLog {
	public static final Integer MIN_EVENTS_PER_TRACE = 5;
	public static final Integer MAX_EVENTS_PER_TRACE = 45;
	public static final Long TRACES_IN_LOG = (long)50;
	public static final Encoding OUTPUT_ENCODING = Encoding.xes;
	public static final File OUTPUT_LOG = new File("/home/claudio/Desktop/log-from-JSON.xes");

	public static void main(String[] args) throws IOException {
		// This is a JSON string with the minimal definition of a process. It is not case sensitive, and allows for some extra spaces, dashes, etc. in the template names. */
		String processJsonMin =
				"{constraints: [" 
				+ "{template: Succession, parameters: [[A],[B]]}," 
				+ "{template: resPOnse, parameters: [[B],[C]]}," 
				+ "{template: EnD, parameters: [[D]]}," 
				+ "{template: existence, parameters: [[D]]}," 
				+ "{template: \"not chain-succession\", parameters: [[A],[B,D]]}" 
				+ "] }";
		// This is a JSON string with a process having the same constraints as before, but with an unconstrained task on more (E), specified in the "tasks" field. */
		String processJsonWithExtraTask =
				"{constraints: [" 
				+ "{template: Succession, parameters: [[A],[B]]}," 
				+ "{template: resPOnse, parameters: [[B],[C]]}," 
				+ "{template: EnD, parameters: [[D]]}," 
				+ "{template: existence, parameters: [[D]]}," 
				+ "{template: \"not chain-succession\", parameters: [[A],[B,D]]}" 
				+ "],"
				+"tasks: [A,B,C,D,E] }";

		ProcessModel proMod =
			new ProcessModelEncoderDecoder()
//		/* Alternative 1: load from file. Uncomment the following line to use this method. */ 
//			.readFromJsonFile(new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco.json"));
//		/* Alternative 2: load from a (minimal) string version of the JSON model. Uncomment the following line to use this method. */ 
			.readFromJsonString(processJsonMin);
//		/* Alternative 3: load from another string version of the JSON model. Uncomment the following line to use this method. */ 
//			.readFromJsonString(processJsonWithExtraTask);

		/*
		 * Specifies the parameters used to create the log
		 */
		LogMakerParameters logMakParameters =
				new LogMakerParameters(
						MIN_EVENTS_PER_TRACE, MAX_EVENTS_PER_TRACE, TRACES_IN_LOG);

		/*
		 * Creates the log.
		 */
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		/*
		 * The log XLog is an in-memory representation of the log, which can be later serialized in XES or MXML formats.
		 */
		XLog log = logMak.createLog(proMod);
		
		logMakParameters.outputEncoding = OUTPUT_ENCODING;
		//System.out.println(logMak.printEncodedLog());
		
		logMakParameters.outputLogFile = OUTPUT_LOG;
		logMak.storeLog();
	}
}