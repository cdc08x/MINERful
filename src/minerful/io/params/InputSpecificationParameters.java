/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.io.params;

import java.io.File;

import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class InputSpecificationParameters extends ParamsManager {
	public static final String INPUT_SPECIFICATIONFILE_PATH_PARAM_NAME = "iSF";
	public static final String INPUT_SPECIFICATIONFILE_PATH_PARAM_LONG_NAME = "input-specification-file";
    public static final String INPUT_SPECIFICATION_ENC_PARAM_NAME = "iSE";
    public static final String INPUT_SPECIFICATION_ENC_PARAM_LONG_NAME = "input-specification-encoding";
	
    public static final InputEncoding DEFAULT_INPUT_SPECIFICATION_ENC = InputEncoding.JSON;

    /**
     * Possible file encodings for marshalled process models.
     * @author Claudio Di Ciccio
     */
	public enum InputEncoding {
		DECLARE_MAP,	
		JSON // default
	}

	/**
	 * Input language encoding for the input process model (see {@link InputEncoding InputEncoding}. Default value is {@link #DEFAULT_INPUT_SPECIFICATION_ENC DEFAULT_INPUT_MODEL_ENC}
	 */
	public InputEncoding inputLanguage;
	/**
	 * File in which the process model is stored.
	 */
    public File inputFile;

    public InputSpecificationParameters() {
    	super();
    	inputLanguage = InputSpecificationParameters.DEFAULT_INPUT_SPECIFICATION_ENC;
    	inputFile = null;
    }

    public InputSpecificationParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public InputSpecificationParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        this.inputFile = openInputFile(line, INPUT_SPECIFICATIONFILE_PATH_PARAM_NAME);
        this.inputLanguage = InputEncoding.valueOf(
                fromStringToEnumValue(line.getOptionValue(INPUT_SPECIFICATION_ENC_PARAM_NAME, this.inputLanguage.toString())
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
				Option.builder(INPUT_SPECIFICATION_ENC_PARAM_NAME)
                .hasArg().argName("language")
                .longOpt(INPUT_SPECIFICATION_ENC_PARAM_LONG_NAME)
                .desc("input specification encoding language " + printValues(InputEncoding.values()) + " (default: " + printValues(DEFAULT_INPUT_SPECIFICATION_ENC) + ")")
                .type(String.class)
                .build()
//						.create(INPUT_SPECIFICATION_ENC_PARAM_NAME)
        );
        options.addOption(
				Option.builder(INPUT_SPECIFICATIONFILE_PATH_PARAM_NAME)
                .hasArg().argName("path")
                .longOpt(INPUT_SPECIFICATIONFILE_PATH_PARAM_LONG_NAME)
                .desc("path of the file from which the process specification should be read")
                .type(String.class)
                .build()
//						 .create(INPUT_SPECIFICATIONFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}