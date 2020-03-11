/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class InputLogCmdParameters extends ParamsManager {
	public static final EventClassification DEFAULT_EVENT_CLASSIFICATION = EventClassification.name;
	public static final InputEncoding DEFAULT_INPUT_ENCODING = InputEncoding.xes;
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
    public static final String INPUT_LOG_ENCODING_PARAM_NAME = "iLE";
	public static final String EVENT_CLASSIFICATION_PARAM_NAME = "iLClassif";
	public static final String INPUT_LOGFILE_PATH_LONG_PARAM_NAME = "in-log-file";
    public static final String INPUT_ENC_PARAM_LONG_NAME = "in-log-encoding";
	public static final String EVENT_CLASSIFICATION_LONG_PARAM_NAME = "in-log-evt-classifier";
	public static final String START_FROM_TRACE_PARAM_NAME = "iLStartAt";
	public static final Integer FIRST_TRACE_NUM = 0;
	public static final String SUB_LOG_SIZE_PARAM_NAME = "iLSubLen";
	public static final Integer WHOLE_LOG_LENGTH = 0;

	public enum InputEncoding {
		/**
		 * For XES logs (also compressed)
		 */
		xes,
		/**
		 * For MXML logs (also compressed)
		 */
		mxml,
		/**
		 * For string-encoded traces, where each character is assumed to be a task symbol
		 */
		strings;
	}
	
	public enum EventClassification {
		name, logspec
	}

	/** Encoding language for the input event log (see enum {@link minerful.params.InputLogCmdParameters.InputEncoding InputEncoding}). Default is: {@link minerful.params.InputLogCmdParameters.InputEncoding#xes InputEncoding.xes}.*/
	public InputEncoding inputLanguage;
	/** Classification policy to relate events to event classes, that is the task names (see enum {@link minerful.params.InputLogCmdParameters.EventClassification EventClassification}). Default is: {@link minerful.params.InputLogCmdParameters.EventClassification#name EventClassification.name}.*/
	public EventClassification eventClassification;
	/** Input event log file. It must not be <code>null</code>. */
    public File inputLogFile;

	/** Number of the trace to start the analysis from */
	public Integer startFromTrace;
	/** Length of the sub-sequence of traces to analyse */
	public Integer subLogLength;

    public InputLogCmdParameters() {
    	super();
    	inputLanguage = DEFAULT_INPUT_ENCODING;
    	eventClassification = DEFAULT_EVENT_CLASSIFICATION;
		this.startFromTrace = FIRST_TRACE_NUM;
		this.subLogLength = WHOLE_LOG_LENGTH;
    	inputLogFile = null;
    }
    
    public InputLogCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public InputLogCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
		this.inputLogFile = openInputFile(line, INPUT_LOGFILE_PATH_PARAM_NAME);

        this.inputLanguage = InputEncoding.valueOf(
                line.getOptionValue(
                    INPUT_LOG_ENCODING_PARAM_NAME,
                    this.inputLanguage.toString()
                )
            );
        this.eventClassification = EventClassification.valueOf(
                line.getOptionValue(
                	EVENT_CLASSIFICATION_PARAM_NAME,
                    this.eventClassification.toString()
                )
            );
        this.startFromTrace = Integer.valueOf(
                line.getOptionValue(
                    START_FROM_TRACE_PARAM_NAME,
                    this.startFromTrace.toString()
                )
            );
        this.subLogLength = Integer.valueOf(
                line.getOptionValue(
                    SUB_LOG_SIZE_PARAM_NAME,
                    this.subLogLength.toString()
                )
            );
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
                Option.builder(INPUT_LOG_ENCODING_PARAM_NAME)
						.hasArg().argName("language")
						.longOpt(INPUT_ENC_PARAM_LONG_NAME)
						.desc("input encoding language " + printValues(InputEncoding.values())
						+ printDefault(fromEnumValueToString(DEFAULT_INPUT_ENCODING)))
						.type(String.class)
				.build()
        );
        options.addOption(
                Option.builder(EVENT_CLASSIFICATION_PARAM_NAME)
						.hasArg().argName("class")
						.longOpt(EVENT_CLASSIFICATION_LONG_PARAM_NAME)
						.desc("event classification (resp., by activity name, or according to the log-specified pattern) " + printValues(EventClassification.values())
						+ printDefault(fromEnumValueToString(DEFAULT_EVENT_CLASSIFICATION)))
						.type(String.class)
				.build()
        );
        options.addOption(
                Option.builder(INPUT_LOGFILE_PATH_PARAM_NAME)
						.hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
						.longOpt(INPUT_LOGFILE_PATH_LONG_PARAM_NAME)
						.desc("path to read the log file from")
						.type(String.class)
				.build()
    	);
        options.addOption(
        		Option.builder(START_FROM_TRACE_PARAM_NAME)
						.hasArg().argName("number")
						.longOpt("start-from-trace")
						.desc("ordinal number of the trace from which the analysed sub-log should start"
						+ printDefault(FIRST_TRACE_NUM))
						.type(Long.class)
				.build()
        		);
        options.addOption(
        		Option.builder(SUB_LOG_SIZE_PARAM_NAME)
						.hasArg().argName("length")
						.longOpt("sub-log-size")
						.desc("number of traces to be analysed in the sub-log. To have the entire log analysed, leave the default value"
						+ printDefault(WHOLE_LOG_LENGTH))
						.type(Long.class)
				.build()
				);
        return options;
	}
}