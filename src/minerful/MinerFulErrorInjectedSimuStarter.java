package minerful;

import minerful.concept.ProcessModel;
import minerful.io.params.OutputModelParameters;
import minerful.logmaker.errorinjector.params.ErrorInjectorCmdParameters;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulErrorInjectedSimuStarter extends MinerFulSimuStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulErrorInjectedSimuStarter.class);

	@Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				tracesMakerOptions = StringTracesMakerCmdParameters.parseableOptions(),
    			errorInjectorOptions = ErrorInjectorCmdParameters.parseableOptions(),
    			viewOptions = ViewCmdParameters.parseableOptions(),
    			outputOptions = OutputModelParameters.parseableOptions(),
    			postProptions = PostProcessingCmdParameters.parseableOptions();
    	
    	for (Object opt: postProptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakerOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: outputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: errorInjectorOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    /**
     * @param args the command line arguments:
     * 	[regular expression]
     *  [number of strings]
     *  [minimum number of characters per string]
     *  [maximum number of characters per string]
     *  [alphabet]...
     */
    public static void main(String[] args) {
    	MinerFulErrorInjectedSimuStarter minErrSimuSta = new MinerFulErrorInjectedSimuStarter();
    	Options cmdLineOptions = minErrSimuSta.setupOptions();
    	
        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
    	StringTracesMakerCmdParameters tracesMakParams =
    			new StringTracesMakerCmdParameters(
    					cmdLineOptions,
    					args);
        MinerFulCmdParameters minerFulParams =
        		new MinerFulCmdParameters(
        				cmdLineOptions,
    					args);
        ErrorInjectorCmdParameters errorInjexParams =
        		new ErrorInjectorCmdParameters(
        				cmdLineOptions,
        				args);
		OutputModelParameters outParams =
				new OutputModelParameters(
						cmdLineOptions,
						args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters(cmdLineOptions, args);
       
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        
        MessagePrinter.configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = new MinerFulStringTracesMaker().makeTraces(tracesMakParams);
    	testBedArray = MinerFulErrorInjectedTracesMakerStarter.injectErrors(testBedArray, tracesMakParams, errorInjexParams);
    	
		try {
			LogParser stringLogParser = new StringLogParser(testBedArray, ClassificationType.NAME);

	        // minerSimuStarter.mine(testBedArray, minerFulParams, tracesMakParams, systemParams);
	        ProcessModel processModel = new MinerFulMinerStarter().mine(stringLogParser, minerFulParams, postParams, tracesMakParams.alphabet);

	        MinerFulOutputManagementLauncher proViewLauncher = new MinerFulOutputManagementLauncher(); 
	        proViewLauncher.manageOutput(processModel, viewParams, outParams, systemParams, stringLogParser);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
}