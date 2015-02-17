/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class Response extends RespondedExistence {
    public static final int RESPONSE_SUB_FAMILY_ID = 11;

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
    public int getSubFamily() {
        return RESPONSE_SUB_FAMILY_ID;
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
}