package minerful.index.comparator.modular;

import minerful.index.SortingPolicy;

/**
 * Specifies the order in which constraints are sorted when it comes to scan them one by one.
 * @author Claudio Di Ciccio
 */
public enum ConstraintSortingPolicy implements SortingPolicy {
	/** Support, confidence level, and interest factor, in descending order */
	SUPPORTCONFIDENCEINTERESTFACTOR,
	/** Family (existence constraints first, then relation constraints, ...), and then position in the subsumption hierarchy (e.g., ChainPrecedence first, then AlternatePrecedence) */
	FAMILYHIERARCHY,
	/** Descending number of connected tasks by means of relation constraints */
	ACTIVATIONTARGETBONDS,
	/** Default, i.e., based on the compareTo() method of the constraints under analysis */
	DEFAULT,
	/** Random sorting */
	RANDOM
}