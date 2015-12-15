package minerful.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ViewCmdParameters extends ParamsManager {
	public static enum ConstraintsSorting {
		type, support, interest;
	}

	public static final String MACHINE_READABLE_RESULTS_PARAM_NAME = "mR";
	public static final String CONSTRAINTS_SORTING_TYPE_NAME = "cS";
	public static final String CONSTRAINTS_NO_FOLDING_NAME = "noCF";

    public Boolean machineReadableResults;
    public ConstraintsSorting constraintsSorting;
    public Boolean noFoldingRequired;


	/**
	 * 
	 */
	public ViewCmdParameters() {
		super();
		machineReadableResults = false;
	    constraintsSorting = ConstraintsSorting.type;
	    noFoldingRequired = false;
	}


    public ViewCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

    public ViewCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        this.constraintsSorting =
        		Enum.valueOf(ConstraintsSorting.class,
        				line.getOptionValue(
        						CONSTRAINTS_SORTING_TYPE_NAME,
        						this.constraintsSorting.toString()
        						)
        				);
        this.machineReadableResults = line.hasOption(MACHINE_READABLE_RESULTS_PARAM_NAME);
        this.noFoldingRequired = line.hasOption(CONSTRAINTS_NO_FOLDING_NAME);
	}
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("type")
        		.withLongOpt("sort-constraints")
        		.withDescription("Sorting policy for constraints of the discovered process: " + printValues(ConstraintsSorting.values()))
        		.withType(new String())
        		.create(CONSTRAINTS_SORTING_TYPE_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("nofolding")
        		.withDescription("avoid the discovered constraints to be folded within implying activities")
        		.create(CONSTRAINTS_NO_FOLDING_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("machine-readable")
        		.withDescription("print a machine readable list of supports, for each constraint")
        		.create(MACHINE_READABLE_RESULTS_PARAM_NAME)
        		);
       return options;
	}
}
