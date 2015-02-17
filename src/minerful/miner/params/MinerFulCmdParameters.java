/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner.params;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class MinerFulCmdParameters extends ParamsManager {
	public enum CONSTRAINT_KINDS {
		EXISTENCE, RELATION,
		FORWARD, BACKWARD, MUTUAL,
		DIRECTIONED, UNDIRECTIONED,
		POSITIVE, NEGATIVE
	}
	
	public static final char AVOID_REDUNDANCY_PARAM = 'R';
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
	public static final String STATS_OUT_PATH_PARAM_NAME = "oSF";
	public static final String PROCESS_SCHEME_OUT_PATH_PARAM_NAME = "oPF";
	public static final String OUT_BRANCHING_LIMIT_PARAM_NAME = "b";
	public static final String FORESEE_DISTANCES_PARAM_NAME = "fD";
	public static final String SHOW_MEMSPACE_USED_PARAM_NAME = "sMS";
	public static final String EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME = "xF";

	public static final Integer MINIMUM_BRANCHING_LIMIT = 1;
            
    public Boolean avoidRedundancy = null;
    public Integer branchingLimit = MINIMUM_BRANCHING_LIMIT;
    public File inputFile = null;
    public File statsOutputFile = null;
    public File processSchemeOutputFile = null;
	public Boolean foreseeDistances = null;
	public Boolean memSpaceShowingRequested = null;
    public Collection<String> activitiesToExcludeFromResult = null;

    
    public MinerFulCmdParameters() {
		super();
		this.avoidRedundancy = false;
		this.foreseeDistances = false;
		this.memSpaceShowingRequested = false;
	}
    
    public MinerFulCmdParameters(Options options, String[] args) {
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public MinerFulCmdParameters(String[] args) {
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
		this.branchingLimit = Integer.valueOf(line.getOptionValue(
				OUT_BRANCHING_LIMIT_PARAM_NAME,
				MINIMUM_BRANCHING_LIMIT.toString()
			)
		);
		if (this.branchingLimit < MINIMUM_BRANCHING_LIMIT) {
			throw new IllegalArgumentException(
					"Invalid value for " + OUT_BRANCHING_LIMIT_PARAM_NAME +
					" (must be equal to or greater than " + (MINIMUM_BRANCHING_LIMIT) + ")");
		}
        this.avoidRedundancy = line.hasOption(AVOID_REDUNDANCY_PARAM);

        this.foreseeDistances = line.hasOption(FORESEE_DISTANCES_PARAM_NAME);
        this.memSpaceShowingRequested = line.hasOption(SHOW_MEMSPACE_USED_PARAM_NAME);
        
        String
        	inputFilePath = line.getOptionValue(INPUT_LOGFILE_PATH_PARAM_NAME),
        	outStatsFilePath = line.getOptionValue(STATS_OUT_PATH_PARAM_NAME),
        	procSchemeFilePath = line.getOptionValue(PROCESS_SCHEME_OUT_PATH_PARAM_NAME),
        	listOfExcludedOnesFromResultsFilePath = line.getOptionValue(EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME);
        if (inputFilePath != null) {
            this.inputFile = new File(inputFilePath);
            if (        !this.inputFile.exists()
                    ||  !this.inputFile.canRead()
                    ||  !this.inputFile.isFile()) {
                throw new IllegalArgumentException("Unreadable file: " + inputFilePath);
            }
        }
        if (outStatsFilePath != null) {
        	this.statsOutputFile = new File(outStatsFilePath);
        }
        if (procSchemeFilePath != null) {
        	this.processSchemeOutputFile = new File(procSchemeFilePath);
        }
        if (listOfExcludedOnesFromResultsFilePath != null) {
            File listOfExcludedOnesFromResultsFile = new File(listOfExcludedOnesFromResultsFilePath);
            if (        !listOfExcludedOnesFromResultsFile.exists()
                    ||  !listOfExcludedOnesFromResultsFile.canRead()
                    ||  !listOfExcludedOnesFromResultsFile.isFile()) {
                throw new IllegalArgumentException("Unreadable file: " + listOfExcludedOnesFromResultsFilePath);
            } else {
            	try {
					BufferedReader buRo = new BufferedReader(new FileReader(listOfExcludedOnesFromResultsFile));
					String excluActi = buRo.readLine();
					this.activitiesToExcludeFromResult = new ArrayList<String>();
					while (excluActi != null) {
						this.activitiesToExcludeFromResult.add(excluActi);
						excluActi = buRo.readLine();
					}
				} catch (IOException e) {
					throw new IllegalArgumentException("Unreadable file: " + listOfExcludedOnesFromResultsFilePath);
				}
            }
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
                .hasArg().withArgName("path")
                .withLongOpt("in-log")
                .withDescription("path to read the log file from")
                .withType(new String())
                .create(INPUT_LOGFILE_PATH_PARAM_NAME)
    	);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("stats-out")
        		.withDescription("path to write the statistics in")
        		.withType(new String())
        		.create(STATS_OUT_PATH_PARAM_NAME)
        		);
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("path")
                .withLongOpt("exclude-results-in")
                .withDescription("path of the file where the tasks to exclude from the result are listed")
                .withType(new String())
                .create(EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME)
    	);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("proc-out")
        		.withDescription("path to write the discovered process scheme in")
        		.withType(new String())
        		.create(PROCESS_SCHEME_OUT_PATH_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("number")
        		.withLongOpt("out-branch")
        		.withDescription("out-branching maximum level (must be greater than or equal to"
						+ (MINIMUM_BRANCHING_LIMIT)
						+ ", the default)")
        		.withType(new Integer(0))
        		.create(OUT_BRANCHING_LIMIT_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("noredundancy")
        		.withDescription("avoid redundancy")
        		.create(AVOID_REDUNDANCY_PARAM)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("foresee-distances")
        		.withDescription(
        				attachInstabilityWarningToDescription("compute the foreseen confidence interval for the expected distance between tasks in relation constraints")
        		)
        		.create(FORESEE_DISTANCES_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("show-mem")
        		.withDescription("show the memory consumption peak (could slower the overall computation)")
        		.create(SHOW_MEMSPACE_USED_PARAM_NAME)
        		);
        return options;
	}
}