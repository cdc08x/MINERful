/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class Succession extends CoExistence {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*(%1$s.*%2$s)*[^%1$s^%2$s]*";
	}

    public Succession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public Succession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        super(forwardConstraint, backwardConstraint, support);
    }

    public Succession(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public Succession(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public Succession(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public Succession(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    @Override
    protected boolean ckeckConsistency(
            RespondedExistence forwardConstraint,
            RespondedExistence backwardConstraint) {
        return      forwardConstraint.base.equals(backwardConstraint.base)
                &&  forwardConstraint.implied.equals(backwardConstraint.implied)
                &&  this.getHierarchyLevel() == forwardConstraint.getHierarchyLevel()
                &&  this.getHierarchyLevel() == backwardConstraint.getHierarchyLevel();
    }
	
	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new CoExistence(base, implied);
	}

	@Override
	public Constraint getSupposedForwardConstraint() {
		return new Response(base, implied);
	}

	@Override
	public Constraint getSupposedBackwardConstraint() {
		return new Precedence(base, implied);
	}
}