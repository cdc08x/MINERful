package minerful.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import minerful.utils.MessagePrinter;

public abstract class ParamsManager {
    public static final String EXPERIMENTAL_DEVELOPMENT_STAGE_MESSAGE = 
			"*** WARNING: experimental development stage of implementation!";
	private static final int DEFAULT_PROMPT_WIDTH = 160;
    protected HelpFormatter helpFormatter = new HelpFormatter();
	public static final String ARRAY_TOKENISER_SEPARATOR = MessagePrinter.ARRAY_TOKENISER_SEPARATOR;

    public ParamsManager() {
    	helpFormatter.setWidth(DEFAULT_PROMPT_WIDTH);
	}

	public void printHelp() {
    	this.printHelp(this.listParseableOptions());
    }

    public void printHelp(Options options) {
    	helpFormatter.printHelp("cmd_name", options, true);
    }
    
    public void printHelpForWrongUsage(String errorMessage, Options options) {
    	System.err.println("Wrong usage: " + errorMessage);
    	this.printHelp(options);
    }
    
    public void printHelpForWrongUsage(String errorMessage) {
    	this.printHelpForWrongUsage(errorMessage, this.listParseableOptions());
    }

    public Options addParseableOptions(Options options) {
        Options myOptions = listParseableOptions();
        for (Object myOpt : myOptions.getOptions()) {
            options.addOption((Option) myOpt);
        }
        return options;
    }

    protected void parseAndSetup(Options otherOptions, String[] args) {
        // create the command line parser
        CommandLineParser parser = new PosixParser();
        Options options = addParseableOptions(otherOptions);
        try {
        	CommandLine line = parser.parse(options, args, false);
        	setup(line);
        } catch (ParseException exp) {
            System.err.println("Unexpected exception:" + exp.getMessage());
        }
    }

    public Options listParseableOptions() {
    	return parseableOptions();
    }
    
	protected File openInputFile(CommandLine line, String paramName) {
		File inpuFile = null;
		if (!line.hasOption(paramName))
			return inpuFile;
		
		String inputFilePath = line.getOptionValue(paramName);
        if (inputFilePath != null) {
            inpuFile = new File(inputFilePath);
            if (        !inpuFile.exists()
                    ||  !inpuFile.canRead()
                    ||  !inpuFile.isFile()) {
                throw new IllegalArgumentException("Unreadable file: " + inputFilePath);
            }
        }
		return inpuFile;
	}
    
	protected File openOutputFile(CommandLine line, String paramName) {
		if (!line.hasOption(paramName))
			return null;
		File outpuFile = new File(line.getOptionValue(paramName));
		if (outpuFile != null) {
			if (outpuFile.isDirectory()) {
				throw new IllegalArgumentException("Unwritable file: " + outpuFile + " is a directory!");
			}
		}
		return outpuFile;
	}

	protected File openOutputDir(CommandLine line, String paramName) {
		File inpuDir = null;
		if (!line.hasOption(paramName))
			return inpuDir;
		
		String inputDirPath = line.getOptionValue(paramName);
        if (inputDirPath != null) {
            inpuDir = new File(inputDirPath);
            if (        !inpuDir.exists()
                    ||  !inpuDir.canWrite()
                    ||  !inpuDir.isDirectory()) {
                throw new IllegalArgumentException("Unaccessible directory: " + inputDirPath);
            }
        }
		return inpuDir;
	}
   
    /**
     * Meant to be hidden by extending classes!
     */
    private static Options parseableOptions() {
		return new Options();
	}

	protected abstract void setup(CommandLine line);
                
    protected static String fromStringToEnumValue(String token) {
    	if (token != null)
    		return token.trim().toUpperCase().replace("-", "_");
    	return null;
	}
    
    protected static String[] tokenise(String paramString) {
    	return MessagePrinter.tokenise(paramString);
    }
    
    public static String printDefault(Object defaultValue) {
    	return ".\nDefault is: '" + defaultValue.toString() + "'"; 
    }

    protected static String attachInstabilityWarningToDescription(String description) {
    	return EXPERIMENTAL_DEVELOPMENT_STAGE_MESSAGE + "\n" + description;
    }
    
    public static String printValues(Object... values) {
    	return MessagePrinter.printValues(values);
    }
    
    public static String fromEnumValueToString(Object token) {
    	return MessagePrinter.fromEnumValueToString(token);
    }
    
    public static String fromEnumValuesToTokenJoinedString(Object... tokens) {
    	return MessagePrinter.fromEnumValuesToTokenJoinedString(tokens);
    }
}