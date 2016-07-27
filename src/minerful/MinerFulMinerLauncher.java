package minerful;

import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.XesLogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputCmdParameters;
import minerful.params.InputCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

import org.apache.commons.cli.Options;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

public class MinerFulMinerLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulMinerLauncher.class);

	private InputCmdParameters inputParams;
	private MinerFulCmdParameters minerFulParams;
	private SystemCmdParameters systemParams;
	private PostProcessingCmdParameters postParams;
	private MinerFulMinerStarter minerFulStarter;
	private LogParser logParser;
	private ViewCmdParameters viewParams;
	private OutputModelParameters outParams;
	
	/**
	 * For dummy testing only.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
    	MinerFulMinerStarter minerMinaStarter = new MinerFulMinerStarter();
    	Options cmdLineOptions = minerMinaStarter.setupOptions();
    	
    	InputCmdParameters inputParams =
    			new InputCmdParameters(
    					cmdLineOptions,
    					args);
        MinerFulCmdParameters minerFulParams =
        		new MinerFulCmdParameters(
        				cmdLineOptions,
    					args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        PostProcessingCmdParameters postParams =
        		new PostProcessingCmdParameters(cmdLineOptions, args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
    	if (inputParams.inputFile == null) {
    		systemParams.printHelpForWrongUsage("Input file missing!", cmdLineOptions);
    		System.exit(1);
    	}
        
        MessagePrinter.configureLogging(systemParams.debugLevel);
        logger.info("Loading log...");
        
        XesLogParser logParser = new XesLogParser(inputParams.inputFile, fromInputParamToXesLogClassificationType(inputParams.eventClassification));
        
//        DeclareEncoderDecoder.marshal(MinerFulLauncher.TEST_OUTPUT, new MinerFulLauncher(inputParams, minerFulParams, viewParams, systemParams).mine(logParser.getFirstXLog()));
        new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams).mine(logParser.getFirstXLog());
        
        System.exit(0);
	}

	public MinerFulMinerLauncher(InputCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams) {
		this(inputParams, minerFulParams, postParams, systemParams, null, null);
	}

	public MinerFulMinerLauncher(InputCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams,
			ViewCmdParameters viewParams, OutputModelParameters outParams) {
		this.inputParams = inputParams;
		this.minerFulParams = minerFulParams;
		this.systemParams = systemParams;
		this.postParams = postParams;
		this.viewParams = viewParams;
		this.outParams = outParams;
		
		this.minerFulStarter = new MinerFulMinerStarter();
        MessagePrinter.configureLogging(systemParams.debugLevel);
	}
	
	public ProcessModel mine() {
    	if (inputParams.inputFile == null) {
    		MessagePrinter.printlnError("Missing input file");
    		System.exit(1);
    	}
    	
        MinerFulMinerStarter.logger.info("Loading log...");
        
        logParser = MinerFulMinerStarter.deriveLogParserFromLogFile(inputParams, minerFulParams);
		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();
		return minerFulStarter.mine(logParser, minerFulParams, systemParams, postParams, taskCharArchive);
	}
	
	public ProcessModel manageOutput(ProcessModel processModel) {
		new MinerFulOutputManagementLauncher().manageOutput(processModel, viewParams, outParams, systemParams, logParser);
		return processModel;
	}

	public DeclareMap mine(XLog xLog) {
		ClassificationType classiType = fromInputParamToXesLogClassificationType(this.inputParams.eventClassification);
		XesLogParser logParser = new XesLogParser(xLog, classiType);
		ProcessModel processModel = minerFulStarter.mine(logParser, minerFulParams, systemParams, postParams, logParser.getTaskCharArchive());

		return new DeclareMapEncoderDecoder(processModel).createDeclareMap();
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


}