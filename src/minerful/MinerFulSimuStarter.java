/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulSimuStarter extends MinerFulMinerStarter {

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				tracesMakerOptions = StringTracesMakerCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions();
		
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakerOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
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
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        
        configureLogging(systemParams.debugLevel);
        
        String[] testBedArray = new String[0];
        
        testBedArray = new MinerFulStringTracesMaker().makeTraces(tracesMakParams);
		try {
			LogParser stringLogParser = new StringLogParser(testBedArray, ClassificationType.NAME);
			TaskCharArchive taskCharArchive = new TaskCharArchive(stringLogParser.getEventEncoderDecoder().getTranslationMap());

	        // minerSimuStarter.mine(testBedArray, minerFulParams, tracesMakParams, systemParams);
			ProcessModel processModel = minerSimuStarter.mine(stringLogParser, minerFulParams, viewParams, systemParams, taskCharArchive);
	        
	        MinerFulProcessViewerStarter proViewStarter = new MinerFulProcessViewerStarter(); 
	        proViewStarter.print(processModel, viewParams, systemParams, stringLogParser);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
    }
 }