package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.RelationConstraint;

public class InterestConfidenceBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		Double 	interestOfO1 = o1.getInterestFactor(),
				interestOfO2 = o2.getInterestFactor();
		int		result = 0;
		
		result = interestOfO1.compareTo(interestOfO2);
		
		if (result == 0) {
			interestOfO1 = o1.getConfidence();
			interestOfO2 = o2.getConfidence();
			result = interestOfO1.compareTo(interestOfO2);
		}
		
		return (
			(result == 0)
			?	o1.compareTo(o2)
			:	result * (-1)
		);
	}
}