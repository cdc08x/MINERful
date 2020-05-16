package minerful.postprocessing.pruning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import minerful.automaton.AutomatonFactory;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.MutualRelationConstraint;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.index.ModularConstraintsSorter;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.postprocessing.params.PostProcessingCmdParameters;

public class ConflictAndRedundancyResolver {
	public static final String CONFLICT_REDUNDANCY_CHECK_CODE = "'CR-check'";

	public static final int MAXIMUM_VISIBLE_CONSTRAINTS_FOR_REDUNDANCY_CHECK = 24;

	private ProcessModel safeProcess;
	private ProcessModel originalProcess;
	private ConstraintsBag originallHierarchyUnredundantBag;
	private boolean checking;
	private final boolean avoidingRedundancy;
	private final boolean avoidingRedundancyWithDoubleCheck;
	private ModularConstraintsSorter sorter;
	private SubsumptionHierarchyMarker subsumMarker;

	private Automaton safeAutomaton;

	private TreeSet<Constraint> blackboard;
	
	private long secondPassStartTime = -1;

	private static Logger logger = Logger.getLogger(ConflictAndRedundancyResolver.class.getCanonicalName());
	
	private Set<Constraint>
		originalHierarchyUnredundantConstraints,
		notSurelySafeProcessConstraints,
		conflictingConstraintsInOriginalNonRedundantModel,
		conflictingConstraintsInOriginalModel,
		conflictingConstraints,
		redundantConstraints,
		redundantConstraintsAtSecondPass,
		redundantConstraintsInOriginalModel;
	private int
		conflictChecksPerformed,
		redundancyChecksPerformed;
	private ConstraintSortingPolicy[] rankingPolicies;
	
	public ConflictAndRedundancyResolver(ProcessModel process, PostProcessingCmdParameters params) {
		this.avoidingRedundancyWithDoubleCheck = params.postProcessingAnalysisType.isRedundancyResolutionDoubleCheckRequested();
		this.avoidingRedundancy = this.avoidingRedundancyWithDoubleCheck || params.postProcessingAnalysisType.isRedundancyResolutionRequested();
		this.originalProcess = process;
		this.sorter = new ModularConstraintsSorter();
		this.rankingPolicies = params.sortingPolicies;
		this.subsumMarker = new SubsumptionHierarchyMarker();
		this.subsumMarker.setPolicy(SubsumptionHierarchyMarkingPolicy.CONSERVATIVE);
		this.init();
	}
	
	public void init() {
		this.checking = false;
		this.conflictChecksPerformed = 0;
		this.redundancyChecksPerformed = 0;
		this.conflictingConstraints = new TreeSet<Constraint>();
		this.redundantConstraints = new TreeSet<Constraint>();
		this.redundantConstraintsAtSecondPass = new TreeSet<Constraint>();
		
		// Pre-processing: mark subsumption-redundant constraints
		this.subsumMarker.setConstraintsBag(this.originalProcess.bag);
		this.subsumMarker.markSubsumptionRedundantConstraints();
		// Create a copy of the original bag where subsumption-redundant constraints are removed
		this.originallHierarchyUnredundantBag = (ConstraintsBag) this.originalProcess.bag.clone();
		this.originallHierarchyUnredundantBag.removeMarkedConstraints();
		this.originalHierarchyUnredundantConstraints = this.originallHierarchyUnredundantBag.getAllConstraints();
		/*
		 * The blackboard is meant to associate to all constraints a tick,
		 * whenever the constraint has already been checked
		 */
		this.sorter.setConstraints(originalHierarchyUnredundantConstraints);
		this.blackboard = new TreeSet<Constraint>(this.sorter.getComparator());
		ConstraintsBag safeBag = this.originallHierarchyUnredundantBag.getOnlyFullySupportedConstraintsInNewBag();
		Collection<Constraint> safeConstraints = safeBag.getAllConstraints();
		this.sorter.setConstraints(safeConstraints);

		/*
		 * Step 1: Consider as safe those constraints that have a support
		 * of 100%: if they have a support of 100%, a model already exists for
		 * them: the log itself. So, their conjunction cannot be unsatisfiable.
		 */
		if (avoidingRedundancy) {
			logger.info("Checking redundancies of fully-supported constraints...");

			ConstraintsBag emptyBag = this.originallHierarchyUnredundantBag.createEmptyIndexedCopy();
			this.safeProcess = new ProcessModel(this.originalProcess.getTaskCharArchive(), emptyBag);
			Automaton candidateAutomaton = null;
			this.safeAutomaton = this.safeProcess.buildAlphabetAcceptingAutomaton();
			for (Constraint candidateCon : this.sorter.sort(this.rankingPolicies)) {
				logger.trace("Checking redundancy of " + candidateCon);
				candidateAutomaton = new RegExp(candidateCon.getRegularExpression()).toAutomaton();
				if (	!candidateCon.isRedundant()	// If this constraint was not already found to be redundant in some way before
					&&	!this.isConstraintAlreadyChecked(candidateCon)	// If this constraint was not already checked
					&&	this.checkRedundancy(this.safeAutomaton, this.safeProcess.bag, candidateAutomaton, candidateCon)
					// and the check of redundancy has a negative response (namely, it is not redundant)
				) {
					this.safeAutomaton = this.intersect(this.safeAutomaton, candidateAutomaton);
					this.safeProcess.bag.add(candidateCon.getBase(), candidateCon);
				}
				blackboard.add(candidateCon);
			}
		} else {
			this.safeProcess = new ProcessModel(this.originalProcess.getTaskCharArchive(), safeBag);
			this.safeAutomaton = this.safeProcess.buildAutomaton();
			for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(safeBag)) {
//System.out.println("PRESENTATION -- The safe constraint: " + c + " supp: " + c.support + "; conf: " + c.confidence + "; inf.f: " + c.interestFactor + " rex: " + c.getRegularExpression());
//System.out.println("PRESENTATION -- The safe constraint automaton: " + c + " \n" + safeProcess.buildAlphabetAcceptingAutomaton().intersection(new RegExp(c.getRegularExpression()).toAutomaton()).toDot());
				blackboard.add(c);
			}
		}

//System.out.println("PRESENTATION -- The safe automaton:\n" + safeAutomaton.toDot());
		ConstraintsBag unsafeBag = this.originallHierarchyUnredundantBag.createComplementOfCopyPrunedByThreshold(Constraint.MAX_SUPPORT);
//		for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(unsafeBag)) {
//			blackboard.add(c);
//		}
		this.sorter.setConstraints(unsafeBag.getAllConstraints());
		this.notSurelySafeProcessConstraints = this.sorter.sort(this.rankingPolicies);
	}

	public ProcessModel resolveConflictsOrRedundancies() {
		logger.info("Checking redundancies and conflicts of non-fully-supported constraints");

		this.checking = true;
		Automaton candidateAutomaton = null;
		for (Constraint candidateCon : this.notSurelySafeProcessConstraints) {
			if (!isConstraintAlreadyChecked(candidateCon)) {
				logger.trace("Checking consistency of " + candidateCon);
//System.out.println("PRESENTATION -- The unsafe constraint: " + candidateCon + " supp: " + candidateCon.support + "; conf: " + candidateCon.confidence + "; inf.f: " + candidateCon.interestFactor);
//System.out.println("PRESENTATION -- The unsafe constraint automaton: " + candidateCon + " \n" + safeProcess.buildAlphabetAcceptingAutomaton().intersection(new RegExp(candidateCon.getRegularExpression()).toAutomaton()).toDot());
				candidateAutomaton = new RegExp(candidateCon.getRegularExpression()).toAutomaton();
				if (!this.avoidingRedundancy || this.checkRedundancy(candidateAutomaton, candidateCon))
					resolveConflictsRecursively(candidateAutomaton, candidateCon);
			}
		}

//		safeProcess.bag = safeProcess.bag.markSubsumptionRedundantConstraints();
//		safeProcess.bag.removeMarkedConstraints();

		if (this.avoidingRedundancyWithDoubleCheck) {
			logger.info("Checking redundant constraints in a second pass...");

			this.doubleCheckRedundancies();
		}

		this.subsumMarker.setConstraintsBag(this.safeProcess.bag);
		this.subsumMarker.markSubsumptionRedundantConstraints();

		this.checking = false;

		return this.safeProcess;
	}

	private void doubleCheckRedundancies() {
		this.secondPassStartTime = System.currentTimeMillis();
		
		if (this.safeProcess.howManyConstraints() > 1) {
			sorter.setConstraints(this.safeProcess.getAllConstraints());
			// Let us take ALL constraints of the safe process
			ArrayList<Constraint> constraintsSortedForDoubleCheck = new ArrayList<Constraint>(sorter.sort(this.rankingPolicies));
			// Let us visit them in the reverse order with which they were added -- so as to be consistent with the given ranking policy
			ListIterator<Constraint> iterator =
					constraintsSortedForDoubleCheck.listIterator(
							constraintsSortedForDoubleCheck.size()
							// The last one is the constraint that we checked last. In theory, it should not constitute a problem
							- 2);
			Constraint candidateCon = null;
			Automaton secondPassGridCheckAutomaton = null;
			
			while (iterator.hasPrevious()) {
				candidateCon = iterator.previous();
				logger.trace("Second-pass grid check of constraint: " +  candidateCon);
				
				secondPassGridCheckAutomaton =
						AutomatonFactory.buildAutomaton(
								this.safeProcess.bag,
								this.safeProcess.getTaskCharArchive().getIdentifiersAlphabet(),
								candidateCon);
				
				// If the safe automaton accepts 
				if (secondPassGridCheckAutomaton.subsetOf(
						// ... all the constraints BUT the current one...
//					this.safeAutomaton.minus(
						// ... accepts a subset of the languages that the current one accepts...
//							new RegExp(candidateCon.getRegularExpression()).toAutomaton()))) {
						this.safeAutomaton)) {
					// ... then the current constraint is basically useless. Explanation is: some other constraint had been added later that made an already saved constraint redundant.
					this.safeProcess.bag.remove(candidateCon);
					this.redundantConstraintsAtSecondPass.add(candidateCon);
					this.redundantConstraints.add(candidateCon);
					candidateCon.setRedundant(true);
					logger.warn(candidateCon + " is redundant (second-pass grid check)");
					}
				redundancyChecksPerformed++;
			}
		}
	}

	public void resolveConflictsRecursively(Automaton candidateAutomaton, Constraint candidateCon) {
		if (isConstraintAlreadyChecked(candidateCon)) {
			logger.trace(candidateCon + " was already checked");
			return;
		} else {
			conflictChecksPerformed++;
			blackboard.add(candidateCon);
		}

		logger.trace("Checking conflict with " + candidateCon + ": Conjuncting the safe automaton with Reg.exp: " + candidateCon.getRegularExpression());
		Automaton auxAutomaton = this.intersect(this.safeAutomaton, candidateAutomaton);
		Constraint
			relaxedCon = null;

		if (isAutomatonEmpty(auxAutomaton)) {
			logger.warn(candidateCon
					+ " conflicts with the existing safe automaton!");
//			logger.warn("Current set of safe constraints: " + this.safeProcess.bag);
			conflictingConstraints.add(candidateCon);
			candidateCon.setConflicting(true);

			relaxedCon = candidateCon.getConstraintWhichThisIsBasedUpon();
			if (relaxedCon == null) {
				relaxedCon = candidateCon.suggestConstraintWhichThisShouldBeBasedUpon();
				if (relaxedCon != null) {
					relaxedCon = candidateCon.createConstraintWhichThisShouldBeBasedUpon();
					logger.trace(relaxedCon + " included in process model as relaxation, replacing " + candidateCon);
				}
			}

			if (relaxedCon == null || relaxedCon == candidateCon) {
				logger.warn(candidateCon + " has to be removed at once");
			} else {
				logger.trace(candidateCon + " relaxed to " + relaxedCon);

				resolveConflictsRecursively(new RegExp(relaxedCon.getRegularExpression()).toAutomaton(), relaxedCon);
			}

			if (candidateCon.getSubFamily().equals(RelationConstraintSubFamily.COUPLING)) {
				MutualRelationConstraint coCandidateCon = (MutualRelationConstraint) candidateCon;
				Constraint
					forwardCon = coCandidateCon.getForwardConstraint(),
					backwardCon = coCandidateCon.getBackwardConstraint();
				
				if (forwardCon != null && backwardCon != null) {
					logger.trace("Splitting the coupling relation constraint "
							+ coCandidateCon + " into "
							+ coCandidateCon.getForwardConstraint() + " and "
							+ coCandidateCon.getBackwardConstraint());
					this.resolveConflictsRecursively(
							new RegExp(forwardCon.getRegularExpression()).toAutomaton(),
							forwardCon);
					this.resolveConflictsRecursively(
							new RegExp(backwardCon.getRegularExpression()).toAutomaton(),
							backwardCon);
				}
			}

		} else {
			safeAutomaton = auxAutomaton;
//System.out.println("PRESENTATION -- Safe automaton so far: " + safeAutomaton.toDot());
			safeProcess.bag.add(candidateCon.getBase(), candidateCon);
		}
	}

	private Automaton intersect(Automaton automaton, Automaton candidateAutomaton) {
		Automaton intersectedAutomaton = automaton.intersection(candidateAutomaton);
		
		logger.trace("Automaton states: " + intersectedAutomaton.getNumberOfStates() + "; transitions: " + intersectedAutomaton.getNumberOfTransitions());
		
		return intersectedAutomaton;
	}

	private boolean checkRedundancy(Automaton candidateAutomaton, Constraint candidateCon) {
		return checkRedundancy(this.safeAutomaton, this.safeProcess.bag, candidateAutomaton, candidateCon);
	}
	
	private boolean checkRedundancy(Automaton safeAutomaton, ConstraintsBag safeBag, Automaton candidateAutomaton, Constraint candidateCon) {
		redundancyChecksPerformed++;
		logger.trace("Checking redundancy of " + candidateCon);
		// If candidateCon is not redundant, i.e., if the language of safeAutomaton is not a subset of the language of automaton, then candidateCon can be included
		if (!safeAutomaton.subsetOf(candidateAutomaton)) {
			return true;
		} else {
			logger.warn(candidateCon + " is redundant. It is already implied" +
					(	safeBag.howManyConstraints() < ConflictAndRedundancyResolver.MAXIMUM_VISIBLE_CONSTRAINTS_FOR_REDUNDANCY_CHECK
						? " by " + LinearConstraintsIndexFactory.getAllConstraints(safeBag)
						: " by the current set of constraints."
					)
			);
			this.redundantConstraints.add(candidateCon);
			candidateCon.setRedundant(true);
			return false;
		}
	}

	private boolean isConstraintAlreadyChecked(Constraint candidateCon) {
		return blackboard.contains(candidateCon);
	}

	private boolean isAutomatonEmpty(Automaton automaton) {
		return automaton.isEmpty() || automaton.isEmptyString();
	}
	
	public Set<Constraint> getIdentifiedConflictingConstraints() {
		return this.conflictingConstraints;
	}
	
	public Set<Constraint> getIdentifiedRedundantConstraints() {
		return this.redundantConstraints;
	}
	
	public Set<Constraint> getIdentifiedRedundantConstraintsDuringSecondPass() {
		return this.redundantConstraintsAtSecondPass;
	}
	
	public Set<Constraint> getConflictingConstraintsInOriginalUnredundantModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (conflictingConstraintsInOriginalNonRedundantModel == null) {
			if (conflictingConstraints != null) {
				conflictingConstraintsInOriginalNonRedundantModel = new TreeSet<Constraint>();
				conflictingConstraintsInOriginalNonRedundantModel.addAll(this.originalHierarchyUnredundantConstraints);
				conflictingConstraintsInOriginalNonRedundantModel.retainAll(new TreeSet<Constraint>(conflictingConstraints));
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return conflictingConstraintsInOriginalNonRedundantModel;
	}

	public Set<Constraint> getConflictingConstraintsInOriginalModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (conflictingConstraintsInOriginalModel == null) {
			if (conflictingConstraints != null) {
				conflictingConstraintsInOriginalModel = new TreeSet<Constraint>();
				conflictingConstraintsInOriginalModel.addAll(this.originalProcess.bag.getAllConstraints());
				conflictingConstraintsInOriginalModel.retainAll(new TreeSet<Constraint>(conflictingConstraints));
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return conflictingConstraintsInOriginalModel;
	}

	public Set<Constraint> getRedundantConstraintsInOriginalModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (redundantConstraintsInOriginalModel == null) {
			if (redundantConstraints != null) {
				redundantConstraintsInOriginalModel = new TreeSet<Constraint>();
				redundantConstraintsInOriginalModel.addAll(this.originalProcess.bag.getAllConstraints());
				redundantConstraintsInOriginalModel.retainAll(redundantConstraints);
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return redundantConstraintsInOriginalModel;
	}

	public Set<Constraint> getRedundantConstraintsInOriginalUnredundantModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (redundantConstraintsInOriginalModel == null) {
			if (redundantConstraints != null) {
				redundantConstraintsInOriginalModel = new TreeSet<Constraint>();
				redundantConstraintsInOriginalModel.addAll(this.originalProcess.bag.getAllConstraints());
				redundantConstraintsInOriginalModel.retainAll(redundantConstraints);
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return redundantConstraintsInOriginalModel;
	}

	public ProcessModel getSafeProcess() {
		return safeProcess;
	}

	public int howManyInputConstraints() {
		return this.originalProcess.bag.howManyConstraints();
	}
	
	public int howManyInputUnredundantConstraints() {
		return this.originalHierarchyUnredundantConstraints.size();
	}
	
	public int howManyPerformedConflictChecks() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
		}
		return this.conflictChecksPerformed;
	}
	
	public int howManyPerformedRedundancyChecks() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
		}
		return this.redundancyChecksPerformed;
	}
	
	public void printComputationStats(long startTime, long finishTime) {
        StringBuffer
    	csvSummaryBuffer = new StringBuffer(),
    	csvSummaryLegendBuffer = new StringBuffer(),
    	csvSummaryComprehensiveBuffer = new StringBuffer();
        
        csvSummaryBuffer.append(ConflictAndRedundancyResolver.CONFLICT_REDUNDANCY_CHECK_CODE);
        csvSummaryLegendBuffer.append("'Operation code'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.howManyInputConstraints());
        csvSummaryLegendBuffer.append("'Input constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.howManyInputUnredundantConstraints());
        csvSummaryLegendBuffer.append("'Input constraints from hierarchy-unredundant model'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.howManyPerformedConflictChecks());
        csvSummaryLegendBuffer.append("'Performed conflict checks'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getIdentifiedConflictingConstraints().size());
        csvSummaryLegendBuffer.append("'Identified conflicting constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getConflictingConstraintsInOriginalModel().size());
        csvSummaryLegendBuffer.append("'Conflicting constraints in original model'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getConflictingConstraintsInOriginalUnredundantModel().size());
        csvSummaryLegendBuffer.append("'Conflicting constraints in original hierarchy-unredundant model'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.howManyPerformedRedundancyChecks());
        csvSummaryLegendBuffer.append("'Performed redundancy checks'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getIdentifiedRedundantConstraints().size());
        csvSummaryLegendBuffer.append("'Identified redundant constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
        // --------------------------------
        csvSummaryBuffer.append(this.getIdentifiedRedundantConstraintsDuringSecondPass().size());
        csvSummaryLegendBuffer.append("'Identified redundant constraints in second pass'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getRedundantConstraintsInOriginalModel().size());
        csvSummaryLegendBuffer.append("'Redundant constraints in original model'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getRedundantConstraintsInOriginalUnredundantModel().size());
        csvSummaryLegendBuffer.append("'Redundant constraints in original hierarchy-unredundant model'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append( secondPassStartTime > 0 ? (finishTime - secondPassStartTime) : 0);
        csvSummaryLegendBuffer.append("'Time for second redundancy check round'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(finishTime - startTime);
        csvSummaryLegendBuffer.append("'Total time to resolve conflicts and redundancies'");
//      csvSummaryBuffer.append(";");
//      csvSummaryLegendBuffer.append(";");

        csvSummaryComprehensiveBuffer.append("\n\nConflict resolution: \n");
        csvSummaryComprehensiveBuffer.append(csvSummaryLegendBuffer.toString());
        csvSummaryComprehensiveBuffer.append("\n");
        csvSummaryComprehensiveBuffer.append(csvSummaryBuffer.toString());

        logger.info(csvSummaryComprehensiveBuffer.toString());
	}
}