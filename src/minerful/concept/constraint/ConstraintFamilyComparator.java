package minerful.concept.constraint;

import java.util.Comparator;

public class ConstraintFamilyComparator implements Comparator<ConstraintFamily> {

	@Override
	public int compare(ConstraintFamily o1, ConstraintFamily o2) {
		return o1.compareTo(o2);
	}

}
