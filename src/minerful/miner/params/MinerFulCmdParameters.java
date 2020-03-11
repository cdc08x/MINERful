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
import org.apache.commons.cli.Options;

public class MinerFulCmdParameters extends ParamsManager {
	public enum CONSTRAINT_KINDS {
		EXISTENCE, RELATION,
		FORWARD, BACKWARD, MUTUAL,
		DIRECTIONED, UNDIRECTIONED,
		POSITIVE, NEGATIVE
	}
	
	public static final String STATS_OUT_PATH_PARAM_NAME = "statsXML";
	public static final String OUT_BRANCHING_LIMIT_PARAM_NAME = "b";
	public static final String FORESEE_DISTANCES_PARAM_NAME = "withDist";
	public static final String SHOW_MEMSPACE_USED_PARAM_NAME = "showMem";
	public static final String EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME = "exclTasks";
	public static final String KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME = "para";
	public static final String QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME = "paraQ";
//	public static final String TIME_ANALYSIS_PARAM_NAME = "time";

	public static final Integer MINIMUM_BRANCHING_LIMIT = 1;
	public static final Integer DEFAULT_OUT_BRANCHING_LIMIT = MINIMUM_BRANCHING_LIMIT;
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
		this.branchingLimit = DEFAULT_OUT_BRANCHING_LIMIT;
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
        
       	this.statsOutputFile = openOutputFile(line, STATS_OUT_PATH_PARAM_NAME);

        File listOfExcludedOnesFromResultsFile = openInputFile(line, EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME);
        if (listOfExcludedOnesFromResultsFile != null) {
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
				throw new IllegalArgumentException("Unreadable file: " + line.getOptionValue(EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME));
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
				Option.builder(STATS_OUT_PATH_PARAM_NAME)
						.hasArg().argName("path")
						.longOpt("stats-XML-out")
						.desc("path of the file in which the statistics kept in the MINERful knowledge base (say, that task A occurs but B does not for N times, etc.) should be saved; the file is stored in an XML format")
						.type(String.class)
						.build()
        		);
        options.addOption(
                Option.builder(EXCLUDED_FROM_RESULTS_SPEC_FILE_PATH_PARAM_NAME)
						.hasArg().argName("path")
						.longOpt("exclude-results-in")
						.desc("path of the file where the tasks to exclude from the result are listed")
						.type(String.class)
						.build()
    	);
        options.addOption(
        		Option.builder(OUT_BRANCHING_LIMIT_PARAM_NAME)
						.hasArg().argName("number")
						.longOpt("out-branch")
						.desc("out-branching maximum level for discovered constraints (must be greater than or equal to "
						+ (MINIMUM_BRANCHING_LIMIT)
						+ ")"
						+ printDefault(DEFAULT_OUT_BRANCHING_LIMIT))
						.type(String.class)
						.build()
        		);
        options.addOption(
        		Option.builder(KB_PARALLEL_COMPUTATION_THREADS_PARAM_NAME)
						.hasArg().argName("number")
						.longOpt("kb-ll-threads")
						.desc("threads for log-processing parallel execution (must be greater than or equal to "
						+ (MINIMUM_PARALLEL_EXECUTION_THREADS)
						+ ")"
						+ printDefault(MINIMUM_PARALLEL_EXECUTION_THREADS))
						.type(String.class)
						.build()
        		);
        options.addOption(
        		Option.builder(QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME)
						.hasArg().argName("number")
						.longOpt("q-ll-threads")
						.desc("threads for querying parallel execution of the knowledge base (must be greater than or equal to "
						+ (MINIMUM_PARALLEL_EXECUTION_THREADS)
						+ ")"
						+ printDefault(MINIMUM_PARALLEL_EXECUTION_THREADS))
						.type(String.class)
						.build()
        		);
        options.addOption(
        		Option.builder(FORESEE_DISTANCES_PARAM_NAME)
						.longOpt("foresee-distances")
						.desc(
						attachInstabilityWarningToDescription("compute the foreseen confidence interval for the expected distance between tasks in relation constraints")
				)
						.build()
        		);
//        options.addOption(
//        		Option.builder(TIME_ANALYSIS_PARAM)
//        		.longOpt("time-aware")
//        		.desc(
//        				attachInstabilityWarningToDescription("include the analysis of event timestamps into discovery")
//        		)
//        		);
        options.addOption(
        		Option.builder(SHOW_MEMSPACE_USED_PARAM_NAME)
						.longOpt("show-mem-peak")
						.desc("show the memory consumption peak (could slow down the overall computation)")
						.build()
        		);
        options.addOption(
        		Option.builder(QUERY_PARALLEL_COMPUTATION_THREADS_PARAM_NAME)
						.hasArg().argName("number")
						.longOpt("q-ll-threads")
						.desc("threads for querying parallel execution of the knowledge base (must be greater than or equal to "
						+ (MINIMUM_PARALLEL_EXECUTION_THREADS)
						+ ")"
						+ printDefault(MINIMUM_PARALLEL_EXECUTION_THREADS))
						.type(String.class)
						.build()
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