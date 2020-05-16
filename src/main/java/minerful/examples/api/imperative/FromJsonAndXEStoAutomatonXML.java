package minerful.examples.api.imperative;

import java.io.File;
import java.io.IOException;

import minerful.MinerFulOutputManagementLauncher;
import minerful.concept.ProcessModel;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogParser;
import minerful.logparser.XesLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;

public class FromJsonAndXEStoAutomatonXML {
	public static final File OUTPUT_XML_FILE = new File("/home/claudio/example-process-automaton.xml");
	public static final File INPUT_XES_FILE = new File("/home/claudio/example-log.xes");

	public static void main(String[] args) throws Exception {
		// This is a JSON string with the definition of a process. It is not case sensitive, and allows for some extra spaces, dashes, etc. in the template names. */
		String processJsonMin =
				"{constraints: [" 
				+ "{template: respondedexistence, parameters: [['Submit abstract'],['Write new paper']]}," 
				+ "{template: response, parameters: [['Submit paper'],['Send confirmation email']]}," 
				+ "{template: succession, parameters: [['Submit paper'],['Review paper']]},"
				+ "{template: precedence, parameters: [['Review paper'],['Accept paper']]}," 
				+ "{template: notsuccession, parameters: [['Reject paper'],['Submit paper']]}," 
				+ "{template: notcoexistence, parameters: [['Accept paper'],['Reject paper']]}"
				+ "] }";

		ProcessModel proMod =
			new ProcessModelEncoderDecoder()
//		/* Alternative 1: load from file. Uncomment the following line to use this method. */ 
//			.readFromJsonFile(new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco.json"));
//		/* Alternative 2: load from a (minimal) string version of the JSON model. Uncomment the following line to use this method. */ 
			.readFromJsonString(processJsonMin);
		

		/*
		 * Read the log
		 */
		LogParser logParser = new XesLogParser(INPUT_XES_FILE, ClassificationType.LOG_SPECIFIED);
		
		/*
		 * Specifies the parameters used to create the automaton
		 */		
		OutputModelParameters outParams = new OutputModelParameters();
		outParams.fileToSaveXmlFileForAutomaton = OUTPUT_XML_FILE;
		
		new MinerFulOutputManagementLauncher().manageOutput(proMod, outParams, logParser);
		
		System.exit(0);
	}
}