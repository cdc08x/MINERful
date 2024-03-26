package minerful.postprocessing.params;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.params.ParamsManager;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarkingPolicy;


public class PostProcessingCmdParameters extends ParamsManager {
	/**
	 * Specifies the type of post-processing analysis, through which getting rid of redundancies or conflicts in the process specification.
	 * @author Claudio Di Ciccio
	 */
	public static enum PostProcessingAnalysisType {
		/** No post-processing analysis. */
		NONE,
		/** Hierarchical subsumption pruning of constraints. */
		HIERARCHY,	// default
		/** Hierarchical subsumption pruning of constraints and conflicts check. */
		HIERARCHYCONFLICT,
		/** Hierarchical subsumption pruning of constraints, conflicts check, and single-pass automata-based redundancy elimination. */
		HIERARCHYCONFLICTREDUNDANCY,
		/** Hierarchical subsumption pruning of constraints, conflicts check, and double-pass automata-based redundancy elimination. */
		HIERARCHYCONFLICTREDUNDANCYDOUBLE;

		public String getDescription() {
			switch(this) {
			case HIERARCHY:
				return "Template-hierarchy based simplification";
			case HIERARCHYCONFLICT:
				return "Template-hierarchy based simplification plus conflict check";
			case HIERARCHYCONFLICTREDUNDANCY:
				return "Template-hierarchy based simplification plus conflict and redundancy check";
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return "Template-hierarchy based simplification plus conflict and double-pass redundancy check";
			case NONE:
			default:
				return "No simplification";
			}
		}
		
		public boolean isPostProcessingRequested() {
			switch(this) {
			case NONE:
				return false;
			default:
				return true;
			}
		}
		
		public boolean isHierarchySubsumptionResolutionRequested() {
			//conflict default and hierarchy
			switch(this) {
			case HIERARCHY:
			case HIERARCHYCONFLICT:
			case HIERARCHYCONFLICTREDUNDANCY:
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
			}
		}

		public boolean isConflictResolutionRequested() {
			switch(this) {
			case HIERARCHYCONFLICT:
			case HIERARCHYCONFLICTREDUNDANCY:
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
			}
		}

		public boolean isRedundancyResolutionRequested() {
			switch(this) {
			case HIERARCHYCONFLICTREDUNDANCY:
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
			}
		}
		
		public boolean isRedundancyResolutionDoubleCheckRequested() {
			switch(this) {
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
			}
		}
}

	public static final String ANALYSIS_TYPE_PARAM_NAME = "prune";
	public static final String RANKING_POLICY_PARAM_NAME = "pruneRnk";
	public static final String SUBSUMPTION_HIERARCHY_PRUNING_POLICY_PARAM_NAME = "pruneHier";
	public static final String KEEP_CONSTRAINTS_PARAM_NAME = "keep";
	public static final char EVT_SUPPORT_THRESHOLD_PARAM_NAME = 's';
	public static final char EVT_COVERAGE_THRESHOLD_PARAM_NAME = 'g';
	public static final char EVT_CONFIDENCE_THRESHOLD_PARAM_NAME = 'c';
	public static final String TRC_SUPPORT_THRESHOLD_PARAM_NAME = "sT";
	public static final String TRC_COVERAGE_THRESHOLD_PARAM_NAME = "gT";
	public static final String TRC_CONFIDENCE_THRESHOLD_PARAM_NAME = "cT";

	public static final Double DEFAULT_EVT_SUPPORT_THRESHOLD = 0.05;
	public static final Double DEFAULT_EVT_COVERAGE_THRESHOLD = 0.05;
	public static final Double DEFAULT_EVT_CONFIDENCE_THRESHOLD = 0.95;
	public static final Double DEFAULT_TRC_SUPPORT_THRESHOLD = 0.125;
	public static final Double DEFAULT_TRC_COVERAGE_THRESHOLD = 0.125;
	public static final Double DEFAULT_TRC_CONFIDENCE_THRESHOLD = 0.95;
	public static final PostProcessingAnalysisType DEFAULT_POST_PROCESSING_ANALYSIS_TYPE = PostProcessingAnalysisType.HIERARCHY;
	public static final SubsumptionHierarchyPruningPolicy DEFAULT_HIERARCHY_POLICY = SubsumptionHierarchyPruningPolicy.HIERARCHY_FIRST;
	public static final boolean DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY = false;

	/** Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following in the array is utilised. Default value is {@link #DEFAULT_PRIORITY_POLICIES DEFAULT_PRIORITY_POLICIES}. */
	public ConstraintSortingPolicy[] sortingPolicies;	// mandatory assignment
	/** Type of post-processing analysis required. Default value is {@link #DEFAULT_POST_PROCESSING_ANALYSIS_TYPE DEFAULT_ANALYSIS_TYPE}. */
	public PostProcessingAnalysisType postProcessingAnalysisType;
	/** Ignore this: it is still unused -- Policies according to which constraints are ranked in terms of significance. Default value is {@link #DEFAULT_HIERARCHY_POLICY DEFAULT_HIERARCHY_POLICY}. */
	public SubsumptionHierarchyPruningPolicy hierarchyPolicy;
	/** Minimum event-based support threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_EVT_SUPPORT_THRESHOLD DEFAULT_SUPPORT_THRESHOLD}. */
    public Double evtSupportThreshold;
	/** Minimum event-based confidence level threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_EVT_CONFIDENCE_THRESHOLD DEFAULT_CONFIDENCE_THRESHOLD}. */
    public Double evtConfidenceThreshold;
	/** Minimum event-based coverage threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_EVT_COVERAGE_THRESHOLD DEFAULT_INTEREST_FACTOR_THRESHOLD}. */
	public Double evtCoverageThreshold;
	/** Minimum trace-based support threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_TRC_SUPPORT_THRESHOLD DEFAULT_SUPPORT_THRESHOLD}. */
    public Double trcSupportThreshold;
	/** Minimum trace-based confidence level threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_TRC_CONFIDENCE_THRESHOLD DEFAULT_CONFIDENCE_THRESHOLD}. */
    public Double trcConfidenceThreshold;
	/** Minimum trace-based coverage threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_TRC_COVERAGE_THRESHOLD DEFAULT_INTEREST_FACTOR_THRESHOLD}. */
	public Double trcCoverageThreshold;
	/** Specifies whether the redundant or inconsistent constraints should be only marked as such (<code>false</code>), hence hidden, or cropped (removed) from the specification (<code>true</code>) */
	public boolean cropRedundantAndInconsistentConstraints;

	public static final ConstraintSortingPolicy[] DEFAULT_PRIORITY_POLICIES = new ConstraintSortingPolicy[] {
		ConstraintSortingPolicy.ACTIVATIONTARGETBONDS,
		ConstraintSortingPolicy.FAMILYHIERARCHY,
		ConstraintSortingPolicy.SUPPORTCONFIDENCECOVERAGE,
	};
	
	public PostProcessingCmdParameters() {
		super();
		this.sortingPolicies = DEFAULT_PRIORITY_POLICIES;
		this.postProcessingAnalysisType = DEFAULT_POST_PROCESSING_ANALYSIS_TYPE;
		this.hierarchyPolicy = DEFAULT_HIERARCHY_POLICY;
	    this.evtSupportThreshold = DEFAULT_EVT_SUPPORT_THRESHOLD;
	    this.evtConfidenceThreshold = DEFAULT_EVT_CONFIDENCE_THRESHOLD;
	    this.evtCoverageThreshold = DEFAULT_EVT_COVERAGE_THRESHOLD;
	    this.trcSupportThreshold = DEFAULT_TRC_SUPPORT_THRESHOLD;
	    this.trcConfidenceThreshold = DEFAULT_TRC_CONFIDENCE_THRESHOLD;
	    this.trcCoverageThreshold = DEFAULT_TRC_COVERAGE_THRESHOLD;
	    this.cropRedundantAndInconsistentConstraints = !DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY;
	}
	
	public static PostProcessingCmdParameters makeParametersForNoPostProcessing() {
		PostProcessingCmdParameters noPostProcessParams = new PostProcessingCmdParameters();
		noPostProcessParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		noPostProcessParams.hierarchyPolicy = SubsumptionHierarchyPruningPolicy.NONE;
		noPostProcessParams.evtSupportThreshold = 0.0;
		noPostProcessParams.evtConfidenceThreshold = 0.0;
		noPostProcessParams.evtCoverageThreshold = 0.0;
		noPostProcessParams.trcSupportThreshold = 0.0;
		noPostProcessParams.trcConfidenceThreshold = 0.0;
		noPostProcessParams.trcCoverageThreshold = 0.0;
		noPostProcessParams.cropRedundantAndInconsistentConstraints = DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY;
	
		return noPostProcessParams;
	}
    
    public PostProcessingCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public PostProcessingCmdParameters(String[] args) {
		this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	

	@Override
	protected void setup(CommandLine line) {
		this.evtSupportThreshold = Double.valueOf(
				line.getOptionValue(
						EVT_SUPPORT_THRESHOLD_PARAM_NAME,
						this.evtSupportThreshold.toString()
				)
		);
		this.evtCoverageThreshold = Double.valueOf(
				line.getOptionValue(
						EVT_COVERAGE_THRESHOLD_PARAM_NAME,
						this.evtCoverageThreshold.toString()
						)
				);
		this.evtConfidenceThreshold = Double.valueOf(
				line.getOptionValue(
						EVT_CONFIDENCE_THRESHOLD_PARAM_NAME,
						this.evtConfidenceThreshold.toString()
						)
				);
		this.trcSupportThreshold = Double.valueOf(
				line.getOptionValue(
						TRC_SUPPORT_THRESHOLD_PARAM_NAME,
						this.trcSupportThreshold.toString()
				)
		);
		this.trcCoverageThreshold = Double.valueOf(
				line.getOptionValue(
						TRC_COVERAGE_THRESHOLD_PARAM_NAME,
						this.trcCoverageThreshold.toString()
						)
				);
		this.trcConfidenceThreshold = Double.valueOf(
				line.getOptionValue(
						TRC_CONFIDENCE_THRESHOLD_PARAM_NAME,
						this.trcConfidenceThreshold.toString()
						)
				);
		
		if (line.hasOption(KEEP_CONSTRAINTS_PARAM_NAME)) {
			this.cropRedundantAndInconsistentConstraints = false;
		} else {
			this.cropRedundantAndInconsistentConstraints = !DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY;
		}

		String analysisTypeString = line.getOptionValue(ANALYSIS_TYPE_PARAM_NAME);
		if (analysisTypeString != null && !analysisTypeString.isEmpty()) {
			try {
				this.postProcessingAnalysisType = PostProcessingAnalysisType.valueOf(fromStringToEnumValue(analysisTypeString));
			} catch (Exception e) {
				System.err.println("Invalid option for " + ANALYSIS_TYPE_PARAM_NAME + ": " + analysisTypeString + ". Using default value.");
			}
		}
		
		this.updateRankingPolicies(line.getOptionValue(RANKING_POLICY_PARAM_NAME));


		String hierarchyPolicyString = line.getOptionValue(SUBSUMPTION_HIERARCHY_PRUNING_POLICY_PARAM_NAME);
		if (hierarchyPolicyString != null && !hierarchyPolicyString.isEmpty()) {
			try {
				this.hierarchyPolicy = SubsumptionHierarchyPruningPolicy.valueOf(fromStringToEnumValue(hierarchyPolicyString));
			} catch (Exception e) {
				System.err.println("Invalid option for " + SUBSUMPTION_HIERARCHY_PRUNING_POLICY_PARAM_NAME + ": " + hierarchyPolicyString + ". Using default value.");
			}
		}
	}



	private void updateRankingPolicies(String paramString) {
		String[] tokens = tokenise(paramString);
		if (tokens == null)
			return;

		ArrayList<ConstraintSortingPolicy> listOfPolicies = new ArrayList<ConstraintSortingPolicy>(tokens.length);
		ConstraintSortingPolicy policy = null;
		
		for (String token : tokens) {
			token = fromStringToEnumValue(token);
			try {
				policy = ConstraintSortingPolicy.valueOf(token);
			} catch (Exception e) {
				System.err.println("Invalid option for " + RANKING_POLICY_PARAM_NAME + ": " + token + " is going to be ignored.");
			}
			listOfPolicies.add(policy);
		}
		
		if (listOfPolicies.size() > 0) {
			this.sortingPolicies = listOfPolicies.toArray(new ConstraintSortingPolicy[0]);
		} else {
			System.err.println("No valid option for " + RANKING_POLICY_PARAM_NAME + ". Using default value.");
		}
	}

	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
                Option.builder(ANALYSIS_TYPE_PARAM_NAME)
						.hasArg().argName("type")
						.longOpt("prune-with")
						.desc("type of post-processing analysis over constraints. It can be one of the following: " + printValues(PostProcessingAnalysisType.values())
						+ printDefault(fromEnumValueToString(DEFAULT_POST_PROCESSING_ANALYSIS_TYPE)))
						.type(String.class)
						.build()
    	);
        options.addOption(
                Option.builder(RANKING_POLICY_PARAM_NAME)
						.hasArg().argName("policy")
						.longOpt("prune-ranking-by")
						.desc("type of constraint ranking for post-processing analysis. It can be a " + ARRAY_TOKENISER_SEPARATOR + "-separated list of the following: " + printValues(ConstraintSortingPolicy.values())
						+ printDefault(fromEnumValuesToTokenJoinedString(DEFAULT_PRIORITY_POLICIES)))
						.type(String.class)
						.build()
    	);
		options.addOption(
                Option.builder(SUBSUMPTION_HIERARCHY_PRUNING_POLICY_PARAM_NAME)
						.hasArg().argName("hierarchy-policy")
						.longOpt("prune-hierarchy-by")
						.desc("determines whether a subsumed constraint is retained whenever its quality measures are higher than the subsuming one ('"
								+ fromEnumValueToString(SubsumptionHierarchyPruningPolicy.MEASURE_FIRST) 
								+ "') or is discarded in favour of the subsuming one regardless ('"
								+ fromEnumValueToString(SubsumptionHierarchyPruningPolicy.HIERARCHY_FIRST)
								+ "'). Notice that this parameter takes effect only if the -"
								+ ANALYSIS_TYPE_PARAM_NAME
								+ " parameter is not set to '" + fromEnumValueToString(PostProcessingAnalysisType.NONE) + "'"
						+ printDefault(fromEnumValuesToTokenJoinedString(DEFAULT_HIERARCHY_POLICY)))
						.type(String.class)
						.build()
    	);
        options.addOption(
                Option.builder(String.valueOf(EVT_SUPPORT_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("support")
						.desc("threshold for support; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_EVT_SUPPORT_THRESHOLD))
						.type(Double.class)
						.build()
        );
        options.addOption(
        		Option.builder(String.valueOf(EVT_CONFIDENCE_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("confidence")
						.desc("threshold for confidence; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_EVT_CONFIDENCE_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
        		Option.builder(String.valueOf(EVT_COVERAGE_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("coverage")
						.desc("threshold for coverage; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_EVT_COVERAGE_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
                Option.builder(String.valueOf(TRC_SUPPORT_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("trace-support")
						.desc("threshold for trace-based support; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_TRC_SUPPORT_THRESHOLD))
						.type(Double.class)
						.build()
        );
        options.addOption(
        		Option.builder(String.valueOf(TRC_CONFIDENCE_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("trace-confidence")
						.desc("threshold for trace-based confidence; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_TRC_CONFIDENCE_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
        		Option.builder(String.valueOf(TRC_COVERAGE_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("trace-coverage")
						.desc("threshold for trace-based coverage; it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_TRC_COVERAGE_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
        		Option.builder(KEEP_CONSTRAINTS_PARAM_NAME)
						.longOpt("keep-constraints")
						.desc("do not physically remove the redundant or inconsistent constraints from the specification")
						.type(Boolean.class)
						.build()
        		);
        return options;
	}
	

		public static enum SubsumptionHierarchyPruningPolicy {
		NONE,
		HIERARCHY_FIRST, // default
		MEASURE_FIRST;	
		
		public SubsumptionHierarchyMarkingPolicy translate() {
			switch(this) {
			case MEASURE_FIRST:
				return SubsumptionHierarchyMarkingPolicy.EAGER_ON_CONFIDENCE_OVER_HIERARCHY;
			case HIERARCHY_FIRST:
			default:
				return SubsumptionHierarchyMarkingPolicy.EAGER_ON_HIERARCHY_OVER_CONFIDENCE;
			}
		}
	}
}