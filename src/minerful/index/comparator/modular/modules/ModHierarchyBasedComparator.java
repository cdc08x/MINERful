package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModHierarchyBasedComparator extends ModularConstraintsComparator {
	public ModHierarchyBasedComparator() {
		super();
	}

	public ModHierarchyBasedComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Integer.valueOf(o1.getHierarchyLevel()).compareTo(Integer.valueOf(o1.getHierarchyLevel()));
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result
				);
	}
}