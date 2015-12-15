package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class SupportFamilyConfidenceInterestFactorHierarchyLevelBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Double.valueOf(o1.support).compareTo(o2.support);
		if (result == 0) {
			result = o1.getFamily().compareTo(o2.getFamily()) * (-1);
			if (result == 0) {
				result = Double.valueOf(o1.confidence).compareTo(o2.confidence);
				if (result == 0) {
					result = Double.valueOf(o1.interestFactor).compareTo(o2.interestFactor);
					if (result == 0) {
						result = Integer.valueOf(o1.getHierarchyLevel()).compareTo(Integer.valueOf(o2.getHierarchyLevel()));
						if (result == 0) {
							result = o1.compareTo(o2);
						}
					}
				}
			}
		}
		return result * (-1);
	}
}