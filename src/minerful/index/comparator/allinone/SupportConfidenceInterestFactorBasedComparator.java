package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class SupportConfidenceInterestFactorBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Double.valueOf(o1.support).compareTo(o2.support);
		if (result == 0) {
			result = Double.valueOf(o1.confidence).compareTo(o2.confidence);
			if (result == 0) {
				result = Double.valueOf(o1.interestFactor).compareTo(o2.interestFactor);
				if (result == 0) {
					result = o1.compareTo(o2);
				}
			}
		}
		return result * (-1);
	}
}