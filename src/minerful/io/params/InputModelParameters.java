/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.io.params;

import java.io.File;

import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class InputModelParameters extends ParamsManager {
	public static final String INPUT_MODELFILE_PATH_PARAM_NAME = "iMF";
	public static final String INPUT_MODELFILE_PATH_PARAM_LONG_NAME = "input-model";
    public static final String INPUT_MODEL_ENC_PARAM_NAME = "iME";
    public static final String INPUT_MODEL_ENC_PARAM_LONG_NAME = "input-model-encoding";

	public enum InputEncoding {
		DECLARE_MAP,
		MINERFUL	// default
	}

	public InputEncoding inputLanguage;
    public File inputFile;

    public InputModelParameters() {
    	super();
    	inputLanguage = InputEncoding.MINERFUL;
    	inputFile = null;
    }

    public InputModelParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public InputModelParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        String inputFilePath = line.getOptionValue(INPUT_MODELFILE_PATH_PARAM_NAME);
        if (inputFilePath != null) {
            this.inputFile = new File(inputFilePath);
            if (        !this.inputFile.exists()
                    ||  !this.inputFile.canRead()
                    ||  !this.inputFile.isFile()) {
                throw new IllegalArgumentException("Unreadable file: " + inputFilePath);
            }
        }
        this.inputLanguage = InputEncoding.valueOf(
                fromStringToEnumValue(line.getOptionValue(INPUT_MODEL_ENC_PARAM_NAME, this.inputLanguage.toString())
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
                .withLongOpt(INPUT_MODEL_ENC_PARAM_LONG_NAME)
                .withDescription("input model encoding language " + printValues(InputEncoding.values()))
                .withType(new String())
                .create(INPUT_MODEL_ENC_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .withLongOpt(INPUT_MODELFILE_PATH_PARAM_LONG_NAME)
                .withDescription("path to read the process model file from")
                .withType(new String())
                .create(INPUT_MODELFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}