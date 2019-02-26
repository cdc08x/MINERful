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
	
    public static final InputEncoding DEFAULT_INPUT_MODEL_ENC = InputEncoding.MINERFUL;

    /**
     * Possible file encodings for marshalled process models.
     * @author Claudio Di Ciccio
     */
	public enum InputEncoding {
		DECLARE_MAP,
		MINERFUL,	// default
		JSON
	}

	/**
	 * Input language encoding for the input process model (see {@link InputEncoding InputEncoding}. Default value is {@link #DEFAULT_INPUT_MODEL_ENC DEFAULT_INPUT_MODEL_ENC}
	 */
	public InputEncoding inputLanguage;
	/**
	 * File in which the process model is stored.
	 */
    public File inputFile;

    public InputModelParameters() {
    	super();
    	inputLanguage = InputModelParameters.DEFAULT_INPUT_MODEL_ENC;
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
        this.inputFile = openInputFile(line, INPUT_MODELFILE_PATH_PARAM_NAME);
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
                .withDescription("input model encoding language " + printValues(InputEncoding.values()) + " (default: " + printValues(DEFAULT_INPUT_MODEL_ENC) + ")")
                .withType(new String())
                .create(INPUT_MODEL_ENC_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .withLongOpt(INPUT_MODELFILE_PATH_PARAM_LONG_NAME)
                .withDescription("path of the file from which the process model should be read")
                .withType(new String())
                .create(INPUT_MODELFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}