/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;

public class Response extends RespondedExistence {

    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s.*%2$s)*[^%1$s]*";
		// [^a]*(a.*b)*[^a]*
    }

    public Response(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public Response(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public Response(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public Response(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public ConstraintSubFamily getSubFamily() {
        return ConstraintSubFamily.RESPONSE;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    @Override
    public Integer getMinimumExpectedDistance() {
    	if (this.isExpectedDistanceConfidenceIntervalProvided())
    		return (int)Math.max(1, StrictMath.round(expectedDistance - confidenceIntervalMargin));
    	return null;
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new RespondedExistence(base, implied);
	}
}