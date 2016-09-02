package minerful.postprocessing.params;

import java.util.ArrayList;
import java.util.StringTokenizer;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.params.ParamsManager;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarkingPolicy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;


public class PostProcessingCmdParameters extends ParamsManager {
	public static enum RankingPolicy {
		SUPPORTCONFIDENCEINTERESTFACTOR,
		FAMILYHIERARCHY,
		ACTIVATIONTARGETBONDS,
		RANDOM
	}

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
		
		public boolean isRedundancyCheckRequested() {
			switch(this) {
			case HIERARCHYCONFLICTREDUNDANCY:
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
			}
		}
		
		public boolean isRedundancyDoubleCheckRequested() {
			switch(this) {
			case HIERARCHYCONFLICTREDUNDANCYDOUBLE:
				return true;
			default:
				return false;
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
	}

	public static final String ARRAY_SEPARATOR = ":";
	public static final String ANALYSIS_TYPE_PARAM_NAME = "ppAT";
	public static final String RANKING_POLICY_PARAM_NAME = "ppPP";
	public static final String HIERARCHY_SUBSUMPTION_PRUNING_POLICY_PARAM_NAME = "ppHSPP";
	public static final String KEEP_CONSTRAINTS_PARAM_NAME = "keep";
	public static final char SUPPORT_THRESHOLD_PARAM_NAME = 's';
	public static final char INTEREST_THRESHOLD_PARAM_NAME = 'i';
	public static final char CONFIDENCE_THRESHOLD_PARAM_NAME = 'c';

	public static final Double DEFAULT_SUPPORT_THRESHOLD = Constraint.MAX_SUPPORT;
	public static final Double DEFAULT_INTEREST_FACTOR_THRESHOLD = Constraint.MIN_INTEREST_FACTOR;
	public static final Double DEFAULT_CONFIDENCE_THRESHOLD = Constraint.MIN_CONFIDENCE;
	public static final PostProcessingAnalysisType DEFAULT_ANALYSIS_TYPE = PostProcessingAnalysisType.HIERARCHY;
	public static final HierarchySubsumptionPruningPolicy DEFAULT_HIERARCHY_POLICY = HierarchySubsumptionPruningPolicy.SUPPORTHIERARCHY;
	public static final boolean DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY = false;

	/** Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following in the array is utilised. Default value is {@link #DEFAULT_PRIORITY_POLICIES DEFAULT_PRIORITY_POLICIES}. */
	public ConstraintSortingPolicy[] sortingPolicies;	// mandatory assignment
	/** Type of post-processing analysis required. Default value is {@link #DEFAULT_ANALYSIS_TYPE DEFAULT_ANALYSIS_TYPE}. */
	public PostProcessingAnalysisType analysisType;
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
		this.analysisType = DEFAULT_ANALYSIS_TYPE;
		this.hierarchyPolicy = DEFAULT_HIERARCHY_POLICY;
	    this.supportThreshold = DEFAULT_SUPPORT_THRESHOLD;
	    this.confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD;
	    this.interestFactorThreshold = DEFAULT_INTEREST_FACTOR_THRESHOLD;
	    this.cropRedundantAndInconsistentConstraints = !DEFAULT_REDUNDANT_INCONSISTENT_CONSTRAINTS_KEEPING_POLICY;
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
				this.analysisType = PostProcessingAnalysisType.valueOf(fromStringToEnumValue(analysisTypeString));
			} catch (Exception e) {
				System.err.println("Invalid option for " + ANALYSIS_TYPE_PARAM_NAME + ": " + analysisTypeString + ". Using default value.");
			}
		}
		
		this.updateRankingPolicies(line.getOptionValue(RANKING_POLICY_PARAM_NAME));
	}

	private void updateRankingPolicies(String paramString) {
		if (paramString == null)
			return;
		StringTokenizer strTok = new StringTokenizer(paramString, ARRAY_SEPARATOR);
		String token = null;
		ArrayList<ConstraintSortingPolicy> listOfPolicies = new ArrayList<ConstraintSortingPolicy>(strTok.countTokens());
		ConstraintSortingPolicy policy = null;
		
		while (strTok.hasMoreTokens()) {
			token = strTok.nextToken();
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
                .withLongOpt("post-processing")
                .withDescription("type of post-processing analysis over constraints. It can be one of the following: " + printValues(PostProcessingAnalysisType.values()) +
                		". Default is: " + fromEnumValueToString(DEFAULT_ANALYSIS_TYPE.toString()))
                .withType(new String())
                .create(ANALYSIS_TYPE_PARAM_NAME)
    	);
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("policy")
                .withLongOpt("post-processing-rank")
                .withDescription("type of ranking of constraints for post-processing analysis. It can be a " + ARRAY_SEPARATOR + "-separated list of the following: " + printValues(RankingPolicy.values()) +
                		". Default is: " + fromEnumValueToString(StringUtils.join(DEFAULT_PRIORITY_POLICIES, ARRAY_SEPARATOR)))
                .withType(new String())
                .create(RANKING_POLICY_PARAM_NAME)
    	);
        options.addOption(
                OptionBuilder
                .hasArg().withArgName("threshold")
                .withLongOpt("support")
                .withDescription("threshold for support (reliability); it must be a real value ranging from 0.0 to 1.0" +
                		". Default is: " + DEFAULT_SUPPORT_THRESHOLD)
                .withType(new Double(0))
                .create(SUPPORT_THRESHOLD_PARAM_NAME)
        );
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("threshold")
        		.withLongOpt("confidence")
        		.withDescription("threshold for confidence level (relevance); it must be a real value ranging from 0.0 to 1.0" +
                		". Default is: " + DEFAULT_CONFIDENCE_THRESHOLD)
        		.withType(new Double(0))
        		.create(CONFIDENCE_THRESHOLD_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("threshold")
        		.withLongOpt("interest-factor")
        		.withDescription("threshold for interest factor (relevance); it must be a real value ranging from 0.0 to 1.0" +
                		". Default is: " + DEFAULT_INTEREST_FACTOR_THRESHOLD)
        		.withType(new Double(0))
        		.create(INTEREST_THRESHOLD_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("keep-constraints")
        		.withDescription("do not physically remove the redundant or inconsistent constraints from the model")
        		.withType(new Boolean(false))
        		.create(KEEP_CONSTRAINTS_PARAM_NAME)
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