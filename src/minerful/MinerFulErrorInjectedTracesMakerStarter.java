package minerful;

import java.io.FileWriter;
import java.io.IOException;

import minerful.logmaker.errorinjector.ErrorInjector;
import minerful.logmaker.errorinjector.ErrorInjectorFactory;
import minerful.logmaker.errorinjector.params.ErrorInjectorCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.stringsmaker.MinerFulStringTracesMaker;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;


public class MinerFulErrorInjectedTracesMakerStarter extends AbstractMinerFulStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulErrorInjectedTracesMakerStarter.class);
	
    @Override
	public Options setupOptions() {
    	Options cmdLineOptions = new Options();
    	
    	Options systemOptions = SystemCmdParameters.parseableOptions(),
    			tracesMakOptions = StringTracesMakerCmdParameters.parseableOptions(),
    			errorInjectorOptions = ErrorInjectorCmdParameters.parseableOptions();
    	
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: tracesMakOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: errorInjectorOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
	public static String[] injectErrors(String[] testBedArray, StringTracesMakerCmdParameters tracesMakParams, ErrorInjectorCmdParameters errorInjexParams) {
        ErrorInjectorFactory errorInjexFactory = new ErrorInjectorFactory();
        ErrorInjector errorInjex = errorInjexFactory.createErrorInjector(
        		errorInjexParams.getErrorInjectionSpreadingPolicy(),
        		errorInjexParams.getErrorType(),
        		testBedArray);
		
    	errorInjex.setAlphabet(tracesMakParams.alphabet);
    	errorInjex.setErrorsInjectionPercentage(errorInjexParams.getErrorsInjectionPercentage());
    	if (errorInjexParams.isTargetCharDefined())
    		errorInjex.setTargetChar(errorInjexParams.getTargetChar());
    	
        logger.trace(
                (
                      "\n\n"
                    + "Error injection spreading policy: " + errorInjexParams.getErrorInjectionSpreadingPolicy() + "\n"
                    + "Error injection type: " + errorInjexParams.getErrorType() + "\n"
                    + "Error injection percentage: " + errorInjexParams.getErrorsInjectionPercentage() + "\n"
                    + "Target character: " + errorInjexParams.getTargetChar()
                ).replaceAll("\n", "\n\t")
            );

        testBedArray = errorInjex.injectErrors();

        if (errorInjexParams.logFile != null) {
        	StringBuffer tracesBuffer = new StringBuffer();
        	FileWriter fileWri = null;
        	try {
				fileWri = new FileWriter(errorInjexParams.logFile);
			} catch (IOException e) {
				logger.error("File writing error", e);
			}
        	for (int i = 0; i < testBedArray.length; i++) {
        		tracesBuffer.append(testBedArray[i] + "\n");
        	}
            if (tracesBuffer.length() > 0) {
            	try {
            		fileWri.write(tracesBuffer.toString());
            		fileWri.flush();
            	} catch (IOException e) {
            		logger.error("File writing error", e);
            	}
            	logger.info("Error-injected log file stored in: " + errorInjexParams.logFile.getAbsolutePath());
            }
        }

        return testBedArray;
	}

    public static void main(String[] args) {
    	MinerFulErrorInjectedTracesMakerStarter minErrTraMaker = new MinerFulErrorInjectedTracesMakerStarter();
    	Options cmdLineOptions = minErrTraMaker.setupOptions();
    	
    	StringTracesMakerCmdParameters tracesMakParams =
    			new StringTracesMakerCmdParameters(
    					cmdLineOptions,
    					args);
        ErrorInjectorCmdParameters errorInjexParams =
        		new ErrorInjectorCmdParameters(
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
    	
    	String[] testBedArray = new MinerFulStringTracesMaker().makeTraces(tracesMakParams);
    	testBedArray = injectErrors(testBedArray, tracesMakParams, errorInjexParams);
    	
    	logger.debug(
                "\n"
                + "[Testbed after error injection]");
        for (int i = 0; i < testBedArray.length; i++) {
            logger.debug(String.format("%0" + (int)(Math.ceil(Math.log10(testBedArray.length))) + "d", (i))  + ")\t" + testBedArray[i]);
        }

	}
}