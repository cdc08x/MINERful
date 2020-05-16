package minerful.miner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.TaskCharSetFactory;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.index.ConstraintIndexHasseInverseDepthFirstStepper;
import minerful.index.ConstraintIndexHasseMaker;
import minerful.index.ConstraintIndexHassePruner;
import minerful.miner.engine.ProbabilisticRelationInBranchedConstraintsMiningEngine;
import minerful.miner.engine.ProbabilisticRelationOutBranchedConstraintsMiningEngine;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.LocalStatsWrapperForCharsets;
import minerful.miner.stats.charsets.TasksSetCounter;

import org.apache.commons.math3.util.ArithmeticUtils;

public class ProbabilisticRelationBranchedConstraintsMiner extends RelationConstraintsMiner {
	
	// TODO To be made user-defined, not a constant within the code
	public static final boolean INCLUDE_ALTERNATION = true;
	
	public static final String[] COMPUTED_SUPPORTS =
		new String[] {
			RespondedExistence.class.getName(),
			Response.class.getName(), AlternateResponse.class.getName(), ChainResponse.class.getName(),
			Precedence.class.getName(), AlternatePrecedence.class.getName(), ChainPrecedence.class.getName()
		};

	public static final int NO_LIMITS_IN_BRANCHING = Integer.MAX_VALUE;
	protected int branchingLimit = NO_LIMITS_IN_BRANCHING;
	protected TaskCharSetFactory taskCharSetFactory;
	protected ProbabilisticRelationInBranchedConstraintsMiningEngine inBraDisco;
	protected ProbabilisticRelationOutBranchedConstraintsMiningEngine ouBraDisco;

	public ProbabilisticRelationBranchedConstraintsMiner(
			GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
        super(globalStats, taskCharArchive, tasksToQueryFor);
		this.taskCharSetFactory = new TaskCharSetFactory(taskCharArchive);
		this.inBraDisco = new ProbabilisticRelationInBranchedConstraintsMiningEngine(globalStats);
		this.ouBraDisco = new ProbabilisticRelationOutBranchedConstraintsMiningEngine(globalStats);
	}
	
	public ProbabilisticRelationBranchedConstraintsMiner(
			GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor, int branchingLimit) {
		this(globalStats, taskCharArchive, tasksToQueryFor);
		this.branchingLimit =
				(		(branchingLimit < this.taskCharArchive.size())
						?	branchingLimit
						:	NO_LIMITS_IN_BRANCHING
				);
	}

	@Override
	protected Set<? extends Constraint> refineRelationConstraints(
			Set<Constraint> setOfConstraints) {
		// TODO Nothing to do, by now
		return setOfConstraints;
	}

	@Override
	public ConstraintsBag discoverConstraints(ConstraintsBag constraintsBag) {
        // Initialization
        if (constraintsBag == null) {
            constraintsBag = new ConstraintsBag(tasksToQueryFor);
        }
        LocalStatsWrapper auxLocalStats = null;
        Set<Constraint> auxCons = super.makeTemporarySet();

        for (TaskChar tChUnderAnalysis : this.tasksToQueryFor) {
            auxLocalStats = this.globalStats.statsTable.get(tChUnderAnalysis);
            // Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
            if (auxLocalStats.getTotalAmountOfOccurrences() > 0) {
            	logger.info("Evaluating constraints for: " + tChUnderAnalysis + "... ");
            	
                auxCons.addAll(this.discoverRelationConstraints(tChUnderAnalysis, constraintsBag));
                
                logger.info("Done.");
            }
        }
        for (Constraint con : auxCons) {
        	RelationConstraint relCon = (RelationConstraint)con;// come on, I know it can only be a Relation Constraint!
        	if (relCon.isActivationBranched())
        		constraintsBag.add(relCon.getImplied(), relCon);
        	else
        		constraintsBag.add(relCon.getBase(), relCon);
        }
        return constraintsBag;
	}
	
	@Override
	protected Set<Constraint> discoverRelationConstraints(TaskChar taskChUnderAnalysis, ConstraintsBag constraintsBag) {
		ConstraintIndexHasseMaker
			hasseOutMaker = new ConstraintIndexHasseMaker(this.taskCharArchive, this.branchingLimit, taskChUnderAnalysis),
			hasseInMaker = new ConstraintIndexHasseMaker(this.taskCharArchive, this.branchingLimit, taskChUnderAnalysis);
		
		ConstraintIndexHasseInverseDepthFirstStepper stepper = new ConstraintIndexHasseInverseDepthFirstStepper(hasseOutMaker.hasseDiagram);
		
		Set<Constraint> discoveredConstraints = new TreeSet<Constraint>();

		if (!globalStats.isForBranchedConstraints())
			return discoveredConstraints;
		
		LocalStatsWrapper tChUnderAnalysisLocalStats = globalStats.statsTable.get(taskChUnderAnalysis);
		
		// Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
		long tChUnderAnalysisOccurrences = tChUnderAnalysisLocalStats.getTotalAmountOfOccurrences();
		if (tChUnderAnalysisOccurrences <= 0)
			return discoveredConstraints;
/*
		SortedSet<TaskCharSet> combosToAnalyze =
				taskCharSetFactory.createAllMultiCharCombosExcludingOneTaskChar(taskChUnderAnalysis, this.branchingLimit);
*/
		Map<String, Boolean>
			interruptedCalculation = new HashMap<String, Boolean>();
		for (String constraintTemplate : COMPUTED_SUPPORTS) {
			interruptedCalculation.put(constraintTemplate, false);
		}

		RespondedExistence
			nuOBRespondedExistence = null/*,
			nuIBRespondedExistence = null*/;
		Response
			nuOBResponse = null/*,
			nuIBResponse = null*/;
/**/
		AlternateResponse
			nuOBAlternateResponse = null/*,
			nuIBAlternateResponse = null*/;
		ChainResponse
			nuOBChainResponse = null/*,
			nuIBChainResponse = null*/;
		Precedence
			/*nuOBPrecedence = null,*/
			nuIBPrecedence = null;
/**/	
		AlternatePrecedence
			nuIBAlternatePrecedence = null/*,
			/*nuOBAlternatePrecedence = null*/;
		ChainPrecedence
			/*nuOBChainPrecedence = null,*/
			nuIBChainPrecedence = null;
/*		
		CoExistence
			nuOBCoExistence = null,
			nuIBCoExistence = null;
		Succession
			nuOBSuccession = null/*,
			nuIBSuccession = null;
		AlternateSuccession
			nuOBAlternateSuccession = null,
			nuIBAlternateSuccession = null;
		ChainSuccession
			nuOBChainSuccession = null,
			nuIBChainSuccession = null;
		NotCoExistence
			nuOBNotCoExistence = null,
			nuIBNotCoExistence = null;
		NotSuccession
			nuOBNotSuccession = null,
			nuIBNotSuccession = null;
		NotChainSuccession
			nuOBNotChainSuccession = null,
			nuIBNotChainSuccession = null;
*/
		TaskCharSet comboToAnalyze = null;
		while (stepper.isThereAnyNodeLeftToAnalyse()) {
			comboToAnalyze = stepper.getCurrentTaskCharSet();
/*******	Out-branched */
			nuOBRespondedExistence = this.ouBraDisco
					.discoverBranchedRespondedExistenceConstraints(
							taskChUnderAnalysis, tChUnderAnalysisLocalStats, tChUnderAnalysisOccurrences,
							comboToAnalyze);
			nuOBResponse = this.ouBraDisco
					.discoverBranchedResponseConstraints(
							taskChUnderAnalysis, tChUnderAnalysisLocalStats, tChUnderAnalysisOccurrences,
							comboToAnalyze);

			if (INCLUDE_ALTERNATION) {
/**/
				nuOBAlternateResponse = this.ouBraDisco
						.discoverBranchedAlternateResponseConstraints(
								taskChUnderAnalysis,
							tChUnderAnalysisLocalStats, tChUnderAnalysisOccurrences,
							comboToAnalyze);
/**/
			}
			nuOBChainResponse = this.ouBraDisco
					.discoverBranchedChainResponseConstraints(
							taskChUnderAnalysis,
							tChUnderAnalysisLocalStats, tChUnderAnalysisOccurrences,
							comboToAnalyze);
/*				nuOBPrecedence = this.ouBraDisco
						.discoverBranchedPrecedenceConstraints(
								taskChUnderAnalysis,
								comboToAnalyze);
				nuOBAlternatePrecedence = this.ouBraDisco
						.discoverBranchedAlternatePrecedenceConstraints(
								taskChUnderAnalysis,
								comboToAnalyze);
				nuOBChainPrecedence = this.ouBraDisco
						.discoverBranchedChainPrecedenceConstraints(
								taskChUnderAnalysis,
								comboToAnalyze);
				nuOBCoExistence = this.ouBraDisco
						.discoverBranchedCoExistenceConstraints(
								taskChUnderAnalysis, tChUnderAnalysisAppearances,
								comboToAnalyze);
				nuOBSuccession = this.ouBraDisco
						.discoverBranchedSuccessionConstraints(
								taskChUnderAnalysis, tChUnderAnalysisAppearances,
								comboToAnalyze);
				nuOBAlternateSuccession = this.ouBraDisco
						.discoverBranchedAlternateSuccessionConstraints(
								taskChUnderAnalysis, tChUnderAnalysisAppearances,
								comboToAnalyze);
				nuOBChainSuccession = this.ouBraDisco
						.discoverBranchedChainSuccessionConstraints(
								taskChUnderAnalysis, tChUnderAnalysisAppearances,
								comboToAnalyze);
*/
			hasseOutMaker.addConstraint(comboToAnalyze, nuOBRespondedExistence);
			hasseOutMaker.addConstraint(comboToAnalyze, nuOBResponse);
/**/
			if (INCLUDE_ALTERNATION) {
				hasseOutMaker.addConstraint(comboToAnalyze, nuOBAlternateResponse);
			}
/**/
			hasseOutMaker.addConstraint(comboToAnalyze, nuOBChainResponse);
			
			if (hasValuesAboveThresholds(nuOBRespondedExistence)) this.computedConstraintsAboveThresholds++;
			if (hasValuesAboveThresholds(nuOBResponse)) this.computedConstraintsAboveThresholds++;
/**/
			if (INCLUDE_ALTERNATION) {
				if (hasValuesAboveThresholds(nuOBAlternateResponse)) this.computedConstraintsAboveThresholds++;
			}
/**/
			if (hasValuesAboveThresholds(nuOBChainResponse)) this.computedConstraintsAboveThresholds++;
/*
				hasseMaker.addConstraint(comboToAnalyze, nuOBPrecedence);
				hasseMaker.addConstraint(comboToAnalyze, nuOBAlternatePrecedence);
				hasseMaker.addConstraint(comboToAnalyze, nuOBChainPrecedence);
				hasseMaker.addConstraint(comboToAnalyze, nuOBCoExistence);
				hasseMaker.addConstraint(comboToAnalyze, nuOBSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuOBAlternateSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuOBChainSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuOBNotCoExistence);
				hasseMaker.addConstraint(comboToAnalyze, nuOBNotSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuOBNotChainSuccession);
 */

/*******	In-branched */
/*				nuIBRespondedExistence = this.inBraDisco.discoverBranchedRespondedExistenceConstraints(
						taskChUnderAnalysis,
						comboToAnalyze);
				nuIBResponse = this.inBraDisco
					.discoverBranchedResponseConstraints(
							taskChUnderAnalysis,
							comboToAnalyze);
				nuIBAlternateResponse = this.inBraDisco
					.discoverBranchedAlternateResponseConstraints(
							taskChUnderAnalysis,
							comboToAnalyze);
				nuIBChainResponse = this.inBraDisco
					.discoverBranchedChainResponseConstraints(
							taskChUnderAnalysis,
							comboToAnalyze);
*/				
			nuIBPrecedence = this.inBraDisco
				.discoverBranchedPrecedenceConstraints(
						taskChUnderAnalysis,
						tChUnderAnalysisLocalStats,
						tChUnderAnalysisOccurrences,
						comboToAnalyze);
/**/
			if (INCLUDE_ALTERNATION) {
				nuIBAlternatePrecedence = this.inBraDisco
					.discoverBranchedAlternatePrecedenceConstraints(
						taskChUnderAnalysis,
						tChUnderAnalysisLocalStats,
						tChUnderAnalysisOccurrences,
						comboToAnalyze);
			}
/**/
			nuIBChainPrecedence = this.inBraDisco
				.discoverBranchedChainPrecedenceConstraints(
						taskChUnderAnalysis,
						tChUnderAnalysisLocalStats,
						tChUnderAnalysisOccurrences,
						comboToAnalyze);
/*				
				nuIBCoExistence = this.inBraDisco
					.discoverBranchedCoExistenceConstraints(
							taskChUnderAnalysis,
							tChUnderAnalysisLocalStats,
							tChUnderAnalysisAppearances,
							comboToAnalyze);
				nuIBSuccession = this.inBraDisco
					.discoverBranchedSuccessionConstraints(
							taskChUnderAnalysis,
							tChUnderAnalysisLocalStats,
							tChUnderAnalysisAppearances,
							comboToAnalyze);
				nuIBAlternateSuccession = this.inBraDisco
					.discoverBranchedAlternateSuccessionConstraints(
							taskChUnderAnalysis,
							tChUnderAnalysisLocalStats,
							tChUnderAnalysisAppearances,
							comboToAnalyze);
				nuIBChainSuccession = this.inBraDisco
					.discoverBranchedChainSuccessionConstraint(
							taskChUnderAnalysis,
							tChUnderAnalysisLocalStats,
							tChUnderAnalysisAppearances,
							comboToAnalyze);
	
				nuIBNotCoExistence = new NotCoExistence(
					nuIBCoExistence.getBase(), nuIBCoExistence.getImplied(),
						Constraint.complementSupport(nuIBCoExistence.support));
				nuIBNotSuccession = new NotSuccession(
					nuIBSuccession.getBase(), nuIBSuccession.getImplied(),
						Constraint.complementSupport(nuIBSuccession.support));
				nuIBNotChainSuccession = new NotChainSuccession(
					nuIBChainSuccession.getBase(), nuIBChainSuccession.getImplied(),
						Constraint.complementSupport(nuIBChainSuccession.support));
*//*
				hasseMaker.addConstraint(comboToAnalyze, nuIBResponse);
				hasseMaker.addConstraint(comboToAnalyze, nuIBAlternateResponse);
				hasseMaker.addConstraint(comboToAnalyze, nuIBChainResponse);
 */
			hasseInMaker.addConstraint(comboToAnalyze, nuIBPrecedence);
/**/
			if (INCLUDE_ALTERNATION) {
				hasseInMaker.addConstraint(comboToAnalyze, nuIBAlternatePrecedence);
			}
/**/
			hasseInMaker.addConstraint(comboToAnalyze, nuIBChainPrecedence);

			if (hasValuesAboveThresholds(nuIBPrecedence)) this.computedConstraintsAboveThresholds++;
/**/
			if (INCLUDE_ALTERNATION) {
				if (hasValuesAboveThresholds(nuIBAlternatePrecedence)) this.computedConstraintsAboveThresholds++;
			}
/**/
			if (hasValuesAboveThresholds(nuIBChainPrecedence)) this.computedConstraintsAboveThresholds++;

/*
				hasseMaker.addConstraint(comboToAnalyze, nuIBCoExistence);
				hasseMaker.addConstraint(comboToAnalyze, nuIBSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuIBAlternateSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuIBChainSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuIBNotCoExistence);
				hasseMaker.addConstraint(comboToAnalyze, nuIBNotSuccession);
				hasseMaker.addConstraint(comboToAnalyze, nuIBNotChainSuccession);
*/

/*******	Hierarchy and subsumption linking */			
			nuOBResponse.setConstraintWhichThisIsBasedUpon(nuOBRespondedExistence);
			
			if (INCLUDE_ALTERNATION) {
				nuOBAlternateResponse.setConstraintWhichThisIsBasedUpon(nuOBResponse);
				nuOBChainResponse.setConstraintWhichThisIsBasedUpon(nuOBAlternateResponse);
			}
			else {
				nuOBChainResponse.setConstraintWhichThisIsBasedUpon(nuOBResponse);				
			}
			
			// Mind the inversion in roles: nuIBPrecedence -> nuOBRespondedExistence !!
			nuIBPrecedence.setConstraintWhichThisIsBasedUpon(nuOBRespondedExistence);

			if (INCLUDE_ALTERNATION) {
				nuIBAlternatePrecedence.setConstraintWhichThisIsBasedUpon(nuIBPrecedence);
				nuIBChainPrecedence.setConstraintWhichThisIsBasedUpon(nuIBAlternatePrecedence);
			} else {
				nuIBChainPrecedence.setConstraintWhichThisIsBasedUpon(nuIBPrecedence);
			}
/*
				nuOBPrecedence.setConstraintWhichThisIsBasedUpon(nuIBRespondedExistence);
				nuOBAlternatePrecedence.setConstraintWhichThisIsBasedUpon(nuOBPrecedence);
				nuOBChainPrecedence.setConstraintWhichThisIsBasedUpon(nuOBAlternatePrecedence);
				nuOBCoExistence.setImplyingConstraints(nuOBRespondedExistence, nuIBRespondedExistence);
				nuOBSuccession.setConstraintWhichThisIsBasedUpon(nuOBCoExistence);
				nuOBSuccession.setImplyingConstraints(nuOBResponse, nuOBPrecedence);
				nuOBAlternateSuccession.setImplyingConstraints(nuOBAlternateResponse, nuOBAlternatePrecedence);
				nuOBChainSuccession.setImplyingConstraints(nuOBChainResponse, nuOBChainPrecedence);
				nuOBNotCoExistence.setOpposedTo(nuOBCoExistence);
				nuOBNotCoExistence.setConstraintWhichThisIsBasedUpon(nuOBNotSuccession);
				nuOBNotSuccession.setOpposedTo(nuOBSuccession);
				nuOBNotSuccession.setConstraintWhichThisIsBasedUpon(nuOBNotChainSuccession);
				nuOBNotChainSuccession.setOpposedTo(nuOBChainSuccession);
*/			
			stepper.moveOneStepAhead();
		}

		ConstraintIndexHassePruner outPruner = new ConstraintIndexHassePruner(true, hasseOutMaker.hasseDiagram);
		outPruner.prune();
		ConstraintIndexHassePruner inPruner = new ConstraintIndexHassePruner(false, hasseInMaker.hasseDiagram);
		inPruner.prune();

		discoveredConstraints.addAll(outPruner.nonRedundantConstraints());
		discoveredConstraints.addAll(inPruner.nonRedundantConstraints());
		
		double participationFraction = super.computeParticipationFraction(taskChUnderAnalysis, tChUnderAnalysisLocalStats, globalStats.logSize);
		discoveredConstraints = refineByComputingConfidenceLevel(discoveredConstraints, participationFraction);
		
		return discoveredConstraints;
	}
    
    public Set<Constraint> refineByComputingConfidenceLevel(Set<Constraint> discoveredConstraints, double participationFraction) {
		for (Constraint relCon : discoveredConstraints) {
			relCon.setConfidence(relCon.getSupport() * participationFraction);
		}
		return discoveredConstraints;
    }

	protected boolean areLocalStatsOkForBranchedConstraintsAnalysis(LocalStatsWrapper pivotLocalStats) {
		return (pivotLocalStats instanceof LocalStatsWrapperForCharsets);
	}

	protected Collection<TaskChar> getTheRestOfTheAlphabet(Collection<TaskChar> alphabet, TasksSetCounter charSetCounter,
			TaskChar taskToExclude) {
				Collection<TaskChar> supportingTasks = new TreeSet<TaskChar>(alphabet);
				supportingTasks.removeAll(charSetCounter.getTaskCharSet());
				supportingTasks.remove(taskToExclude);
				return supportingTasks;
			}

	public static boolean isBranchingLimited(int branchingLimit) {
		return branchingLimit < NO_LIMITS_IN_BRANCHING;
	}
	
	@Override
	public long howManyPossibleConstraints() {
		int realBranchingLimit =
				(this.branchingLimit < this.taskCharArchive.size()
				?	this.branchingLimit
				:	this.taskCharArchive.size() - 1);
		
		long numberOfPossibleConstraintsPerActivity = 0;
		
		for (int i = 1; i <= realBranchingLimit; i++) {
			numberOfPossibleConstraintsPerActivity +=
				ArithmeticUtils
				.binomialCoefficient(
						this.taskCharArchive.size(), // n
						i); // k
		}
		
		return
				(	MetaConstraintUtils.getAllDiscoverableForwardRelationConstraintTemplates().size() -1 + // out-branching
					MetaConstraintUtils.getAllDiscoverableBackwardRelationConstraintTemplates().size() -1 // in branching
				)
				* tasksToQueryFor.size()
				* numberOfPossibleConstraintsPerActivity;
	}
}