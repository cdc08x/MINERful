package minerful.index.comparator.allinone;

import java.util.Comparator;

import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.RelationConstraint;

public class CoverageConfidenceBasedComparator implements Comparator<Constraint> {
	@Override
	public int compare(Constraint o1, Constraint o2) {
		Double 	coverageOfO1 = o1.getEventBasedMeasures().getCoverage(),
				coverageOfO2 = o2.getEventBasedMeasures().getCoverage();
		int		result = 0;
		
		result = coverageOfO1.compareTo(coverageOfO2);
		
		if (result == 0) {
			coverageOfO1 = o1.getEventBasedMeasures().getConfidence();
			coverageOfO2 = o2.getEventBasedMeasures().getConfidence();
			result = coverageOfO1.compareTo(coverageOfO2);
		}
		
		return (
			(result == 0)
			?	o1.compareTo(o2)
			:	result * (-1)
		);
	}
}