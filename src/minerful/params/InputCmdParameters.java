/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.params;

import java.io.File;

import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class InputCmdParameters extends ParamsManager {
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
    public static final String INPUT_ENC_PARAM_NAME = "iE";

	public enum InputEncoding {
		xes, strings;
	}

	public InputEncoding inputLanguage = InputEncoding.xes;
            
    public File inputFile = null;

    public InputCmdParameters(Options options, String[] args) {
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public InputCmdParameters(String[] args) {
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
                .hasArg().withArgName("path")
                .withLongOpt("in-log")
                .withDescription("path to read the log file from")
                .withType(new String())
                .create(INPUT_LOGFILE_PATH_PARAM_NAME)
    	);
        return options;
	}
}