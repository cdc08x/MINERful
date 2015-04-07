package minerful;

import org.apache.commons.cli.Options;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.io.encdec.declare.DeclareEncoderDecoder;
import minerful.logparser.XesLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputCmdParameters;
import minerful.params.InputCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;

public class MinerFulLauncher {
	private InputCmdParameters inputParams;
	private MinerFulCmdParameters minerFulParams;
	private ViewCmdParameters viewParams;
	private SystemCmdParameters systemParams;
	private MinerFulMinerStarter minerFulStarter;
	
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
        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
    	if (inputParams.inputFile == null) {
    		systemParams.printHelpForWrongUsage("Input file missing!", cmdLineOptions);
    		System.exit(1);
    	}
        
        MinerFulMinerStarter.configureLogging(systemParams.debugLevel);
        MinerFulMinerStarter.logger.info("Loading log...");
        
        XesLogParser logParser = new XesLogParser(inputParams.inputFile, fromInputParamToXesLogClassificationType(inputParams.eventClassification));
        
        DeclareEncoderDecoder.marshal("/home/claudio/Desktop/porcheriediraraspecie.xml", new MinerFulLauncher(inputParams, minerFulParams, viewParams, systemParams).mine(logParser.getFirstXLog()));
	}

	public MinerFulLauncher(InputCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams,
			SystemCmdParameters systemParams) {
		this.inputParams = inputParams;
		this.minerFulParams = minerFulParams;
		this.viewParams = viewParams;
		this.systemParams = systemParams;
		this.minerFulStarter = new MinerFulMinerStarter();
	}

	public DeclareMap mine(XLog xLog) {
		ClassificationType classiType = fromInputParamToXesLogClassificationType(this.inputParams.eventClassification);
		XesLogParser logParser = new XesLogParser(xLog, classiType);
		TaskCharRelatedConstraintsBag bag = minerFulStarter.mine(logParser, minerFulParams, viewParams, systemParams, logParser.getTaskCharArchive());

		return new DeclareEncoderDecoder(new ProcessModel(bag)).getMap();
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