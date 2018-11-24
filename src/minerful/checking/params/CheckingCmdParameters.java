package minerful.checking.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import minerful.params.ParamsManager;


public class CheckingCmdParameters extends ParamsManager {
	/**
	 * Defines how constraints are meant to be considered as satisfied:
	 * either including vacuous satisfactions ({@link #LOOSE LOOSE})
	 * or not (({@link #STRICT STRICT})).
	 * The default is {@link #DEFAULT_STRICTNESS_POLICY DEFAULT_STRICTNESS_POLICY}
	 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
	 */
	public static enum StrictnessPolicy {
		LOOSE,
		STRICT
	}

	public static final String STRICTNESS_POLICY_PARAM_NAME = "chkS";
	public static final String SAVE_AS_CSV_PARAM_NAME = "chkOut";
	
	public static final StrictnessPolicy DEFAULT_STRICTNESS_POLICY = StrictnessPolicy.LOOSE;

	/** Policy according to which constraints are considered as satisfied or not (see {@link StrictnessPolicy StrictnessPolicy}. The default value is {@link #DEFAULT_STRICTNESS_POLICY DEFAULT_STRICTNESS_POLICY}. */
	public StrictnessPolicy strictnessPolicy;
	/** File in which the checking output is printed in a CSV format. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveResultsAsCSV;
	
	public CheckingCmdParameters() {
		super();
		this.strictnessPolicy = DEFAULT_STRICTNESS_POLICY;
		this.fileToSaveResultsAsCSV = null;
	}
    
    public CheckingCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public CheckingCmdParameters(String[] args) {
		this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
		this.strictnessPolicy = StrictnessPolicy.valueOf(
				line.getOptionValue(
						STRICTNESS_POLICY_PARAM_NAME,
						this.strictnessPolicy.toString()
						)
				);
        String fileToSaveResultsAsCsvString = line.getOptionValue(SAVE_AS_CSV_PARAM_NAME);
        if (fileToSaveResultsAsCsvString != null) {
            this.fileToSaveResultsAsCSV = new File(fileToSaveResultsAsCsvString);
        }
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
                .hasArg().withArgName("type")
                .withLongOpt("checking-strictness")
                .withDescription("level of strictness of the checking analysis over constraints. It can be one of the following: " + printValues(StrictnessPolicy.values())
                		+ printDefault(fromEnumValueToString(DEFAULT_STRICTNESS_POLICY)))
                .withType(new String())
                .create(STRICTNESS_POLICY_PARAM_NAME)
    	);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-check-as-csv")
        		.withDescription("print results in CSV format into the specified file")
        		.withType(new String())
        		.create(SAVE_AS_CSV_PARAM_NAME)
        		);
        return options;
	}
}