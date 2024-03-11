/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskCharArchive;
import minerful.io.params.OutputSpecificationParameters;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulSimuStarter extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulSimuStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				tracesMakerOptions = StringTracesMakerCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions(),
				outputOptions = OutputSpecificationParameters.parseableOptions(),
				postProptions = PostProcessingCmdParameters.parseableOptions();
		
    	for (Object opt: postProptions.getOptions()) {
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
    	for (Object opt: systemOptions.getOptions()) {
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
    	MinerFulSimuStarter minerSimuStarter = new MinerFulSimuStarter();
    	Options cmdLineOptions = minerSimuStarter.setupOptions();
    	
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
		OutputSpecificationParameters outParams =
				new OutputSpecificationParameters(
						cmdLineOptions,
						args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters(
						cmdLineOptions,
						args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        
        MessagePrinter.configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = new String[0];
        
        testBedArray = new MinerFulStringTracesMaker().makeTraces(tracesMakParams);
		try {
			LogParser stringLogParser = new StringLogParser(testBedArray, ClassificationType.NAME);
			TaskCharArchive taskCharArchive = new TaskCharArchive(stringLogParser.getEventEncoderDecoder().getTranslationMap());

	        // minerSimuStarter.mine(testBedArray, minerFulParams, tracesMakParams, systemParams);
			ProcessSpecification processSpecification = minerSimuStarter.mine(stringLogParser, minerFulParams, postParams, taskCharArchive);
	        
	        MinerFulOutputManagementLauncher proViewStarter = new MinerFulOutputManagementLauncher(); 
	        proViewStarter.manageOutput(processSpecification, viewParams, outParams, systemParams, stringLogParser);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
 }