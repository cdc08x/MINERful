/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SystemCmdParameters extends ParamsManager {
    private static final DebugLevel DEFAULT_DEBUG_LEVEL = DebugLevel.info;
	public enum DebugLevel {
        none, info, debug, trace, all;
    }

    public static final char DEBUG_PARAM_NAME = 'd';
	public static final char HELP_PARAM_NAME = 'h';

	/** Desired level of debugging (see enum {@link minerful.params.SystemCmdParameters.DebugLevel DebugLevel}) */
    public DebugLevel debugLevel;
	/** Set this variable to <code>true</code> to print out a help screen */
	public Boolean help;

	public SystemCmdParameters() {
		super();
		debugLevel = DEFAULT_DEBUG_LEVEL;
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
				Option.builder(String.valueOf(SystemCmdParameters.HELP_PARAM_NAME))
						.longOpt("help")
						.desc("print help")
        		.build()
		);
        options.addOption(
				Option.builder(String.valueOf(SystemCmdParameters.DEBUG_PARAM_NAME))
						.hasArg().argName("debug_level")
						.longOpt("debug")
						.desc("debug level " + printValues(DebugLevel.values())
								+ printDefault(fromEnumValueToString(DEFAULT_DEBUG_LEVEL))
				)
						.type(Integer.class)
						.build()
        );
        return options;
	}
}