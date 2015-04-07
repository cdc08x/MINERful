package minerful.errorinjector.params;

import java.io.File;

import minerful.errorinjector.ErrorInjector;
import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


public class ErrorInjectorCmdParameters extends ParamsManager {
	private static final String ERROR_SPREADING_POLICY_PARAM_NAME = "eS";
	private static final String ERROR_TYPE_PARAM_NAME = "eT";
	private static final String ERROR_PERCENTAGE_PARAM_NAME = "eP";
	private static final String TARGET_CHAR_PARAM_NAME = "eC";
	public static final String OUTPUT_LOG_PATH_PARAM_NAME = "eLF";
	public static final int ERROR_INJECTION_PERCENTAGE_DEFAULT = 0;

	private ErrorInjector.SpreadingPolicy errorInjectionSpreadingPolicy;
	private ErrorInjector.ErrorType errorType;
	private int errorsInjectionPercentage;/*percentage of the errors to inject */
	private Character targetChar;
	public File logFile;

	public ErrorInjectorCmdParameters() {
		this.errorInjectionSpreadingPolicy = ErrorInjector.SpreadingPolicy.getDefault();
		this.errorType = ErrorInjector.ErrorType.getDefault();
		this.errorsInjectionPercentage = ERROR_INJECTION_PERCENTAGE_DEFAULT;
		this.targetChar = null;
		this.logFile = null;
	}
	
    public ErrorInjectorCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public ErrorInjectorCmdParameters(String[] args) {
		this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        this.errorInjectionSpreadingPolicy =
        		ErrorInjector.SpreadingPolicy.valueOf(
	        		line.getOptionValue(
	        				ErrorInjectorCmdParameters.ERROR_SPREADING_POLICY_PARAM_NAME,
	        				this.errorInjectionSpreadingPolicy.toString()
	        		)
        		);
        this.errorType =
        		ErrorInjector.ErrorType.valueOf(
        			line.getOptionValue(
        					ErrorInjectorCmdParameters.ERROR_TYPE_PARAM_NAME,
        					errorType.toString()
        			)
        		);
        this.errorsInjectionPercentage = Integer.valueOf(
        		line.getOptionValue(ErrorInjectorCmdParameters.ERROR_PERCENTAGE_PARAM_NAME,
        				String.valueOf(this.errorsInjectionPercentage)));
        if (line.hasOption(ErrorInjectorCmdParameters.TARGET_CHAR_PARAM_NAME)) {
        	this.targetChar = Character.valueOf(line.getOptionValue(ErrorInjectorCmdParameters.TARGET_CHAR_PARAM_NAME).charAt(0));
        }
        if (line.hasOption(ErrorInjectorCmdParameters.OUTPUT_LOG_PATH_PARAM_NAME)) {
        	this.logFile = new File(line.getOptionValue(ErrorInjectorCmdParameters.OUTPUT_LOG_PATH_PARAM_NAME));
        }
   }
    
	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("policy")
                .withLongOpt("err-spread-policy")
                .withDescription("policy for the distribution of the errors. Possible values are:\n" +
                		"'" + ErrorInjector.SpreadingPolicy.collection + "'\n for spreading the errors over the whole collection of strings [DEFAULT];\n" +
                		"'" + ErrorInjector.SpreadingPolicy.string + "'\n for injecting the errors in every string")
                .withType(new Integer(0))
                .create(ErrorInjectorCmdParameters.ERROR_SPREADING_POLICY_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("type")
                .withLongOpt("err-type")
                .withDescription("type of the errors to inject. Possible values are:\n" +
                		"'" + ErrorInjector.ErrorType.ins + "'\n suppression of the target character;\n" +
                		"'" + ErrorInjector.ErrorType.del + "'\n insertion of the target character;\n" +
                		"'" + ErrorInjector.ErrorType.insdel + "'\n mixed (suppressions or insertions, as decided by random) [DEFAULT]")
                .withType(new Integer(0))
                .create(ErrorInjectorCmdParameters.ERROR_TYPE_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("percent")
                .withLongOpt("err-percentage")
                .withDescription("percentage of the errors to inject (from 0 to 100) [DEFAULT: 0]")
                .withType(new Integer(0))
                .create(ErrorInjectorCmdParameters.ERROR_PERCENTAGE_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("char")
                .withLongOpt("err-target")
                .withDescription("target character")
                .withType(new Character('0'))
                .create(ErrorInjectorCmdParameters.TARGET_CHAR_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("file path")
                .withLongOpt("err-out-log")
                .withDescription("path to the file to write the error-injected log in")
                .withType(new String())
                .create(ErrorInjectorCmdParameters.OUTPUT_LOG_PATH_PARAM_NAME)
    	);
		return options;
	}

	public ErrorInjector.SpreadingPolicy getErrorInjectionSpreadingPolicy() {
        return this.errorInjectionSpreadingPolicy;
	}

	public ErrorInjector.ErrorType getErrorType() {
		return this.errorType;
	}

	public double getErrorsInjectionPercentage() {
		return (errorsInjectionPercentage);
	}

	public Character getTargetChar() {
		return targetChar;
	}
	
	public boolean isTargetCharDefined() {
		return (this.targetChar != null);
	}
}