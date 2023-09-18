package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModCoverageBasedComparator extends ModularConstraintsComparator {
	public ModCoverageBasedComparator() {
		super();
	}

	public ModCoverageBasedComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	@Override
	public int compare(Constraint o1, Constraint o2) {
		Double
			interestOfO1 = o1.getEventBasedMeasures().getCoverage(),
			interestOfO2 = o2.getEventBasedMeasures().getCoverage();
		int result = 0;
		
		result = interestOfO1.compareTo(interestOfO2);
		
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result * (-1)
				);
	}
}