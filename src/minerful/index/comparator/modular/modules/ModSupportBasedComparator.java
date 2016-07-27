package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModSupportBasedComparator extends ModularConstraintsComparator {
	public ModSupportBasedComparator() {
		super();
	}

	public ModSupportBasedComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	@Override
	public int compare(Constraint o1, Constraint o2) {
		Double
			supportOfO1 = o1.getSupport(),
			supportOfO2 = o2.getSupport();
		int result = 0;
		
		result = supportOfO1.compareTo(supportOfO2);
		
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result * (-1)
				);
	}
}