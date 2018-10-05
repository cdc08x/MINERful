/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class InputCmdParameters extends ParamsManager {
	public static final EventClassification DEFAULT_EVENT_CLASSIFICATION = EventClassification.name;
	public static final InputEncoding DEFAULT_INPUT_ENCODING = InputEncoding.xes;
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
    public static final String INPUT_ENCODING_PARAM_NAME = "iE";
	public static final String EVENT_CLASSIFICATION_PARAM_NAME = "eC";
	public static final String INPUT_LOGFILE_PATH_LONG_PARAM_NAME = "in-log";
    public static final String INPUT_ENC_PARAM_LONG_NAME = "in-enc";
	public static final String EVENT_CLASSIFICATION_LONG_PARAM_NAME = "evt-class";
	public static final String START_FROM_TRACE_PARAM_NAME = "tStart";
	public static final Integer FIRST_TRACE_NUM = 0;
	public static final String SUB_LOG_SIZE_PARAM_NAME = "subL";
	public static final Integer WHOLE_LOG_LENGTH = 0;

	public enum InputEncoding {
		xes, strings;
	}
	
	public enum EventClassification {
		name, logspec
	}

	/** Encoding language for the input event log (see enum {@link minerful.params.InputCmdParameters.InputEncoding InputEncoding}). Default is: {@link minerful.params.InputCmdParameters.InputEncoding#xes InputEncoding.xes}.*/
	public InputEncoding inputLanguage;
	/** Classification policy to relate events to event classes, that is the task names (see enum {@link minerful.params.InputCmdParameters.EventClassification EventClassification}). Default is: {@link minerful.params.InputCmdParameters.EventClassification#name EventClassification.name}.*/
	public EventClassification eventClassification;
	/** Input event log file. It must not be <code>null</code>. */
    public File inputLogFile;

	/** Number of the trace to start the analysis from */
	public Integer startFromTrace;
	/** Length of the sub-sequence of traces to analyse */
	public Integer subLogLength;

    public InputCmdParameters() {
    	super();
    	inputLanguage = DEFAULT_INPUT_ENCODING;
    	eventClassification = DEFAULT_EVENT_CLASSIFICATION;
		this.startFromTrace = FIRST_TRACE_NUM;
		this.subLogLength = WHOLE_LOG_LENGTH;
    	inputLogFile = null;
    }
    
    public InputCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public InputCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        String inputFilePath = line.getOptionValue(INPUT_LOGFILE_PATH_PARAM_NAME);
        if (inputFilePath != null) {
            this.inputLogFile = new File(inputFilePath);
            if (        !this.inputLogFile.exists()
                    ||  !this.inputLogFile.canRead()
                    ||  !this.inputLogFile.isFile()) {
                throw new IllegalArgumentException("Unreadable file: " + inputFilePath);
            }
        }
        this.inputLanguage = InputEncoding.valueOf(
                line.getOptionValue(
                    INPUT_ENCODING_PARAM_NAME,
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
                OptionBuilder
                .hasArg().withArgName("language")
                .withLongOpt(INPUT_ENC_PARAM_LONG_NAME)
                .withDescription("input encoding language " + printValues(InputEncoding.values()) 
                		+ printDefault(fromEnumValueToString(DEFAULT_INPUT_ENCODING)))
                .withType(new String())
                .create(INPUT_ENCODING_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("class")
                .withLongOpt(EVENT_CLASSIFICATION_LONG_PARAM_NAME)
                .withDescription("event classification (resp., by activity name, or according to the log-specified pattern) " + printValues(EventClassification.values())
                		+ printDefault(fromEnumValueToString(DEFAULT_EVENT_CLASSIFICATION)))
                .withType(new String())
                .create(EVENT_CLASSIFICATION_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .isRequired(true)
                .withLongOpt(INPUT_LOGFILE_PATH_LONG_PARAM_NAME)
                .withDescription("path to read the log file from")
                .withType(new String())
                .create(INPUT_LOGFILE_PATH_PARAM_NAME)
    	);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("number")
        		.withLongOpt("start-from-trace")
        		.withDescription("ordinal number of the trace from which the analysed sub-log should start "
						+ printDefault(FIRST_TRACE_NUM))
        		.withType(new Long(0))
        		.create(START_FROM_TRACE_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("length")
        		.withLongOpt("sub-log-size")
        		.withDescription("number of traces to be analysed in the sub-log. To have the entire log analysed, leave the default value. "
						+ printDefault(WHOLE_LOG_LENGTH))
        		.withType(new Long(0))
        		.create(SUB_LOG_SIZE_PARAM_NAME)
        		);
        return options;
	}
}