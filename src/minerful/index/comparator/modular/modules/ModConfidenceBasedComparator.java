package minerful.index.comparator.modular.modules;

import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ModularConstraintsComparator;

public class ModConfidenceBasedComparator extends ModularConstraintsComparator {
	public ModConfidenceBasedComparator() {
		super();
	}

	public ModConfidenceBasedComparator(ModularConstraintsComparator secondLevelComparator) {
		super(secondLevelComparator);
	}

	
	@Override
	public int compare(Constraint o1, Constraint o2) {
		Double
			confidenceOfO1 = o1.confidence,
			confidenceOfO2 = o2.confidence;
		int result = 0;
		
		result = confidenceOfO1.compareTo(confidenceOfO2);
		
		return (
				(result == 0)
					?	super.compare(o1, o2)
					:	result * (-1)
				);
	}
}