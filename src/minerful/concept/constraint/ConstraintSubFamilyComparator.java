package minerful.concept.constraint;

import java.util.Comparator;

import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

public class ConstraintSubFamilyComparator implements Comparator<ConstraintSubFamily> {

	@Override
	public int compare(ConstraintSubFamily o1, ConstraintSubFamily o2) {
		if (o1 instanceof RelationConstraintSubFamily && o2 instanceof RelationConstraintSubFamily)
			return ((RelationConstraintSubFamily)o1).compareTo((RelationConstraintSubFamily)o2);
		else if (o1 instanceof ExistenceConstraintSubFamily && o2 instanceof ExistenceConstraintSubFamily)
			return ((ExistenceConstraintSubFamily)o1).compareTo((ExistenceConstraintSubFamily)o2);
		else
			throw new IllegalArgumentException(
					"Uncomparable sub-families provided as arguments: " + o1.getClass() + ", " + o1.getClass());
	}

}