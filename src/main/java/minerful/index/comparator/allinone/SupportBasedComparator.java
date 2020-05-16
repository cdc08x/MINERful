package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class SupportBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Double.valueOf(o1.getSupport()).compareTo(Double.valueOf(o2.getSupport()));
		return (
			(result == 0)
			?	o1.compareTo(o2)
			:	result * (-1)
		);
	}
}