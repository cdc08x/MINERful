package minerful.index;

import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;

import org.apache.log4j.Logger;

public class ConstraintIndexHassePruner extends ConstraintIndexHasseManager {
	private static Logger logger = Logger.getLogger(ProcessModel.class.getCanonicalName());
	
	private final boolean forOutBrancing;
	
	public ConstraintIndexHassePruner(boolean isForOutBrancing, ConstraintIndexHasseDiagram hasseDiagram) {
		super(hasseDiagram);
		this.forOutBrancing = isForOutBrancing;
	}

	public void prune() {
		this.labelRedundancyWrtSetContainment();
	}

	public Set<? extends Constraint> nonRedundantConstraints() {
		Set<Constraint> nonRedundantConstraints = new TreeSet<Constraint>();
		
		for (ConstraintIndexHasseNode firstLevelChild : this.hasseDiagram.root.children.values()) {
			nonRedundantConstraints(nonRedundantConstraints, firstLevelChild);
		}

		return nonRedundantConstraints;
	}
	
	private void nonRedundantConstraints(Set<Constraint> constraintsToUpdate, ConstraintIndexHasseNode nodeToExplore) {
		for (Constraint c : nodeToExplore.constraints.values()) {
			if (!c.isRedundant())
				constraintsToUpdate.add(c);
		}
		for (ConstraintIndexHasseNode child : nodeToExplore.children.values()) {
			nonRedundantConstraints(constraintsToUpdate, child);
		}
		return;
	}

	private void labelRedundancyWrtSetContainment() {
		if (this.forOutBrancing) {
			/*
			 *  E.g.,
			 *  support ( Response(a, {b, c}) ) <= support ( Response(a, {b, c, d}) )
			 */
			for (Class<? extends Constraint> conClass : MetaConstraintUtils.getAllDiscoverableForwardRelationConstraintTemplates()) {
				for (ConstraintIndexHasseNode sinkChild : this.hasseDiagram.getSinkNodes()) {
					this.labelRedundancyWrtSetContainment(
							sinkChild,
							conClass,
							NavDirection.UP);
				}
			}
			/*
			 *  E.g.,
			 *  support ( Precedence(a, {b, c}) ) >= support ( Precedence(a, {b, c, d}) )
			 */
//			for (Class<? extends Constraint> conClass : MetaConstraintUtils.getAllPossibleBackwardsRelationConstraintTemplates()) {
//				for (ConstraintIndexHasseNode firstChild : this.hasseDiagram.root.children.values()) {
//					this.labelRedundancyWrtSetContainment(
//						firstChild,
//						conClass,
//						NavDirection.DOWN);
//				}
//			}
		}
		else {
			/*
			 *  E.g.,
			 *  support ( Response({a, b}, d) ) >= support ( Response({a, b, c}, d}) )
			 */
//			for (Class<? extends Constraint> conClass : MetaConstraintUtils.getAllPossibleOnwardsRelationConstraintTemplates()) {
//				for (ConstraintIndexHasseNode firstChild : this.hasseDiagram.root.children.values()) {
//					this.labelRedundancyWrtSetContainment(
//						firstChild,
//						conClass,
//						NavDirection.DOWN);
//				}
//			}
			/*
			 *  E.g.,
			 *  support ( Precedence({a, b}, d) ) <= support ( Precedence({a, b, c}, d}) )
			 */
			for (Class<? extends Constraint> conClass : MetaConstraintUtils.getAllDiscoverableBackwardRelationConstraintTemplates()) {
				for (ConstraintIndexHasseNode sinkChild : this.hasseDiagram.getSinkNodes()) {
					this.labelRedundancyWrtSetContainment(
							sinkChild,
							conClass,
							NavDirection.UP);
				}
			}
		}
		/*
		 *  Negative relation constraints behave always the same:
		 *  e.g.,
		 *  support ( NotCoExistence(a, {b, c}) ) >= support ( NotCoExistence(a, {b, c, d}) )
		 *  as well as
		 *  support ( NotCoExistence({a, b}, d) ) >= support ( NotCoExistence({a, b, c}, d}) )
		 */
		
//		for (Class<? extends Constraint> conClass : MetaConstraintUtils.getAllPossibleNegativeRelationConstraintTemplates()) {
//			for (ConstraintIndexHasseNode firstChild : this.hasseDiagram.root.children.values()) {
//				this.labelRedundancyWrtSetContainment(
//					firstChild,
//					conClass,
//					NavDirection.DOWN);
//			}
//		}
	}
	
	private void labelRedundancyWrtSetContainment(
			ConstraintIndexHasseNode nodeUnderAnalysis,
			Class<? extends Constraint> conClass,
			NavDirection explorationDirection) {
		/*
		 ********************************
		 * Policy: maximize support &
		 * IncreasingAlongHierarchy: true
		 * (branching on target, i.e., forward-target & out-branching OR backward-target & in-branching)
		 * =>
		 * explorationDirection: UP,
		 * from: sink
		 ******************************** 
		 * =>
		 * Start from sink.
		 * Search for parents and uncles.
		 * If this node is associated to a parent or an uncle sharing the same support (at most, the parent/uncle's is lower),
		 * label this as redundant and proceed with that parent/uncle.
		 * Otherwise, label that parent/uncle and all ancestors as redundant, then return.
		 ********************************
		 ********************************
		 * Policy: maximize support &
		 * IncreasingAlongHierarchy: false
		 * (branching on target, i.e., forward-target & out-branching OR backward-target & in-branching
		 * +
		 * negative relation constraints)
		 * =>
		 * explorationDirection: DOWN,
		 * from: root's children
		 ******************************** 
		 * =>
		 * Start from root's children.
		 * Search for children nodes.
		 * For each of them, if their support is equal to all the uncles and parents (it cannot be higher),
		 * mark this and all uncles/parents as redundant, the proceed with that child.
		 * Otherwise, mark the child and all descendants as redundant, then return.
		 */
		
		Constraint currentConstraint = null;
		currentConstraint = nodeUnderAnalysis.constraints.get(conClass);
		
		if (currentConstraint == null)
			return;

		Constraint
			parentOrUncleConstraint = null,
			childConstraint = null;

		switch (explorationDirection) {
		case UP:
			for (ConstraintIndexHasseNode parentOrUncle : nodeUnderAnalysis.getParentAndUncles()) {
				if (!parentOrUncle.equals(this.hasseDiagram.root)) {
					parentOrUncleConstraint = parentOrUncle.constraints.get(conClass);
					if (currentConstraint.getSupport() > parentOrUncleConstraint.getSupport()) {
						logger.trace(currentConstraint + " has a support, " + currentConstraint.getSupport() + ", which is higher than his parent/uncle " + parentOrUncleConstraint + "'s one, " + parentOrUncleConstraint.getSupport() + " -> labeling " + parentOrUncleConstraint + " and its ancestors as redundant");
						if (!parentOrUncleConstraint.isRedundant()) {
							parentOrUncleConstraint.setRedundant(true);
							propagateRedundancyLabel(parentOrUncle, conClass, explorationDirection);
						}
					} else {
						logger.trace(currentConstraint + " has a support, " + currentConstraint.getSupport() + ", which is equal to or lower than his parent/uncle " + parentOrUncleConstraint + "'s one, " + parentOrUncleConstraint.getSupport() + " -> labeling this as redundant");
						currentConstraint.setRedundant(true);
						if (!parentOrUncleConstraint.isRedundant()) {
							labelRedundancyWrtSetContainment(
									parentOrUncle,
									conClass,
									explorationDirection
							);
						}
					}
				}
			}
			return;
		case DOWN:
			for (ConstraintIndexHasseNode child : nodeUnderAnalysis.children.values()) {
				for (ConstraintIndexHasseNode childParentOrUncle : child.getParentAndUncles()) {
					childConstraint = child.constraints.get(conClass);
					parentOrUncleConstraint = childParentOrUncle.constraints.get(conClass);
					if (parentOrUncleConstraint.getSupport() > childConstraint.getSupport()) {
						logger.trace(parentOrUncleConstraint + " has a support, " + parentOrUncleConstraint.getSupport() + ", which is higher than his child " + childConstraint + "'s one, " + childConstraint.getSupport() + " -> labeling " + childConstraint + " as redundant");
						childConstraint.setRedundant(true);
					}
				}
				if (childConstraint.isRedundant()) {
					logger.trace("At least a parent/uncle of " + childConstraint + " has a higher support -> labeling " + childConstraint + "' descendants as redundant");
					propagateRedundancyLabel(child, conClass, explorationDirection);
				} else {
					labelRedundancyWrtSetContainment(
							child,
							conClass,
							explorationDirection
					);
				}
			}
			return;
		default:
			break;
		}
	}

	private void propagateRedundancyLabel(
			ConstraintIndexHasseNode nodeUnderAnalysis,
			Class<? extends Constraint> conClass,
			NavDirection explorationDirection) {

		if (nodeUnderAnalysis.equals(this.hasseDiagram.root))
			return;

		switch (explorationDirection) {
		case UP:
			for (ConstraintIndexHasseNode parentOrUncle : nodeUnderAnalysis.getParentAndUncles()) {
				if (!parentOrUncle.equals(this.hasseDiagram.root)) {
					if (!parentOrUncle.constraints.get(conClass).isRedundant()) {
						logger.trace("Labeling " + parentOrUncle.constraints.get(conClass) + ", parent/uncle of " + nodeUnderAnalysis.constraints.get(conClass) + ", as redundant");
						parentOrUncle.constraints.get(conClass).setRedundant(true);
						propagateRedundancyLabel(parentOrUncle, conClass, explorationDirection);
					}
				}
			}
			break;
		case DOWN:
			for (ConstraintIndexHasseNode child : nodeUnderAnalysis.children.values()) {
				if (!child.constraints.get(conClass).isRedundant()) {
					logger.trace("Labeling " + child.constraints.get(conClass) + ", child of " + nodeUnderAnalysis.constraints.get(conClass) + ", as redundant");
					child.constraints.get(conClass).setRedundant(true);
					propagateRedundancyLabel(child, conClass, explorationDirection);
				}
			}
			break;
		default:
			break;
		}
		return;
	}
}