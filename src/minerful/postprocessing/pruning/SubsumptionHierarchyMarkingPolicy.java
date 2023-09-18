package minerful.postprocessing.pruning;

public enum SubsumptionHierarchyMarkingPolicy {
	/*
	 * Privileges the hierarchy (eager policy):
	 * for example, if the model contains AlternatePrecedence(A, B) and Precedence(A, B),
	 * the latter is pruned out.
	 */
	EAGER_ON_HIERARCHY_OVER_SUPPORT,
	/*
	 * Privileges the support (eager policy):
	 * for example, if the model contains AlternatePrecedence(A, B) and Precedence(A, B),
	 * and AlternatePrecedence(A, B) has a support of 0.89
	 * whereas Precedence(A, B) has a support of 0.9,
	 * then AlternatePrecedence(A, B) is pruned out.
	 */
	EAGER_ON_SUPPORT_OVER_HIERARCHY,
	/*
	 * Prunes only subsuming constraints, and only if ALL the subsuming ones in the whole hierarchy have the same support
	 */
	CONSERVATIVE
}