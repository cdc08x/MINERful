package minerful.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.logmaker.params.LogMakerParameters;

public class SlidingLogXtractorCmdParameters extends SlidingCmdParameters {
	public static final String OUTPUT_LOGFILES_DIR_PARAM_NAME = "sSLoXOutDir";
    public static final String OUT_ENC_PARAM_NAME = "sSLoXLE";

    /** The directory wherein the output sub-event-logs should be stored. */
    public File outDir;
	/** Event log encoding (see {@link Encoding #Encoding}). */
    public LogMakerParameters.Encoding outputEncoding = LogMakerParameters.DEFAULT_OUTPUT_ENCODING;
    
	public SlidingLogXtractorCmdParameters() {
		super();
		outDir = null;
	}

    public SlidingLogXtractorCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

    public SlidingLogXtractorCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        super.setup(line);
        
        this.outDir = openOutputDir(line, OUTPUT_LOGFILES_DIR_PARAM_NAME);

        this.outputEncoding = LogMakerParameters.Encoding.valueOf(
        		line.getOptionValue(OUT_ENC_PARAM_NAME, this.outputEncoding.toString())
		);	}
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = SlidingCmdParameters.parseableOptions();
        options.addOption(
        		Option.builder(OUTPUT_LOGFILES_DIR_PARAM_NAME)
						.hasArg().argName("directory")
						.required(true)
						.longOpt("sliding-sublogs-extract-out-dir")
						.desc("path of the directory in which the output sub-event-logs should be stored")
						.build()
        		);
		options.addOption(
                Option.builder(OUT_ENC_PARAM_NAME)
						.hasArg().argName("language")
						.longOpt("sliding-sublogs-extract-out-log-encoding")
						.desc("encoding language for the output sub-logs " + printValues(LogMakerParameters.Encoding.values())
						+ printDefault(fromEnumValueToString(LogMakerParameters.DEFAULT_OUTPUT_ENCODING)))
						.type(String.class)
						.build()
    	);
        return options;
	}
}