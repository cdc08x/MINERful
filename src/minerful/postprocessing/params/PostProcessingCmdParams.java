package minerful.postprocessing.params;

import java.util.ArrayList;
import java.util.StringTokenizer;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.CnsSortModularDefaultPolicy;
import minerful.params.ParamsManager;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarkingPolicy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;


public class PostProcessingCmdParams extends ParamsManager {
	public static enum RankingPolicy {
		SUPPORTCONFIDENCEINTERESTFACTOR,
		FAMILYHIERARCHY,
		ACTIVATIONTARGETBONDS,
		RANDOM
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

	public static enum AnalysisType {
		NONE,
		HIERARCHY,	// default
		HIERARCHYCONFLICT,
		HIERARCHYCONFLICTREDUNDANCY,
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
	public static final char SUPPORT_THRESHOLD_PARAM_NAME = 's';
	public static final char INTEREST_THRESHOLD_PARAM_NAME = 'i';
	public static final char CONFIDENCE_THRESHOLD_PARAM_NAME = 'c';

	public static final Double DEFAULT_SUPPORT_THRESHOLD = Constraint.MAX_SUPPORT;
	public static final Double DEFAULT_INTEREST_FACTOR_THRESHOLD = Constraint.MIN_INTEREST_FACTOR;
	public static final Double DEFAULT_CONFIDENCE_THRESHOLD = Constraint.MIN_CONFIDENCE;
	public static final AnalysisType DEFAULT_ANALYSIS_TYPE = AnalysisType.HIERARCHY;
	public static final HierarchySubsumptionPruningPolicy DEFAULT_HIERARCHY_POLICY = HierarchySubsumptionPruningPolicy.SUPPORTHIERARCHY;

	public CnsSortModularDefaultPolicy[] rankingPolicies;	// mandatory assignment
	public AnalysisType analysisType;
	public HierarchySubsumptionPruningPolicy hierarchyPolicy;
    public Double supportThreshold;
    public Double confidenceThreshold;
	public Double interestFactorThreshold;

	public static final CnsSortModularDefaultPolicy[] DEFAULT_PRIORITY_POLICIES = new CnsSortModularDefaultPolicy[] {
		CnsSortModularDefaultPolicy.ACTIVATIONTARGETBONDS,
		CnsSortModularDefaultPolicy.FAMILYHIERARCHY,
		CnsSortModularDefaultPolicy.SUPPORTCONFIDENCEINTERESTFACTOR,
	};
	
	public PostProcessingCmdParams() {
		super();
		this.rankingPolicies = DEFAULT_PRIORITY_POLICIES;
		this.analysisType = DEFAULT_ANALYSIS_TYPE;
		this.hierarchyPolicy = DEFAULT_HIERARCHY_POLICY;
	    this.supportThreshold = DEFAULT_SUPPORT_THRESHOLD;
	    this.confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD;
	    this.interestFactorThreshold = DEFAULT_INTEREST_FACTOR_THRESHOLD;
	}
    
    public PostProcessingCmdParams(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public PostProcessingCmdParams(String[] args) {
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

		String analysisTypeString = line.getOptionValue(ANALYSIS_TYPE_PARAM_NAME);
		if (analysisTypeString != null && !analysisTypeString.isEmpty()) {
			try {
				this.analysisType = AnalysisType.valueOf(fromStringToEnumValue(analysisTypeString));
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
		ArrayList<CnsSortModularDefaultPolicy> listOfPolicies = new ArrayList<CnsSortModularDefaultPolicy>(strTok.countTokens());
		CnsSortModularDefaultPolicy policy = null;
		
		while (strTok.hasMoreTokens()) {
			token = strTok.nextToken();
			token = fromStringToEnumValue(token);
			try {
				policy = CnsSortModularDefaultPolicy.valueOf(token);
			} catch (Exception e) {
				System.err.println("Invalid option for " + RANKING_POLICY_PARAM_NAME + ": " + token + " is going to be ignored.");
			}
			listOfPolicies.add(policy);
		}
		
		if (listOfPolicies.size() > 0) {
			this.rankingPolicies = listOfPolicies.toArray(new CnsSortModularDefaultPolicy[0]);
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
                .withDescription("type of post-processing analysis over constraints. It can be one of the following: " + printValues(AnalysisType.values()) +
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
        return options;
	}
}