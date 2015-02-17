package minerful;

import minerful.params.SystemCmdParameters;
import minerful.tracemaker.MinerFulTracesMaker;
import minerful.tracemaker.params.TracesMakerCmdParameters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulTracesMakerStarter extends AbstractMinerFulStarter {
	@Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			tracesMakOptions = TracesMakerCmdParameters.parseableOptions();
    	
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
    	
    	TracesMakerCmdParameters tracesMakParams =
    			new TracesMakerCmdParameters(
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
    	
    	MinerFulTracesMaker traMaker = new MinerFulTracesMaker();
    	
    	String[] traces = traMaker.makeTraces(tracesMakParams);
    	traMaker.store(tracesMakParams, traces);
	}
}