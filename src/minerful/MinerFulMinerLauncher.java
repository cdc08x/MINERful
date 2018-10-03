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
    	if (inputParams.inputLogFile == null) {
    		MessagePrinter.printlnError("Missing input file");
    		System.exit(1);
    	}
    	
        logger.info("Loading log...");
        
        logParser = MinerFulMinerStarter.deriveLogParserFromLogFile(inputParams, minerFulParams);
		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();
		return minerFulStarter.mine(logParser, inputParams, minerFulParams, systemParams, postParams, taskCharArchive);
	}
	
	public ProcessModel manageOutput(ProcessModel processModel) {
		new MinerFulOutputManagementLauncher().manageOutput(processModel, viewParams, outParams, systemParams, logParser);
		return processModel;
	}

	public DeclareMap mine(XLog xLog) {
		ClassificationType classiType = fromInputParamToXesLogClassificationType(this.inputParams.eventClassification);
		XesLogParser logParser = new XesLogParser(xLog, classiType);
		ProcessModel processModel = minerFulStarter.mine(logParser, inputParams, minerFulParams, systemParams, postParams, logParser.getTaskCharArchive());

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