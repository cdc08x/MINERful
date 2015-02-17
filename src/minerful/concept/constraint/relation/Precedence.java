/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class Precedence extends RespondedExistence {  
    
    public static final int PRECEDENCE_SUB_FAMILY_ID = 12;
    
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%2$s]*(%1$s.*%2$s)*[^%2$s]*";
    }
    
    public Precedence(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
	public Precedence(TaskChar base, TaskChar implied, double support) {
		super(base, implied, support);
	}
    public Precedence(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public Precedence(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}
	
	@Override
    public ImplicationVerse getImplicationVerse() {
    	return ImplicationVerse.BACKWARD;
    }

	@Override
    public int getSubFamily() {
        return PRECEDENCE_SUB_FAMILY_ID;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    @Override
    public Integer getMaximumExpectedDistance() {
    	if (this.isExpectedDistanceConfidenceIntervalProvided())
    		return (int)Math.min(-1, StrictMath.round(expectedDistance + confidenceIntervalMargin));
    	return null;
    }
}