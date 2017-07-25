package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;

public class HierarchyBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		int result = Integer.valueOf(o1.getHierarchyLevel()).compareTo(Integer.valueOf(o1.getHierarchyLevel()));
		return (
				(result == 0)
				?	o1.compareTo(o2)
						:	result * (-1)
				);
	}
}