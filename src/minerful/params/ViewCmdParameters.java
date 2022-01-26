package minerful.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class ViewCmdParameters extends ParamsManager {
	public static enum ConstraintsSorting {
		type, support, interest;
	}

	public static final String MACHINE_READABLE_RESULTS_PARAM_NAME = "vMachine";
	public static final String CONSTRAINTS_SORTING_TYPE_PARAM_NAME = "vSort";
	public static final String CONSTRAINTS_NO_FOLDING_PARAM_NAME = "vNoFold";
	public static final String SUPPRESS_SCREEN_PRINT_OUT_PARAM_NAME = "vShush";
	
	public static final Boolean DEFAULT_DO_MACHINE_READABLE_RESULTS = false;
	public static final ConstraintsSorting DEFAULT_CONSTRAINTS_SORTING_TYPE = ConstraintsSorting.type;
	public static final Boolean DEFAULT_DO_CONSTRAINTS_NO_FOLDING = false;
	public static final Boolean DEFAULT_DO_SUPPRESS_SCREEN_PRINT_OUT = false;

	/** Set this field to <code>true</code> to print a machine-readable list of supports, for each constraint template and constrained activities. */
    public Boolean machineReadableResults;
    /** How to sort constraints in the print-out of results (see enum {@link minerful.params.ConstraintsSorting ConstraintsSorting}). Default is: {@link minerful.params.ConstraintsSorting#property ConstraintsSorting.type}. */
    public ConstraintsSorting constraintsSorting;
	/** Set this field to <code>true</code> to avoid the discovered constraints to be folded under activation tasks in the print-out. */
    public Boolean noFoldingRequired;
	/** Set this field to <code>true</code> to avoid the discovered constraints to be printed out on screen. */
	public Boolean suppressScreenPrintOut;

	/**
	 * 
	 */
	public ViewCmdParameters() {
		super();
		machineReadableResults = DEFAULT_DO_MACHINE_READABLE_RESULTS;
	    constraintsSorting = DEFAULT_CONSTRAINTS_SORTING_TYPE;
	    noFoldingRequired = DEFAULT_DO_CONSTRAINTS_NO_FOLDING;
	    suppressScreenPrintOut = DEFAULT_DO_SUPPRESS_SCREEN_PRINT_OUT;
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
        						CONSTRAINTS_SORTING_TYPE_PARAM_NAME,
        						this.constraintsSorting.toString()
        						)
        				);
        this.machineReadableResults = line.hasOption(MACHINE_READABLE_RESULTS_PARAM_NAME);
        this.noFoldingRequired = line.hasOption(CONSTRAINTS_NO_FOLDING_PARAM_NAME);
        this.suppressScreenPrintOut = line.hasOption(SUPPRESS_SCREEN_PRINT_OUT_PARAM_NAME);
	}
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
        		Option.builder(CONSTRAINTS_SORTING_TYPE_PARAM_NAME)
						.hasArg().argName("type")
						.longOpt("sort-constraints")
						.desc("Sorting policy for constraints of the discovered process: " + printValues(ConstraintsSorting.values()) +
						printDefault(DEFAULT_CONSTRAINTS_SORTING_TYPE))
						.type(String.class)
						.build()
        		);
        options.addOption(
        		Option.builder(CONSTRAINTS_NO_FOLDING_PARAM_NAME)
						.longOpt("no-folding")
						.desc("avoid the discovered constraints to be folded under activation tasks" +
						printDefault(DEFAULT_DO_CONSTRAINTS_NO_FOLDING))
						.build()
        		);
        options.addOption(
        		Option.builder(MACHINE_READABLE_RESULTS_PARAM_NAME)
						.longOpt("machine-readable")
						.desc("print a machine-readable list of supports, for each constraint template and constrained activities in the print-out" +
						printDefault(DEFAULT_DO_MACHINE_READABLE_RESULTS))
						.build()
        		);
        options.addOption(
        		Option.builder(SUPPRESS_SCREEN_PRINT_OUT_PARAM_NAME)
						.longOpt("no-screen-print-out")
						.desc("suppresses the print-out of constraints on screen" +
						printDefault(DEFAULT_DO_SUPPRESS_SCREEN_PRINT_OUT))
						.build()
        		);
       return options;
	}
}
