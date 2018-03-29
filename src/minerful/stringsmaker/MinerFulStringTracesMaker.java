package minerful.stringsmaker;

import java.io.FileWriter;
import java.io.IOException;

import minerful.io.encdec.log.IOutEncoder;
import minerful.io.encdec.log.MxmlEncoder;
import minerful.io.encdec.log.XesEncoder;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;
import nl.flotsam.xeger.Xeger;

import org.apache.log4j.Logger;

@Deprecated
public class MinerFulStringTracesMaker {
    private static Logger logger = Logger.getLogger(MinerFulStringTracesMaker.class.getCanonicalName());

	public String[] makeTraces(StringTracesMakerCmdParameters params) {
        String regexp = "(" + params.regexps[0] + ")";
        Double avgChrsPerString = 0.0;
        long totalChrs = 0L;
        
        
        // building the intersection of the regular expressions
        for (int i = 1; i < params.regexps.length; i++) {
            regexp += "&(" + params.regexps[i] + ")";
        }
        
        // limiting the vocabulary
        String regexpLimitingTheVocabulary = "";
        for (Character s : params.alphabet) {
            regexpLimitingTheVocabulary += s;
        }
        regexp = "([" + regexpLimitingTheVocabulary + "]*)&" + regexp;

        // limiting the number of characters per string
        if (params.isMinChrsPerStringGiven() || params.isMaxChrsPerStringGiven()) {
            regexp =
                    regexp +
                    "&(.{" +
                    (   params.isMinChrsPerStringGiven()
                        ?   params.printMinChrsPerString()
                        :   "0"
                    ) +
                    "," +
                    (   params.isMaxChrsPerStringGiven()
                        ?   params.printMaxChrsPerString()
                        :   ""
                    ) +
                    "})";
        }

        // generating random strings
        Xeger generator = new Xeger(regexp);
        String[] testBedArray = new String[params.size.intValue()];

        int zeroPaddingCharsAmount = (int)(Math.ceil(Math.log10(testBedArray.length)));
        if (zeroPaddingCharsAmount < 1)
        	zeroPaddingCharsAmount = 1;

        for (int i = 0; i < testBedArray.length; i++) {
            testBedArray[i] = generator.generate();
            totalChrs += testBedArray[i].length();
            logger.trace(String.format("%0" + zeroPaddingCharsAmount + "d", (i))  + ")\t" + testBedArray[i]);
        }
        
        avgChrsPerString = 1.0 * totalChrs / params.size;

        logger.trace(
            "\n"
            + "[Testbed]"
            + (
                  "\n\n"
                + "Regular expression(s) generating the proofs: " + params.printRegExps() + "\n"
                + "(extended: " + regexp + ")\n"
                + "conjunction of " + params.getNumberOfConstraints() + " constraint(s)" + "\n"
                + "over " + params.size + " cases" + "\n"
                + "(length of strings ranging from " + params.printMinChrsPerString()
                + " to " + params.printMaxChrsPerString() + ")\n"
                + "(average length of strings: " + avgChrsPerString + ")\n"
                + "with the alphabet: " + params.printAlphabet()
            ).replaceAll("\n", "\n\t")
        );
        
        if (store(params, testBedArray) && params.logFile != null) {
        	logger.info("Log file stored in: " + params.logFile.getAbsolutePath());
        }
        
        return testBedArray;
    }

	public boolean store(StringTracesMakerCmdParameters params, String[] traces) {
		// saving
        IOutEncoder outEnco = null;
        switch (params.outputEncoding) {
        case xes:
        	outEnco = new XesEncoder(traces);
        	break;
        case mxml:
        	outEnco = new MxmlEncoder(traces);
        	break;
    	default:
    		break;
    	}
        
        if (outEnco != null) {
        	try {
	        	if (params.logFile != null) {
					outEnco.encodeToFile(params.logFile);
	        	} else {
	        		MessagePrinter.printlnOut(outEnco.encodeToString());
	        		System.out.flush();
	        	}
        	} catch (IOException e) {
        		logger.error("Encoding error", e);
        		return false;
        	}
        } else {
    		FileWriter fileWri = null;
            if (params.logFile != null) {
            	try {
            		fileWri = new FileWriter(params.logFile);
    			} catch (IOException e) {
    				logger.error("File writing error", e);
    				return false;
    			}
            	
            	if (traces.length > 0) {
                    StringBuffer tracesBuffer = new StringBuffer();
            		
                    for (int i = 0; i < traces.length; i++) {
                        tracesBuffer.append(traces[i] + "\n");
                    }

            		try {
            			fileWri.write(tracesBuffer.toString());
            			fileWri.flush();
            		} catch (IOException e) {
            			logger.error("File writing error", e);
            			return false;
            		}
            	}
            }
        }
        return true;
	}
}