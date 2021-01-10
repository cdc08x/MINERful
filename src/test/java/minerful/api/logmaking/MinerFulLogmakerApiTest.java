package minerful.api.logmaking;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.junit.jupiter.api.Test;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.Precedence;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logmaker.params.LogMakerParameters.Encoding;

/**
 * Tests for Logmaker API
 */
class MinerFulLogmakerApiTest {

	public static final File OUTPUT_LOG_1 = new File("src/test/resources/logmaker/output/logmaker_output1.xes");
	public static final File OUTPUT_LOG_2 = new File("src/test/resources/logmaker/output/logmaker_output2.xes");
	public static final File OUTPUT_LOG_3 = new File("src/test/resources/logmaker/output/logmaker_output3.mxml");
	public static final File OUTPUT_LOG_4 = new File("src/test/resources/logmaker/output/logmaker_output4.xes");

	public static final Integer MIN_EVENTS_PER_TRACE = 1;
	public static final Integer MAX_EVENTS_PER_TRACE = 4;
	public static final Long TRACES_IN_LOG = (long) 5;
	public static final Encoding OUTPUT_ENCODING = Encoding.xes;

	@Test
	void testFromCharactersProcessModelToLog() throws IOException {
		// ------------------------------------------------------------------
		// Creation of the process model...
		// ------------------------------------------------------------------
		// Create the tasks to be used to model the process
		TaskChar a = new TaskChar('a'), b = new TaskChar('b'), c = new TaskChar('c'), d = new TaskChar('d'),
				e = new TaskChar('e');

		// Create the task factory (which automatically associates character IDs to
		// tasks)
		TaskCharArchive taChaAr = new TaskCharArchive(a, b, c, d, e);

		// Initialise the manager class of the bag of constraints constituting the
		// declarative process model.
		// Notice that it requires the set of tasks as input, to know what the process
		// alphabet is.
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());

		// Add new constraints to the bag. The first one is a target-branched
		// constraint:
		// it has two tasks assigned to the first parameter, instead of one as usual!
		bag.add(new Precedence(new TaskCharSet(a, b), new TaskCharSet(c)));
		bag.add(new Init(a));
		bag.add(new Participation(b));
		bag.add(new End(e));

		// Create the process model on the basis of the archive of tasks, and the
		// constraints expressed thereupon
		ProcessModel proMod = new ProcessModel(taChaAr, bag);

		//////////////////////////////////////////////////////////////////
		// Creation of the log...
		//////////////////////////////////////////////////////////////////
		// Initialise the parameters to creat the log
		LogMakerParameters logMakParameters = new LogMakerParameters(MIN_EVENTS_PER_TRACE, MAX_EVENTS_PER_TRACE,
				TRACES_IN_LOG);

		// Instantiate the class to make event logs, based on the parameters defined
		// above
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		// Create the event log
		XLog log = logMak.createLog(proMod);

		// Store the log
		logMakParameters.outputEncoding = Encoding.xes;
		logMakParameters.outputLogFile = OUTPUT_LOG_1;
		logMak.storeLog();
	}

	@Test
	void testFromJsonProcessModelToLog() throws IOException {
		// This is a JSON string with the minimal definition of a process. It is not
		// case sensitive, and allows for some extra spaces, dashes, etc. in the
		// template names. */
		String processJsonMin = "{constraints: [" + "{template: Succession, parameters: [[A],[B]]},"
				+ "{template: resPOnse, parameters: [[B],[C]]}," + "{template: EnD, parameters: [[D]]},"
				+ "{template: existence, parameters: [[D]]},"
				+ "{template: \"not chain-succession\", parameters: [[A],[B,D]]}" + "] }";
		// This is a JSON string with a process having the same constraints as before,
		// but with an unconstrained task on more (E), specified in the "tasks" field.
		// */
		String processJsonWithExtraTask = "{constraints: [" + "{template: Succession, parameters: [[A],[B]]},"
				+ "{template: resPOnse, parameters: [[B],[C]]}," + "{template: EnD, parameters: [[D]]},"
				+ "{template: existence, parameters: [[D]]},"
				+ "{template: \"not chain-succession\", parameters: [[A],[B,D]]}" + "]," + "tasks: [A,B,C,D,E] }";

		ProcessModel proMod = new ProcessModelEncoderDecoder()
//				/* Alternative 1: load from file. Uncomment the following line to use this method. */ 
//					.readFromJsonFile(new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco.json"));
//				/* Alternative 2: load from a (minimal) string version of the JSON model. Uncomment the following line to use this method. */ 
				.readFromJsonString(processJsonMin);
//				/* Alternative 3: load from another string version of the JSON model. Uncomment the following line to use this method. */ 
//					.readFromJsonString(processJsonWithExtraTask);

		/*
		 * Specifies the parameters used to create the log
		 */
		LogMakerParameters logMakParameters = new LogMakerParameters(MIN_EVENTS_PER_TRACE, MAX_EVENTS_PER_TRACE,
				TRACES_IN_LOG);

		/*
		 * Creates the log.
		 */
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		/*
		 * The log XLog is an in-memory representation of the log, which can be later
		 * serialized in XES or MXML formats.
		 */
		XLog log = logMak.createLog(proMod);

		logMakParameters.outputEncoding = OUTPUT_ENCODING;
		// System.out.println(logMak.printEncodedLog());

		logMakParameters.outputLogFile = OUTPUT_LOG_2;
		logMak.storeLog();
	}

	@Test
	void testFromStringsProcessModelToLog() throws IOException {
		//////////////////////////////////////////////////////////////////
		// Creation of the process model...
		//////////////////////////////////////////////////////////////////
		// Create the task factory (which automatically associates character IDs to
		////////////////////////////////////////////////////////////////// tasks)
		TaskCharFactory tChFactory = new TaskCharFactory();

		// Create the tasks to be used to model the process
		TaskChar a0 = tChFactory.makeTaskChar("A0");
		TaskChar a0a1 = tChFactory.makeTaskChar("A0A1");
		TaskChar b0b1b2b0 = tChFactory.makeTaskChar("B0B1B2_BO");
		TaskChar b0b1b2b0b3 = tChFactory.makeTaskChar("B0B1B2_BOB1B2B3");

		// Create the tasks archive to store the "process alphabet"
		TaskCharArchive taChaAr = new TaskCharArchive(a0, a0a1, b0b1b2b0, b0b1b2b0b3);

		// Initialise the manager class of the bag of constraints constituting the
		// declarative process model.
		// Notice that it requires the set of tasks as input, to know what the process
		// alphabet is.
		ConstraintsBag bag = new ConstraintsBag(taChaAr.getTaskChars());

		// Add new constraints to the bag. The first one is a target-branched
		// constraint:
		// it has two tasks assigned to the first parameter, instead of one as usual!
		bag.add(new AlternatePrecedence(new TaskCharSet(a0, a0a1), new TaskCharSet(b0b1b2b0)));
		bag.add(new Participation(b0b1b2b0));

		// Create the process model on the basis of the archive of tasks, and the
		// constraints expressed thereupon
		ProcessModel proMod = new ProcessModel(taChaAr, bag);

		//////////////////////////////////////////////////////////////////
		// Creation of the log...
		//////////////////////////////////////////////////////////////////
		// Initialise the parameters to creat the log
		LogMakerParameters logMakParameters = new LogMakerParameters(MIN_EVENTS_PER_TRACE, MAX_EVENTS_PER_TRACE,
				TRACES_IN_LOG);

		// Instantiate the class to make event logs, based on the parameters defined
		// above
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		// Create the event log
		XLog log = logMak.createLog(proMod);

		// Store the log
		logMakParameters.outputEncoding = Encoding.mxml;
		logMakParameters.outputLogFile = OUTPUT_LOG_3;
		logMak.storeLog();
	}

	@Test
	void testFromDeclareMapToLog() throws IOException {
		/*
		 * There are two possible methods of DeclareEncoderDecoder to create a
		 * minerful.concept.ProcessModel out of a Declare Map: 1) public static
		 * ProcessModel fromDeclareMapToMinerfulProcessModel(String declareMapFilePath)
		 * 2) public static ProcessModel
		 * fromDeclareMapToMinerfulProcessModel(org.processmining.plugins.declareminer.
		 * visualizing.AssignmentModel declareMapModel) { The first one is used here,
		 * and reads an XML representation of the Declare map. The second one can be
		 * used to pass in-memory representations of the Declare map.
		 */
		ProcessModel proMod = new DeclareMapEncoderDecoder("src/test/resources/logmaker/testlog.xml")
				.createMinerFulProcessModel();

		/*
		 * Specifies the parameters used to create the log
		 */
		LogMakerParameters logMakParameters = new LogMakerParameters(MIN_EVENTS_PER_TRACE, MAX_EVENTS_PER_TRACE,
				TRACES_IN_LOG);

		/*
		 * Creates the log.
		 */
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParameters);

		/*
		 * The log XLog is an in-memory representation of the log, which can be later
		 * serialized in XES or MXML formats.
		 */
		XLog log = logMak.createLog(proMod);

		logMakParameters.outputEncoding = OUTPUT_ENCODING;
		System.out.println(logMak.printEncodedLog());

		logMakParameters.outputLogFile = OUTPUT_LOG_4;
		logMak.storeLog();
	}

}
