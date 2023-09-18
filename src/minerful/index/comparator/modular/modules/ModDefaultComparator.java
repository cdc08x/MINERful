package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModDefaultComparator extends ModularConstraintsComparator {
	public ModDefaultComparator() {
		super();
	}

	public ModDefaultComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = o1.compareTo(o2);
		
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result
				);
	}
}