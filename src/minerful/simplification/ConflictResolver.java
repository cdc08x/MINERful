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

public class ConflictResolver {
	private ProcessModel safeProcess;
	private boolean checking;

	private Automaton safeAutomaton;

	private Map<Constraint, Boolean> blackboard;

	private static Logger logger = Logger.getLogger(ConflictResolver.class
			.getCanonicalName());
	
	private Set<Constraint> originalNonRedundantConstraints,
		conflictingConstraintsInOriginalNonRedundantModel,
		notSurelySafeProcessConstraints,
		conflictingConstraints,
		conflictingConstraintsInOriginalModel;
	private int checksPerformed;

	public ConflictResolver(ProcessModel process) {
		this.checking = false;
		this.checksPerformed = 0;
		this.conflictingConstraints = new TreeSet<Constraint>();
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
		this.safeProcess = new ProcessModel(safeBag
				.createHierarchyUnredundantCopy());
		this.safeAutomaton = this.safeProcess.buildAutomaton();
		for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(safeBag)) {
			blackboard.put(c, true);
		}

		TaskCharRelatedConstraintsBag unsafeBag = process.bag
				.createComplementOfCopyPrunedByThreshold(Constraint.MAX_SUPPORT);
		for (Constraint c : LinearConstraintsIndexFactory.getAllConstraints(unsafeBag)) {
			blackboard.put(c, false);
		}
		
		this.notSurelySafeProcessConstraints = LinearConstraintsIndexFactory
				.getAllConstraintsSortedBySupportConfidenceInterestFactor(process.bag
						.createComplementOfCopyPrunedByThreshold(
								Constraint.MAX_SUPPORT)
						.createHierarchyUnredundantCopy());
	}

	public void resolveConflicts() {
		this.checking = true;
		for (Constraint candidateCon : this.notSurelySafeProcessConstraints) {
			resolveConflictsRecursively(candidateCon);
		}

		safeProcess.bag = safeProcess.bag.createHierarchyUnredundantCopy();
		this.checking = false;
	}

	public void resolveConflictsRecursively(Constraint candidateCon) {
		if (blackboard.containsKey(candidateCon) && blackboard.get(candidateCon).booleanValue() == true) {
			logger.trace(candidateCon + " was already checked");
			return;
		} else {
			checksPerformed++;
			blackboard.put(candidateCon, true);
		}

		logger.trace("Conjuncting the safe automaton with " + candidateCon);
		Automaton auxAutomaton = this.safeAutomaton.intersection(new RegExp(
				candidateCon.getRegularExpression()).toAutomaton());
		Constraint relaxedCon = null;

		if (isAutomatonEmpty(auxAutomaton)) {
			logger.warn(candidateCon
					+ " conflicts with the existing safe automaton!");
			conflictingConstraints.add(candidateCon);

			if (candidateCon.getFamily().equals(ConstraintFamily.CO_FAMILY_ID)) {
				CouplingRelationConstraint coCandidateCon = (CouplingRelationConstraint) candidateCon;

				logger.trace("Splitting the coupling relation constraint "
						+ coCandidateCon + " into "
						+ coCandidateCon.getForwardConstraint() + " and "
						+ coCandidateCon.getBackwardConstraint());
				this.resolveConflictsRecursively(coCandidateCon
						.getForwardConstraint());
				this.resolveConflictsRecursively(coCandidateCon
						.getBackwardConstraint());
			}

			relaxedCon = candidateCon.getConstraintWhichThisIsBasedUpon();

			if (relaxedCon == null || relaxedCon == candidateCon) {
				logger.warn(candidateCon + " has to be removed at once");
			} else {
				logger.trace(candidateCon + " relaxed to " + relaxedCon);

				resolveConflictsRecursively(relaxedCon);
			}
		} else {
			safeAutomaton = auxAutomaton;
			safeProcess.bag.add(candidateCon.base, candidateCon);
		}
	}

	private boolean isAutomatonEmpty(Automaton automaton) {
		return automaton.isEmpty() || automaton.isEmptyString();
	}
	
	public Set<Constraint> checkedConflictingConstraints() {
		return this.conflictingConstraints;
	}
	
	public Set<Constraint> conflictingConstraintsInOriginalUnredundantModel() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
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
			throw new IllegalStateException("Conflict check in progress");
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

	public ProcessModel getSafeProcess() {
		return safeProcess;
	}

	public Set<Constraint> inputConstraints() {
		return this.blackboard.keySet();
	}
	
	public int performedChecks() {
		if (checking == true) {
			throw new IllegalStateException("Conflict check in progress");
		}
		return this.checksPerformed;
	}
}