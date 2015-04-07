/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class SystemCmdParameters extends ParamsManager {
    public enum DebugLevel {
        none, info, debug, trace, all;
    }

    public static final char DEBUG_PARAM_NAME = 'd';
	public static final char HELP_PARAM_NAME = 'h';

    public DebugLevel debugLevel;
	public Boolean help;

	public SystemCmdParameters() {
		debugLevel = DebugLevel.info;
		help = false;
	}
	
    public SystemCmdParameters(Options options, String[] args) {
        this();
    	// parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public SystemCmdParameters(String[] args) {
        this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        this.help =
        		line.hasOption(SystemCmdParameters.HELP_PARAM_NAME);
        this.debugLevel = DebugLevel.valueOf(
                line.getOptionValue(
                    DEBUG_PARAM_NAME,
                    this.debugLevel.toString()
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
                .withLongOpt("help")
                .withDescription("print help")
                .create(SystemCmdParameters.HELP_PARAM_NAME)
        );
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("debug_level")
                .withLongOpt("debug")
                .withDescription("debug level " + printValues(DebugLevel.values()))
                .withType(new Integer(0))
                .create(SystemCmdParameters.DEBUG_PARAM_NAME)
        );
        return options;
	}
}