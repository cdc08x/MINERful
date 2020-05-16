package minerful.examples.api.logmaking;

import java.io.File;
import java.io.IOException;

import minerful.concept.ProcessModel;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;

import org.deckfour.xes.model.XLog;

/**
 * This usage example class demonstrates how to generate XES logs from an existing Declare map XML file.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromDeclareMapToLog {
	public static final Integer MIN_EVENTS_PER_TRACE = 5;
	public static final Integer MAX_EVENTS_PER_TRACE = 45;
	public static final Long TRACES_IN_LOG = (long)100;
	public static final Encoding OUTPUT_ENCODING = Encoding.xes;
	public static final File OUTPUT_LOG = new File("/home/claudio/Desktop/log-from-Declare-map.xes");

	public static void main(String[] args) throws IOException {
		/*
		 * There are two possible methods of DeclareEncoderDecoder to create a
		 * minerful.concept.ProcessModel out of a Declare Map:
		 * 1)	public static ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath)
		 * 2)	public static ProcessModel fromDeclareMapToMinerfulProcessModel(org.processmining.plugins.declareminer.visualizing.AssignmentModel declareMapModel) {
		 * The first one is used here, and reads an XML representation of the Declare map.
		 * The second one can be used to pass in-memory representations of the Declare map.
		 */
		ProcessModel proMod =
				new DeclareMapEncoderDecoder(
						"/home/claudio/model.xml"
				).createMinerFulProcessModel();
		
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
		System.out.println(logMak.printEncodedLog());
		
		logMakParameters.outputLogFile = OUTPUT_LOG;
		logMak.storeLog();
	}
}