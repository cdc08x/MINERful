package minerful;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskCharArchive;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.OutputSpecificationParameters;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulMinerLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulMinerLauncher.class);

	private InputLogCmdParameters inputParams;
	private MinerFulCmdParameters minerFulParams;
	private SystemCmdParameters systemParams;
	private PostProcessingCmdParameters postParams;
	private MinerFulMinerStarter minerFulStarter;
	private LogParser logParser;
	private ViewCmdParameters viewParams;
	private OutputSpecificationParameters outParams;

	public MinerFulMinerLauncher(InputLogCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams) {
		this(inputParams, minerFulParams, postParams, systemParams, null, null);
	}

	public MinerFulMinerLauncher(InputLogCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams,
			ViewCmdParameters viewParams, OutputSpecificationParameters outParams) {
		this.inputParams = inputParams;
		this.minerFulParams = minerFulParams;
		this.systemParams = systemParams;
		this.postParams = postParams;
		this.viewParams = viewParams;
		this.outParams = outParams;
		
		this.minerFulStarter = new MinerFulMinerStarter();
        MessagePrinter.configureLogging(systemParams.debugLevel);
	}
	
	public ProcessSpecification mine() {
    	if (inputParams.inputLogFile == null) {
    		MessagePrinter.printlnError("Missing input file");
    		System.exit(1);
    	}
    	
        logger.info("Loading log...");
        
        logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputParams, minerFulParams);
		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();
		return minerFulStarter.mine(logParser, inputParams, minerFulParams, postParams, taskCharArchive);
	}
	
	public ProcessSpecification mine(XLog xLog) {
		ClassificationType classiType = fromInputParamToXesLogClassificationType(this.inputParams.eventClassification);
		logParser = new XesLogParser(xLog, classiType);
		return minerFulStarter.mine(logParser, inputParams, minerFulParams, postParams, logParser.getTaskCharArchive());
	}	

	public DeclareMap mineDeclareMap(XLog xLog) {
		return new DeclareMapEncoderDecoder(mine(xLog)).createDeclareMap();
	}
	
	public static ClassificationType fromInputParamToXesLogClassificationType(EventClassification evtClassInputParam) {
		switch (evtClassInputParam) {
		case name:
			return ClassificationType.NAME;
		case logspec:
			return ClassificationType.LOG_SPECIFIED;
		default:
			throw new UnsupportedOperationException("Classification strategy " + evtClassInputParam + " not yet implemented");
		}
	}

	/**
	 * Derives a {@link LogParser log parser} from an event log file.
	 * @param inputParams Input parameters locating and categorising the input log file.
	 * @return A log parser.
	 */
	public static LogParser deriveLogParserFromLogFile(InputLogCmdParameters inputParams) {
		return deriveLogParserFromLogFile(inputParams, null);
	}


	/**
	 * Derives a {@link LogParser log parser} from an event log file with the capability of ignoring certain tasks from the traces.
	 * @param inputParams Input parameters locating and categorising the input log file.
	 * @param minerFulParams Input parameters defining the tasks to be excluded from the log file parsing.
	 * @return A log parser.
	 */
	public static LogParser deriveLogParserFromLogFile(InputLogCmdParameters inputParams, MinerFulCmdParameters minerFulParams) {
		LogParser logParser = null;
		boolean doAnalyseSubLog =
				!inputParams.startFromTrace.equals(InputLogCmdParameters.FIRST_TRACE_NUM)
				||
				!inputParams.subLogLength.equals(InputLogCmdParameters.WHOLE_LOG_LENGTH);
		switch (inputParams.inputLanguage) {
		case xes:
		case mxml:
			ClassificationType evtClassi = MinerFulMinerLauncher.fromInputParamToXesLogClassificationType(inputParams.eventClassification);
			try {
				if (doAnalyseSubLog) {
					logParser = new XesLogParser(inputParams.inputLogFile, evtClassi, inputParams.startFromTrace, inputParams.subLogLength);
				} else {
					logParser = new XesLogParser(inputParams.inputLogFile, evtClassi);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Let us try to free memory from the unused XesDecoder!
			System.gc();
			break;
		case strings:
			try {
				if (doAnalyseSubLog) {
					logParser = new StringLogParser(inputParams.inputLogFile, ClassificationType.NAME, inputParams.startFromTrace, inputParams.subLogLength);
				} else {
					logParser = new StringLogParser(inputParams.inputLogFile, ClassificationType.NAME);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			break;
		default:
			throw new UnsupportedOperationException("This encoding ("
					+ inputParams.inputLanguage + ") is not yet supported");
		}

		// Remove from the analysed alphabet those activities that are
		// specified in a user-defined list
		if (minerFulParams != null && minerFulParams.tasksToBeExcludedFromResult != null && minerFulParams.tasksToBeExcludedFromResult.size() > 0) {
			logParser.excludeTasksByName(minerFulParams.tasksToBeExcludedFromResult);
		}

		return logParser;
	}
}