package minerful.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class SlidingMiningCmdParameters extends SlidingCmdParameters {
	public static final String INTERMEDIATE_OUTPUT_PARAM_NAME = "sliOut";

	/** The file where to store as a CSV file the constraints' support while MINERful slides over the traces. */
    public File intermediateOutputCsvFile;
    
	public SlidingMiningCmdParameters() {
		super();
		intermediateOutputCsvFile = null;
	}

    public SlidingMiningCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

    public SlidingMiningCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        super.setup(line);
        
        this.intermediateOutputCsvFile = openOutputFile(line, INTERMEDIATE_OUTPUT_PARAM_NAME);
	}
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = SlidingCmdParameters.parseableOptions();
        options.addOption(
        		Option.builder(INTERMEDIATE_OUTPUT_PARAM_NAME)
						.hasArg().argName("file")
						.required(true)
						.longOpt("sliding-results-out")
						.desc("path of the file in which the values of constraints' measures are written")
						.build()
        		);
        return options;
	}
}
