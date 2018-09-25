/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.io.params.InputModelParameters;
import minerful.logmaker.params.LogMakerParameters;
import minerful.params.SystemCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulLogMakerStarter extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulLogMakerStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options systemOptions = SystemCmdParameters.parseableOptions(),
				inputOptions = InputModelParameters.parseableOptions(),
				logMakOptions = LogMakerParameters.parseableOptions();

		for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: logMakOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    public static void main(String[] args) {
    	MinerFulLogMakerStarter logMakerStarter = new MinerFulLogMakerStarter();
    	Options cmdLineOptions = logMakerStarter.setupOptions();
    	
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		InputModelParameters inputParams =
				new InputModelParameters(
						cmdLineOptions,
						args);
		LogMakerParameters logMakParameters =
				new LogMakerParameters(
						cmdLineOptions,
						args);
        
        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }

		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}
        
        MessagePrinter.configureLogging(systemParams.debugLevel);
        
        new MinerFulLogMakerLauncher(inputParams, logMakParameters, systemParams).makeLog();
    }
 }