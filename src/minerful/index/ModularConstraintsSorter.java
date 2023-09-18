package minerful.index;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.index.comparator.modular.ModularConstraintsComparatorFactory;

public class ModularConstraintsSorter {
	public static boolean DEFAULT_LAST_LEVEL_COMPARATOR_IS_RANDOM = false;
	private Collection<Constraint> constraints;
	private ModularConstraintsComparatorFactory factory;
	
	public ModularConstraintsSorter() {
	}
	
	public ModularConstraintsSorter(Collection<Constraint> constraints) {
		this.setConstraints(constraints);
	}
	
	public void setConstraints(Collection<Constraint> constraints) {
		this.constraints = constraints;
		this.factory = new ModularConstraintsComparatorFactory(this.constraints);
	}

	public SortedSet<Constraint> sort(ConstraintSortingPolicy... policies) {
		return this.sort(DEFAULT_LAST_LEVEL_COMPARATOR_IS_RANDOM, policies);
	}

	public SortedSet<Constraint> sort(boolean lastLevelComparatorIsRandom, ConstraintSortingPolicy... policies) {
		if (this.constraints == null) {
			throw new IllegalStateException("Constraints not already set for sorting");
		}

		SortedSet<Constraint> sortedConstraints = null;

		Comparator<? super Constraint> cnsCompa =  factory.createModularComparator(lastLevelComparatorIsRandom, policies);
		sortedConstraints = new TreeSet<Constraint>(cnsCompa);
		sortedConstraints.addAll(this.constraints);

		return sortedConstraints;
	}

	public Comparator<? super Constraint> getComparator(ConstraintSortingPolicy... policies) {
		return this.getComparator(DEFAULT_LAST_LEVEL_COMPARATOR_IS_RANDOM);
	}

	public Comparator<? super Constraint> getComparator(boolean lastLevelComparatorIsRandom, ConstraintSortingPolicy... policies) {
		return factory.createModularComparator(lastLevelComparatorIsRandom, policies);
	}
}