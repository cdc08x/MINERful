package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class SupportConfidenceCoverageBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Double.valueOf(o1.getEventBasedMeasures().getSupport()).compareTo(o2.getEventBasedMeasures().getSupport());
		if (result == 0) {
			result = Double.valueOf(o1.getEventBasedMeasures().getConfidence()).compareTo(o2.getEventBasedMeasures().getConfidence());
			if (result == 0) {
				result = Double.valueOf(o1.getEventBasedMeasures().getCoverage()).compareTo(o2.getEventBasedMeasures().getCoverage());
				if (result == 0) {
					result = o1.compareTo(o2);
				}
			}
		}
		return result * (-1);
	}
}