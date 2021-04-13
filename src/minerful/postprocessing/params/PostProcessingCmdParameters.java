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
	 * Specifies the type of post-processing analysis, through which getting rid of redundancies or conflicts in the process model.
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
//	public static final String HIERARCHY_SUBSUMPTION_PRUNING_POLICY_PARAM_NAME = "ppHSPP"; // TODO One day
	public static final String KEEP_CONSTRAINTS_PARAM_NAME = "keep";
	public static final char SUPPORT_THRESHOLD_PARAM_NAME = 's';
	public static final char INTEREST_THRESHOLD_PARAM_NAME = 'i';
	public static final char CONFIDENCE_THRESHOLD_PARAM_NAME = 'c';

	public static final Double DEFAULT_SUPPORT_THRESHOLD = 0.95;
	public static final Double DEFAULT_INTEREST_FACTOR_THRESHOLD = 0.125;
	public static final Double DEFAULT_CONFIDENCE_THRESHOLD = 0.25;
	public static final PostProcessingAnalysisType DEFAULT_POST_PROCESSING_ANALYSIS_TYPE = PostProcessingAnalysisType.HIERARCHY;
	public static final HierarchySubsumptionPruningPolicy DEFAULT_HIERARCHY_POLICY = HierarchySubsumptionPruningPolicy.SUPPORTHIERARCHY;
	public static final boolean DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY = false;

	/** Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following in the array is utilised. Default value is {@link #DEFAULT_PRIORITY_POLICIES DEFAULT_PRIORITY_POLICIES}. */
	public ConstraintSortingPolicy[] sortingPolicies;	// mandatory assignment
	/** Type of post-processing analysis required. Default value is {@link #DEFAULT_POST_PROCESSING_ANALYSIS_TYPE DEFAULT_ANALYSIS_TYPE}. */
	public PostProcessingAnalysisType postProcessingAnalysisType;
	/** Ignore this: it is still unused -- Policies according to which constraints are ranked in terms of significance. Default value is {@link #DEFAULT_HIERARCHY_POLICY DEFAULT_HIERARCHY_POLICY}. */
	public HierarchySubsumptionPruningPolicy hierarchyPolicy;
	/** Minimum support threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_SUPPORT_THRESHOLD DEFAULT_SUPPORT_THRESHOLD}. */
    public Double supportThreshold;
	/** Minimum confidence level threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_CONFIDENCE_THRESHOLD DEFAULT_CONFIDENCE_THRESHOLD}. */
    public Double confidenceThreshold;
	/** Minimum interest factor threshold required to consider a discovered constraint significant. Default value is {@link #DEFAULT_INTEREST_FACTOR_THRESHOLD DEFAULT_INTEREST_FACTOR_THRESHOLD}. */
	public Double interestFactorThreshold;
	/** Specifies whether the redundant or inconsistent constraints should be only marked as such (<code>false</code>), hence hidden, or cropped (removed) from the model (<code>true</code>) */
	public boolean cropRedundantAndInconsistentConstraints;

	public static final ConstraintSortingPolicy[] DEFAULT_PRIORITY_POLICIES = new ConstraintSortingPolicy[] {
		ConstraintSortingPolicy.ACTIVATIONTARGETBONDS,
		ConstraintSortingPolicy.FAMILYHIERARCHY,
		ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR,
	};
	
	public PostProcessingCmdParameters() {
		super();
		this.sortingPolicies = DEFAULT_PRIORITY_POLICIES;
		this.postProcessingAnalysisType = DEFAULT_POST_PROCESSING_ANALYSIS_TYPE;
		this.hierarchyPolicy = DEFAULT_HIERARCHY_POLICY;
	    this.supportThreshold = DEFAULT_SUPPORT_THRESHOLD;
	    this.confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD;
	    this.interestFactorThreshold = DEFAULT_INTEREST_FACTOR_THRESHOLD;
	    this.cropRedundantAndInconsistentConstraints = !DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY;
	}
	
	public static PostProcessingCmdParameters makeParametersForNoPostProcessing() {
		PostProcessingCmdParameters noPostProcessParams = new PostProcessingCmdParameters();
		noPostProcessParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		noPostProcessParams.hierarchyPolicy = HierarchySubsumptionPruningPolicy.NONE;
		noPostProcessParams.supportThreshold = 0.0;
		noPostProcessParams.confidenceThreshold = 0.0;
		noPostProcessParams.interestFactorThreshold = 0.0;
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
		this.supportThreshold = Double.valueOf(
				line.getOptionValue(
						SUPPORT_THRESHOLD_PARAM_NAME,
						this.supportThreshold.toString()
				)
		);
		this.interestFactorThreshold = Double.valueOf(
				line.getOptionValue(
						INTEREST_THRESHOLD_PARAM_NAME,
						this.interestFactorThreshold.toString()
						)
				);
		this.confidenceThreshold = Double.valueOf(
				line.getOptionValue(
						CONFIDENCE_THRESHOLD_PARAM_NAME,
						this.confidenceThreshold.toString()
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
						.desc("type of ranking of constraints for post-processing analysis. It can be a " + ARRAY_TOKENISER_SEPARATOR + "-separated list of the following: " + printValues(ConstraintSortingPolicy.values())
						+ printDefault(fromEnumValuesToTokenJoinedString(DEFAULT_PRIORITY_POLICIES)))
						.type(String.class)
						.build()
    	);
        options.addOption(
                Option.builder(String.valueOf(SUPPORT_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("support")
						.desc("threshold for support (reliability); it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_SUPPORT_THRESHOLD))
						.type(Double.class)
						.build()
        );
        options.addOption(
        		Option.builder(String.valueOf(CONFIDENCE_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("confidence")
						.desc("threshold for confidence level (relevance); it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_CONFIDENCE_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
        		Option.builder(String.valueOf(INTEREST_THRESHOLD_PARAM_NAME))
						.hasArg().argName("threshold")
						.longOpt("interest-factor")
						.desc("threshold for interest factor (relevance); it must be a real value ranging from 0.0 to 1.0"
						+ printDefault(DEFAULT_INTEREST_FACTOR_THRESHOLD))
						.type(Double.class)
						.build()
        		);
        options.addOption(
        		Option.builder(KEEP_CONSTRAINTS_PARAM_NAME)
						.longOpt("keep-constraints")
						.desc("do not physically remove the redundant or inconsistent constraints from the model")
						.type(Boolean.class)
						.build()
        		);
        return options;
	}
	

	// TODO Still unused
	public static enum HierarchySubsumptionPruningPolicy {
		NONE,
		HIERARCHY,
		SUPPORTHIERARCHY;	// default
		
		public SubsumptionHierarchyMarkingPolicy translate() {
			switch(this) {
			case HIERARCHY:
				return SubsumptionHierarchyMarkingPolicy.EAGER_ON_HIERARCHY_OVER_SUPPORT;
			case SUPPORTHIERARCHY:
			default:
				return SubsumptionHierarchyMarkingPolicy.EAGER_ON_SUPPORT_OVER_HIERARCHY;
			}
		}
	}
}