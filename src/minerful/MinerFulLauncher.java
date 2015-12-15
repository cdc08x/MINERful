package minerful;

import org.apache.commons.cli.Options;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.ConstraintsBag;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logparser.XesLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputCmdParameters;
import minerful.params.InputCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParams;

public class MinerFulLauncher {
	private InputCmdParameters inputParams;
	private MinerFulCmdParameters minerFulParams;
	private SystemCmdParameters systemParams;
	private PostProcessingCmdParams postParams;
	private MinerFulMinerStarter minerFulStarter;
	
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
        PostProcessingCmdParams postParams =
        		new PostProcessingCmdParams(cmdLineOptions, args);
        
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
        
//        DeclareEncoderDecoder.marshal(MinerFulLauncher.TEST_OUTPUT, new MinerFulLauncher(inputParams, minerFulParams, viewParams, systemParams).mine(logParser.getFirstXLog()));
        new MinerFulLauncher(inputParams, minerFulParams, postParams, systemParams).mine(logParser.getFirstXLog());
        
        System.exit(0);
	}

	public MinerFulLauncher(InputCmdParameters inputParams,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParams postParams, SystemCmdParameters systemParams) {
		this.inputParams = inputParams;
		this.minerFulParams = minerFulParams;
		this.systemParams = systemParams;
		this.postParams = postParams;
		this.minerFulStarter = new MinerFulMinerStarter();
	}

	public DeclareMap mine(XLog xLog) {
		ClassificationType classiType = fromInputParamToXesLogClassificationType(this.inputParams.eventClassification);
		XesLogParser logParser = new XesLogParser(xLog, classiType);
		ProcessModel processModel = minerFulStarter.mine(logParser, minerFulParams, systemParams, postParams, logParser.getTaskCharArchive());

		return new DeclareMapEncoderDecoder(processModel).getMap();
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