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
	
	public static final String INPUT_LOGFILE_PATH_PARAM_NAME = "iLF";
	public static final String STATS_OUT_PATH_PARAM_NAME = "oSF";
	public static final String OUT_BRANCHING_LIMIT_PARAM_NAME = "b";
	public static final String FORESEE_DISTANCES_PARAM_NAME = "fD";
	public static final String SHOW_MEMSPACE_USED_PARAM_NAME = "sMS";
	public static final String EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME = "xF";
	public static final char KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME = 'p';
	public static final String QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME = "pQ";
//	public static final String TIME_ANALYSIS_PARAM_NAME = "time";

	public static final Integer MINIMUM_BRANCHING_LIMIT = 1;
	public static final Integer MINIMUM_PARALLEL_EXECUTION_THREADS = 1;

    /** Out-branching maximum level for discovered constraints (must be greater than or equal to {@link #MINIMUM_BRANCHING_LIMIT MINIMUM_BRANCHING_LIMIT}, the default) */ 
	public Integer branchingLimit;
    /** Output file where log statistics are printed out */ 
    public File statsOutputFile;
    /** Ignore this */
	public Boolean foreseeDistances;
    /** Ignore this */
	public Boolean memSpaceShowingRequested;
    /** Collection of task names to exclude from the discovery */
    public Collection<String> activitiesToExcludeFromResult;
    /** Number of parallel threads to use while running the knowledge-base discovery phase of the algorithm (must be greater than or equal to {@link #MINIMUM_PARALLEL_EXECUTION_THREADS MINIMUM_PARALLEL_EXECUTION_THREADS}, the default) */ 
	public Integer kbParallelProcessingThreads;
    /** Number of parallel threads to use while running the knowledge-base discovery phase of the algorithm (must be greater than or equal to {@link #MINIMUM_PARALLEL_EXECUTION_THREADS MINIMUM_PARALLEL_EXECUTION_THREADS}, the default) */ 
	public Integer queryParallelProcessingThreads;

    
    public MinerFulCmdParameters() {
		super();
		this.branchingLimit = MINIMUM_BRANCHING_LIMIT;
		this.foreseeDistances = false;
		this.memSpaceShowingRequested = false;
		this.kbParallelProcessingThreads = MINIMUM_PARALLEL_EXECUTION_THREADS;
		this.queryParallelProcessingThreads = MINIMUM_PARALLEL_EXECUTION_THREADS;
//		this.takeTime = false;
	}
    
    public MinerFulCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public MinerFulCmdParameters(String[] args) {
		this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
		this.branchingLimit = Integer.valueOf(line.getOptionValue(
				OUT_BRANCHING_LIMIT_PARAM_NAME,
				this.branchingLimit.toString()
			)
		);
		if (this.branchingLimit < MINIMUM_BRANCHING_LIMIT) {
			throw new IllegalArgumentException(
					"Invalid value for " + OUT_BRANCHING_LIMIT_PARAM_NAME + " option" +
					" (must be equal to or greater than " + (MINIMUM_BRANCHING_LIMIT) + ")");
		}
		this.kbParallelProcessingThreads = Integer.valueOf(line.getOptionValue(
				KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME,
				kbParallelProcessingThreads.toString()
			)
		);
		this.queryParallelProcessingThreads = Integer.valueOf(line.getOptionValue(
				QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME,
				queryParallelProcessingThreads.toString()
			)
		);
		if (this.kbParallelProcessingThreads < MINIMUM_PARALLEL_EXECUTION_THREADS) {
			throw new IllegalArgumentException(
					"Invalid value for " + KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME + " option" +
					" (must be equal to or greater than " + (MINIMUM_PARALLEL_EXECUTION_THREADS) + ")");
		}
		if (this.queryParallelProcessingThreads < MINIMUM_PARALLEL_EXECUTION_THREADS) {
			throw new IllegalArgumentException(
					"Invalid value for " + QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME + " option" +
					" (must be equal to or greater than " + (MINIMUM_PARALLEL_EXECUTION_THREADS) + ")");
		}

        this.foreseeDistances = line.hasOption(FORESEE_DISTANCES_PARAM_NAME);
        this.memSpaceShowingRequested = line.hasOption(SHOW_MEMSPACE_USED_PARAM_NAME);
//        this.takeTime = line.hasOption(TIME_ANALYSIS_PARAM);
        
        String
        	outStatsFilePath = line.getOptionValue(STATS_OUT_PATH_PARAM_NAME),
        	listOfExcludedOnesFromResultsFilePath = line.getOptionValue(EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME);
        if (outStatsFilePath != null) {
        	this.statsOutputFile = new File(outStatsFilePath);
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
					buRo.close();
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
        		.hasArg().withArgName("number")
        		.withLongOpt("out-branch")
        		.withDescription("out-branching maximum level for discovered constraints (must be greater than or equal to "
						+ (MINIMUM_BRANCHING_LIMIT)
						+ ", the default)")
        		.withType(new Integer(0))
        		.create(OUT_BRANCHING_LIMIT_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("number")
        		.withLongOpt("kb-ll-threads")
        		.withDescription("threads for log-processing parallel execution (must be greater than or equal to "
						+ (MINIMUM_PARALLEL_EXECUTION_THREADS)
						+ ", the default)")
        		.withType(new Integer(0))
        		.create(KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("number")
        		.withLongOpt("q-ll-threads")
        		.withDescription("threads for querying parallel execution of the knowledge base (must be greater than or equal to "
						+ (MINIMUM_PARALLEL_EXECUTION_THREADS)
						+ ", the default)")
        		.withType(new Integer(0))
        		.create(QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("foresee-distances")
        		.withDescription(
        				attachInstabilityWarningToDescription("compute the foreseen confidence interval for the expected distance between tasks in relation constraints")
        		)
        		.create(FORESEE_DISTANCES_PARAM_NAME)
        		);
//        options.addOption(
//        		OptionBuilder
//        		.withLongOpt("time-aware")
//        		.withDescription(
//        				attachInstabilityWarningToDescription("include the analysis of event timestamps into discovery")
//        		)
//        		.create(TIME_ANALYSIS_PARAM)
//        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("show-mem")
        		.withDescription("show the memory consumption peak (could slow down the overall computation)")
        		.create(SHOW_MEMSPACE_USED_PARAM_NAME)
        		);
        return options;
	}

	public boolean isBranchingRequired() {
		return this.branchingLimit > MINIMUM_BRANCHING_LIMIT;
	}
	
	public boolean isParallelQueryProcessingRequired() {
		return this.queryParallelProcessingThreads > MinerFulCmdParameters.MINIMUM_PARALLEL_EXECUTION_THREADS;
	}
	
	public boolean isParallelKbComputationRequired() {
		return this.kbParallelProcessingThreads > MinerFulCmdParameters.MINIMUM_PARALLEL_EXECUTION_THREADS;
	}
}