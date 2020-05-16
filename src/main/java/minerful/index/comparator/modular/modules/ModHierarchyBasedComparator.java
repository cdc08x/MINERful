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
		// Sort from the most restricting one, to the least restricting one
		int result = Integer.valueOf(o1.getHierarchyLevel()).compareTo(Integer.valueOf(o2.getHierarchyLevel())) * -1;
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result
				);
	}
}