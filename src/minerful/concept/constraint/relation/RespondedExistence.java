/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;

public class RespondedExistence extends RelationConstraint {
    public static final String DISTANCE_PRINT_TEMPLATE = " <%+d \u00F7 %+d> ";

    public Double expectedDistance;
    public Double confidenceIntervalMargin;
    
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*((%1$s.*%2$s.*)|(%2$s.*%1$s.*))*[^%1$s]*";
    }

    public RespondedExistence(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public RespondedExistence(TaskChar pivot, TaskChar searched, double support) {
        super(pivot, searched, support);
    }
    public RespondedExistence(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}
	public RespondedExistence(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    public boolean isExpectedDistanceConfidenceIntervalProvided() {
    	return expectedDistance != null && confidenceIntervalMargin != null;
    }
    
    @Override
    public ImplicationVerse getImplicationVerse() {
    	return ImplicationVerse.FORWARD;
    }
    
    @Override
    public String toString() {
    	if (isExpectedDistanceConfidenceIntervalProvided()) {
    		return super.toString().replace(", ", printDistances());
    	}
    	return super.toString();
    }
    
    protected String printDistances() {
    	return String.format(RespondedExistence.DISTANCE_PRINT_TEMPLATE,
    			getMinimumExpectedDistance(),
    			getMaximumExpectedDistance());
    }
    
    public Integer getMinimumExpectedDistance() {
    	if (this.isExpectedDistanceConfidenceIntervalProvided())
    		return (int)StrictMath.round(expectedDistance - confidenceIntervalMargin);
    	return null;
    }
    
    public Integer getMaximumExpectedDistance() {
    	if (this.isExpectedDistanceConfidenceIntervalProvided())
    		return (int)StrictMath.round(expectedDistance + confidenceIntervalMargin);
    	return null;
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}
}