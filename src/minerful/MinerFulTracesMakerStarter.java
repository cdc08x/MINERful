package minerful;

import minerful.params.SystemCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulTracesMakerStarter extends AbstractMinerFulStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulTracesMakerStarter.class);
			
	@Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			tracesMakOptions = StringTracesMakerCmdParameters.parseableOptions();
    	
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}

    public static void main(String[] args) {
    	MinerFulTracesMakerStarter traMakeStarter = new MinerFulTracesMakerStarter();
    	Options cmdLineOptions = traMakeStarter.setupOptions();
    	
    	StringTracesMakerCmdParameters tracesMakParams =
    			new StringTracesMakerCmdParameters(
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
        
    	MessagePrinter.configureLogging(systemParams.debugLevel);
    	
    	MinerFulStringTracesMaker traMaker = new MinerFulStringTracesMaker();
    	
    	String[] traces = traMaker.makeTraces(tracesMakParams);
    	traMaker.store(tracesMakParams, traces);
	}
}