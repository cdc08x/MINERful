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

	public enum InputEncoding {
		xes, strings;
	}
	
	public enum EventClassification {
		name, logspec
	}

	public InputEncoding inputLanguage;
	public EventClassification eventClassification;
    public File inputFile;

    public InputCmdParameters() {
    	inputLanguage = InputEncoding.xes;
    	eventClassification = EventClassification.name;
    	inputFile = null;
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
            this.inputFile = new File(inputFilePath);
            if (        !this.inputFile.exists()
                    ||  !this.inputFile.canRead()
                    ||  !this.inputFile.isFile()) {
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
                .withLongOpt("in-enc")
                .withDescription("input encoding language " + printValues(InputEncoding.values()))
                .withType(new String())
                .create(INPUT_ENC_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("class")
                .withLongOpt("evt-class")
                .withDescription("event classification (resp., by activity name, or according to the log-specified pattern) " + printValues(EventClassification.values()))
                .withType(new String())
                .create(EVENT_CLASSIFICATION_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .withLongOpt("in-log")
                .withDescription("path to read the log file from")
                .withType(new String())
                .create(INPUT_LOGFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}