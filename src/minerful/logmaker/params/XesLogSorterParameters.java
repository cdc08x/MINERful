package minerful.logmaker.params;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.params.ParamsManager;

public class XesLogSorterParameters extends ParamsManager {
	public static final String TRACES_SORTING_CRITERIA_PARAM_NAME = "trSort";
	public static final String INPUT_XES_PARAM_NAME = "trXESin";
	public static final String OUTPUT_XES_PARAM_NAME = "trXESout";

    public static final SortingCriterion[] DEFAULT_TRACES_SORTING_CRITERIA =
    		new SortingCriterion[]{ SortingCriterion.FIRST_EVENT_ASC };
    
	/**
	 * The criteria according to which traces should be sorted in the event log.
	 * The order in which they are given impacts the respective priority.
	 */
	public SortingCriterion[] tracesSortingCriteria;	// mandatory assignment
    /**
     * File in which the generated XES ({@link http://www.xes-standard.org/openxes/start}) event log is going to be stored.
     */
    public File outputXesFile;	// mandatory assignment
    /**
     * File from which the original XES ({@link http://www.xes-standard.org/openxes/start}) is read.
     */
    public File inputXesFile;	// mandatory assignment
    
    public XesLogSorterParameters () {
    	this(DEFAULT_TRACES_SORTING_CRITERIA,null,null);
    }

	public XesLogSorterParameters(SortingCriterion[] tracesSortingCriteria, File outputXesFile, File inputXesFile) {
		super();
		this.tracesSortingCriteria = tracesSortingCriteria;
		this.outputXesFile = outputXesFile;
		this.inputXesFile = inputXesFile;
	}

    public XesLogSorterParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public XesLogSorterParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}
	
	@Override
	protected void setup(CommandLine line) {
        this.udpateSortingCriteria(line.getOptionValue(TRACES_SORTING_CRITERIA_PARAM_NAME));
        this.inputXesFile = openInputFile(line, INPUT_XES_PARAM_NAME);
        this.outputXesFile = openOutputFile(line, OUTPUT_XES_PARAM_NAME);
	}
	
	private void udpateSortingCriteria(String paramString) {
		String[] tokens = tokenise(paramString);
		if (tokens == null)
			return;

		ArrayList<SortingCriterion> listOfCriteria = new ArrayList<SortingCriterion>(tokens.length);
		SortingCriterion criterion = null;
		
		for (String token : tokens) {
			token = fromStringToEnumValue(token);
			try {
				criterion = SortingCriterion.valueOf(token);
			} catch (Exception e) {
				System.err.println("Invalid option for " + TRACES_SORTING_CRITERIA_PARAM_NAME + ": " + token + " is going to be ignored.");
			}
			listOfCriteria.add(criterion);
		}
		
		if (listOfCriteria.size() > 0) {
			this.tracesSortingCriteria = listOfCriteria.toArray(new SortingCriterion[0]);
		} else {
			System.err.println("No valid option for " + TRACES_SORTING_CRITERIA_PARAM_NAME + ". Using default value.");
		}
	}

	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
                Option.builder(TRACES_SORTING_CRITERIA_PARAM_NAME)
						.hasArg().argName("criteria")
						.longOpt("traces-sorting-criteria")
						.desc("The criteria according to which traces should be sorted in the event log.\n" +
						"The order in which they are given impacts the respective priority. It can be a " + ARRAY_TOKENISER_SEPARATOR + "-separated list of the following: " + printValues(SortingCriterion.values())
						+ printDefault(fromEnumValuesToTokenJoinedString(DEFAULT_TRACES_SORTING_CRITERIA)))
						.type(String.class)
						.build()
    	);
        options.addOption(
                Option.builder(OUTPUT_XES_PARAM_NAME)
						.hasArg().argName("file path")
						.longOpt("out-xes-log")
						.desc("path of the file in which the XES log should be written.")
						.type(String.class)
						.build()
    	);
        options.addOption(
                Option.builder(INPUT_XES_PARAM_NAME)
						.hasArg().argName("file path")
						.longOpt("in-xes-log")
						.desc("path of the file from which the XES log should be read.")
						.type(String.class)
						.build()
    	);
        
        return options;
    }
}