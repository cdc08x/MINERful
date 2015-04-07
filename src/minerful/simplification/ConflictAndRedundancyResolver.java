package minerful.simplification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.CouplingRelationConstraint;
import minerful.index.LinearConstraintsIndexFactory;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class ConflictAndRedundancyResolver {
	public static final boolean DEFAULT_BEHAVIOUR_FOR_REDUNDANCY_CHECK = true;
	private ProcessModel safeProcess;
	private boolean checking;
	private final boolean avoidingRedundancy;

	private Automaton safeAutomaton;

	private Map<Constraint, Boolean> blackboard;

	private static Logger logger = Logger.getLogger(ConflictAndRedundancyResolver.class
			.getCanonicalName());
	
	private Set<Constraint>
		safeNonTotallySupportedConstraints,
		originalNonRedundantConstraints,
		notSurelySafeProcessConstraints,
		conflictingConstraintsInOriginalNonRedundantModel,
		conflictingConstraintsInOriginalModel,
		conflictingConstraints,
		redundantConstraints,
		redundantConstraintsInOriginalNonRedundantModel,
		redundantConstraintsInOriginalModel;
	private int conflictChecksPerformed,
		redundancyChecksPerformed;

	public ConflictAndRedundancyResolver(ProcessModel process) {
		this(process, DEFAULT_BEHAVIOUR_FOR_REDUNDANCY_CHECK);
	}
	
	public ConflictAndRedundancyResolver(ProcessModel process, boolean avoidingRedundancy) {
		this.avoidingRedundancy = avoidingRedundancy;

		this.checking = false;
		this.conflictChecksPerformed = 0;
		this.redundancyChecksPerformed = 0;
		this.conflictingConstraints = new TreeSet<Constraint>();
		this.safeNonTotallySupportedConstraints = new TreeSet<Constraint>();
		this.redundantConstraints = new TreeSet<Constraint>();
		this.originalNonRedundantConstraints =
				LinearConstraintsIndexFactory.getAllConstraints(
						process.bag.createHierarchyUnredundantCopy()
				);
		/*
		 * The blackboard is meant to associate to all constraints a tick,
		 * whenever the constraint has already been checked
		 */
		this.blackboard = new HashMap<Constraint, Boolean>(process.bag.howManyConstraints());
		TaskCharRelatedConstraintsBag safeBag = process.bag
				.createCopyPrunedByThreshold(Constraint.MAX_SUPPORT);
		
		/*
		 * Heuristic 1: Consider as safe those constraints that have a support
		 * of 100%: if they have a support of 100%, a model already exists for
		 * them: the log itself. So, their conjunction cannot be unsatisfiable.
		 */
		if (avoidingRedundancy) {
			TaskCharRelatedConstraintsBag
				emptyBag = safeBag.createEmptyIndexedCopy();
			this.safeProcess = new ProcessModel(emptyBag);
			Automaton candidateAutomaton = null;
			
			this.safeAutomaton = this.safeProcess.buildAlphabetAcceptingAutomaton();
			
			Automaton safeAutomatonFirstPass = this.safeProcess.buildAlphabetAcceptingAutomaton();
			TaskCharRelatedConstraintsBag safeBagFirstPass = safeBag.createEmptyIndexedCopy();
			
			// First pass: order constraints by their number of connections (for the sake of computation), then their support, family, confidence interest factor
			for (Constraint candidateCon : LinearConstraintsIndexFactory.getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(safeBag)) {
				logger.trace("Checking redundancy of " + candidateCon);
				candidateAutomaton = new RegExp(candidateCon.getRegularExpression()).toAutomaton();
				if (!this.isConstraintAlreadyChecked(candidateCon) && this.checkRedundancy(safeAutomatonFirstPass, safeBagFirstPass, candidateAutomaton, candidateCon)) {
					safeAutomatonFirstPass = this.intersect(safeAutomatonFirstPass, candidateAutomaton);
					safeBagFirstPass.add(candidateCon.base, candidateCon);
				}
				blackboard.put(candidateCon, true);
			}

			// Second pass: order constraints by their support, then family, confidence interest factor
			for (Constraint candidateCon : LinearConstraintsIndexFactory.getAllConstraintsSortedBySupportFamilyConfidenceInterestFactorHierarchyLevel(safeBagFirstPass)) {
				logger.trace("Checking redundancy of " + candidateCon + " for the second time");
				candidateAutomaton = new RegExp(candidateCon.getRegularExpression()).toAutomaton();
				if (this.checkRedundancy(candidateAutomaton, candidateCon)) {
					this.safeAutomaton = this.intersect(this.safeAutomaton, candidateAutomaton);
					safeProcess.bag.add(candidateCon.base, candidateCon);
				}
			}
		} else {
			this.safeProcess = new ProcessModel(safeBag.createHierarchyUnredundantCopy());
			this.safeAutomaton = this.safeProcess.buildAutomaton();
			for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(safeBag)) {
				blackboard.put(c, true);
			}
		}

		TaskCharRelatedConstraintsBag unsafeBag = process.bag
				.createComplementOfCopyPrunedByThreshold(Constraint.MAX_SUPPORT);
		for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(unsafeBag)) {
			blackboard.put(c, false);
		}
		
		this.notSurelySafeProcessConstraints = LinearConstraintsIndexFactory
				.getAllConstraintsSortedBySupportFamilyConfidenceInterestFactorHierarchyLevel(unsafeBag);
	}

	private Automaton intersect(Automaton automaton, Automaton candidateAutomaton) {
		Automaton intersectedAutomaton = automaton.intersection(candidateAutomaton);
		
		logger.trace("Automaton states: " + intersectedAutomaton.getNumberOfStates() + "; transitions: " + intersectedAutomaton.getNumberOfTransitions());
		
		return intersectedAutomaton;
	}

	private boolean checkRedundancy(Automaton candidateAutomaton, Constraint candidateCon) {
		return checkRedundancy(this.safeAutomaton, this.safeProcess.bag, candidateAutomaton, candidateCon);
	}
	
	private boolean checkRedundancy(Automaton safeAutomaton, TaskCharRelatedConstraintsBag safeBag, Automaton candidateAutomaton, Constraint candidateCon) {
		redundancyChecksPerformed++;
		// If candidateCon is not redundant, i.e., if the language of safeAutomaton is not a subset of the language of automaton, then candidateCon can be included
		if (!safeAutomaton.subsetOf(candidateAutomaton)) {
			return true;
		} else {
			logger.trace("Ignoring " + candidateCon + " because it is already implied" +
					(safeBag.howManyConstraints() < 25 ? " by " + LinearConstraintsIndexFactory.getAllConstraints(safeBag) : ""));
			this.redundantConstraints.add(candidateCon);
			return false;
		}
	}

	public void resolveConflicts() {
		this.checking = true;
		Automaton candidateAutomaton = null;
		for (Constraint candidateCon : this.notSurelySafeProcessConstraints) {
			logger.trace("Checking consistency of " + candidateCon);
			if (!isConstraintAlreadyChecked(candidateCon)) {
				candidateAutomaton = new RegExp(candidateCon.getRegularExpression()).toAutomaton();
				if (!this.avoidingRedundancy || this.checkRedundancy(candidateAutomaton, candidateCon))
					resolveConflictsRecursively(candidateAutomaton, candidateCon);
			}
		}

		safeProcess.bag = safeProcess.bag.createHierarchyUnredundantCopy();
		this.checking = false;
	}

	public void resolveConflictsRecursively(Automaton candidateAutomaton, Constraint candidateCon) {
		if (isConstraintAlreadyChecked(candidateCon)) {
			logger.info(candidateCon + " was already checked");
			return;
		} else {
			conflictChecksPerformed++;
			blackboard.put(candidateCon, true);
		}

		logger.trace("Conjuncting the safe automaton with " + candidateCon);
		Automaton auxAutomaton = this.intersect(this.safeAutomaton, candidateAutomaton);
		Constraint relaxedCon = null;

		if (isAutomatonEmpty(auxAutomaton)) {
			logger.warn(candidateCon
					+ " conflicts with the existing safe automaton!");
			conflictingConstraints.add(candidateCon);

			relaxedCon = candidateCon.getConstraintWhichThisIsBasedUpon();

			if (relaxedCon == null || relaxedCon == candidateCon) {
				logger.warn(candidateCon + " has to be removed at once");
			} else {
				logger.trace(candidateCon + " relaxed to " + relaxedCon);

				resolveConflictsRecursively(new RegExp(relaxedCon.getRegularExpression()).toAutomaton(), relaxedCon);
			}

			if (candidateCon.getFamily().equals(ConstraintFamily.COUPLING)) {
				CouplingRelationConstraint coCandidateCon = (CouplingRelationConstraint) candidateCon;
				Constraint
					forwardCon = coCandidateCon.getForwardConstraint(),
					backwardCon = coCandidateCon.getBackwardConstraint();

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

		} else {
			safeAutomaton = auxAutomaton;
			safeProcess.bag.add(candidateCon.base, candidateCon);
		}
	}

	private boolean isConstraintAlreadyChecked(Constraint candidateCon) {
		return blackboard.containsKey(candidateCon) && blackboard.get(candidateCon).booleanValue() == true;
	}

	private boolean isAutomatonEmpty(Automaton automaton) {
		return automaton.isEmpty() || automaton.isEmptyString();
	}
	
	public Set<Constraint> checkedConflictingConstraints() {
		return this.conflictingConstraints;
	}
	
	public Set<Constraint> checkedRedundantConstraints() {
		return this.redundantConstraints;
	}
	
	public Set<Constraint> conflictingConstraintsInOriginalUnredundantModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (conflictingConstraintsInOriginalNonRedundantModel == null) {
			if (conflictingConstraints != null) {
				conflictingConstraintsInOriginalNonRedundantModel = new TreeSet<Constraint>();
				conflictingConstraintsInOriginalNonRedundantModel.addAll(this.originalNonRedundantConstraints);
				conflictingConstraintsInOriginalNonRedundantModel.retainAll(conflictingConstraints);
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return conflictingConstraintsInOriginalNonRedundantModel;
	}

	public Set<Constraint> conflictingConstraintsInOriginalModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (conflictingConstraintsInOriginalModel == null) {
			if (conflictingConstraints != null) {
				conflictingConstraintsInOriginalModel = new TreeSet<Constraint>();
				conflictingConstraintsInOriginalModel.addAll(this.blackboard.keySet());
				conflictingConstraintsInOriginalModel.retainAll(conflictingConstraints);
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return conflictingConstraintsInOriginalModel;
	}
	
	public Set<Constraint> redundantConstraintsInOriginalUnredundantModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (redundantConstraintsInOriginalNonRedundantModel == null) {
			if (redundantConstraints != null) {
				redundantConstraintsInOriginalNonRedundantModel = new TreeSet<Constraint>();
				redundantConstraintsInOriginalNonRedundantModel.addAll(this.originalNonRedundantConstraints);
				redundantConstraintsInOriginalNonRedundantModel.retainAll(redundantConstraints);
			} else {
				throw new IllegalStateException("Conflict check not yet performed");
			}
		}
		return redundantConstraintsInOriginalNonRedundantModel;
	}

	public Set<Constraint> redundantConstraintsInOriginalModel() {
		if (checking == true) {
			throw new IllegalStateException("Check in progress");
		}
		if (redundantConstraintsInOriginalModel == null) {
			if (redundantConstraints != null) {
				redundantConstraintsInOriginalModel = new TreeSet<Constraint>();
				redundantConstraintsInOriginalModel.addAll(this.blackboard.keySet());
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

	public Set<Constraint> inputConstraints() {
		return this.blackboard.keySet();
	}
	
	public int performedConflictChecks() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
		}
		return this.conflictChecksPerformed;
	}
	
	public int performedRedundancyChecks() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
		}
		return this.redundancyChecksPerformed;
	}
	
	public int getRedundancyChecksPerformed() {
		return redundancyChecksPerformed;
	}
}