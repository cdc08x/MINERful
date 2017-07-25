package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintSubFamilyComparator;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModFamilyBasedComparator extends ModularConstraintsComparator {
	private ConstraintSubFamilyComparator subFamilyComparator = new ConstraintSubFamilyComparator();
	
	public ModFamilyBasedComparator() {
		super();
	}

	public ModFamilyBasedComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = o1.getFamily().compareTo(o2.getFamily());
		if (result == 0) {
			result = subFamilyComparator.compare(o1.getSubFamily(),o2.getSubFamily());
			if (result == 0) {
				return super.compare(o1, o2);
			}
		}
		return result;
	}
}