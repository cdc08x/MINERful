/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import java.util.Collections;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;

public class Precedence extends RespondedExistence {  
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%2$s.*%1$s)*[^%1$s]*";
    }
    
    @Override
	public String getRegularExpression() {
		// TODO Auto-generated method stub
		return super.getRegularExpression();
	}



	public Precedence(TaskChar param1, TaskChar param2) {
        super(param2, param1);
        this.invertOrderOfParams();
    }
	public Precedence(TaskChar param1, TaskChar param2, double support) {
		super(param2, param1, support);
		this.invertOrderOfParams();
	}
    public Precedence(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param2, param1, support);
		this.invertOrderOfParams();
	}
	public Precedence(TaskCharSet param1, TaskCharSet param2) {
		super(param2, param1);
		this.invertOrderOfParams();
	}
	
	protected void invertOrderOfParams() {
		Collections.reverse(this.parameters);
	}

	@Override
    public ConstraintImplicationVerse getImplicationVerse() {
    	return ConstraintImplicationVerse.BACKWARD;
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

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new RespondedExistence(base, implied);
	}
}