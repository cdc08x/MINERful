package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class SupportFamilyConfidenceCoverageHierarchyLevelBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Double.valueOf(o1.getEventBasedMeasures().getSupport()).compareTo(o2.getEventBasedMeasures().getSupport());
		if (result == 0) {
			result = o1.getFamily().compareTo(o2.getFamily()) * (-1);
			if (result == 0) {
				result = Double.valueOf(o1.getEventBasedMeasures().getConfidence()).compareTo(o2.getEventBasedMeasures().getConfidence());
				if (result == 0) {
					result = Double.valueOf(o1.getEventBasedMeasures().getCoverage()).compareTo(o2.getEventBasedMeasures().getCoverage());
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