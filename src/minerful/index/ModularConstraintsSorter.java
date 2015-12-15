package minerful.index;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.CnsSortModularDefaultPolicy;
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

	public SortedSet<Constraint> sort(CnsSortModularDefaultPolicy... policies) {
		return this.sort(DEFAULT_LAST_LEVEL_COMPARATOR_IS_RANDOM, policies);
	}

	public SortedSet<Constraint> sort(boolean lastLevelComparatorIsRandom, CnsSortModularDefaultPolicy... policies) {
		if (this.constraints == null) {
			throw new IllegalStateException("Constraints not already set for sorting");
		}

		SortedSet<Constraint> sortedConstraints = null;

		Comparator<? super Constraint> cnsCompa =  factory.createModularComparator(lastLevelComparatorIsRandom, policies);
		sortedConstraints = new TreeSet<Constraint>(cnsCompa);
		sortedConstraints.addAll(this.constraints);

		return sortedConstraints;
	}

	public Comparator<? super Constraint> getComparator(CnsSortModularDefaultPolicy... policies) {
		return this.getComparator(DEFAULT_LAST_LEVEL_COMPARATOR_IS_RANDOM);
	}

	public Comparator<? super Constraint> getComparator(boolean lastLevelComparatorIsRandom, CnsSortModularDefaultPolicy... policies) {
		return factory.createModularComparator(lastLevelComparatorIsRandom, policies);
	}
}