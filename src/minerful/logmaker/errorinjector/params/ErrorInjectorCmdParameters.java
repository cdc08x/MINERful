package minerful.logmaker.errorinjector.params;

import java.io.File;

import minerful.logmaker.errorinjector.ErrorInjector;
import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
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
		super();
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
                Option.builder(ErrorInjectorCmdParameters.ERROR_SPREADING_POLICY_PARAM_NAME)
						.hasArg().argName("policy")
						.longOpt("err-spread-policy")
						.desc("policy for the distribution of the errors. Possible values are:\n" +
						"'" + ErrorInjector.SpreadingPolicy.collection + "'\n to spread the errors over the whole collection of traces [DEFAULT];\n" +
						"'" + ErrorInjector.SpreadingPolicy.string + "'\n to inject the errors in every trace")
						.type(Integer.class)
						.build()
        );
        options.addOption(
                Option.builder(ErrorInjectorCmdParameters.ERROR_TYPE_PARAM_NAME)
						.hasArg().argName("type")
						.longOpt("err-type")
						.desc("type of the errors to inject. Possible values are:\n" +
						"'" + ErrorInjector.ErrorType.ins + "'\n suppression of the target task;\n" +
						"'" + ErrorInjector.ErrorType.del + "'\n insertion of the target task;\n" +
						"'" + ErrorInjector.ErrorType.insdel + "'\n mixed (suppressions or insertions, as decided by random) [DEFAULT]")
						.type(Integer.class)
						.build()
        );
        options.addOption(
                Option.builder(ErrorInjectorCmdParameters.ERROR_PERCENTAGE_PARAM_NAME)
						.hasArg().argName("percent")
						.longOpt("err-percentage")
						.desc("percentage of the errors to be injected (from 0 to 100) [DEFAULT: 0]")
						.type(Integer.class)
						.build()
        );
        options.addOption(
                Option.builder(ErrorInjectorCmdParameters.TARGET_CHAR_PARAM_NAME)
						.hasArg().argName("char")
						.longOpt("err-target")
						.desc("target task")
						.type(Character.class)
						.build()
        );
        options.addOption(
                Option.builder(ErrorInjectorCmdParameters.OUTPUT_LOG_PATH_PARAM_NAME)
						.hasArg().argName("file path")
						.longOpt("err-out-log")
						.desc("path to the file in which the error-injected log is stored")
						.type(String.class)
						.build()
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