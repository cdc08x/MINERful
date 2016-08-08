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
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
    public static final String INPUT_ENC_PARAM_NAME = "iE";
	public static final String EVENT_CLASSIFICATION_PARAM_NAME = "eC";
	public static final String INPUT_LOGFILE_PATH_LONG_PARAM_NAME = "in-log";
    public static final String INPUT_ENC_PARAM_LONG_NAME = "in-enc";
	public static final String EVENT_CLASSIFICATION_LONG_PARAM_NAME = "evt-class";

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

    public InputCmdParameters() {
    	super();
    	inputLanguage = InputEncoding.xes;
    	eventClassification = EventClassification.name;
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
                    INPUT_ENC_PARAM_NAME,
                    this.inputLanguage.toString()
                )
            );
        this.eventClassification = EventClassification.valueOf(
                line.getOptionValue(
                	EVENT_CLASSIFICATION_PARAM_NAME,
                    this.eventClassification.toString()
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
                .withDescription("input encoding language " + printValues(InputEncoding.values()))
                .withType(new String())
                .create(INPUT_ENC_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("class")
                .withLongOpt(EVENT_CLASSIFICATION_LONG_PARAM_NAME)
                .withDescription("event classification (resp., by activity name, or according to the log-specified pattern) " + printValues(EventClassification.values()))
                .withType(new String())
                .create(EVENT_CLASSIFICATION_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .withLongOpt(INPUT_LOGFILE_PATH_LONG_PARAM_NAME)
                .withDescription("path to read the log file from")
                .withType(new String())
                .create(INPUT_LOGFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}