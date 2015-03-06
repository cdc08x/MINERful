/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class ChainSuccession extends AlternateSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*(%1$s%2$s[^%1$s^%2$s]*)*[^%1$s^%2$s]*";
	}

    public ChainSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public ChainSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        super(forwardConstraint, backwardConstraint, support);
    }
    public ChainSuccession(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public ChainSuccession(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public ChainSuccession(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public ChainSuccession(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new AlternateSuccession(base, implied);
	}

	@Override
	public Constraint getSupposedForwardConstraint() {
		return new ChainResponse(base, implied);
	}

	@Override
	public Constraint getSupposedBackwardConstraint() {
		return new ChainPrecedence(base, implied);
	}
}