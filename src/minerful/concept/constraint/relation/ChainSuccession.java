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
    public ChainSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public ChainSuccession(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public ChainSuccession(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public ChainSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
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
	public ChainResponse getPlausibleForwardConstraint() {
		return new ChainResponse(base, implied);
	}

	@Override
	public ChainPrecedence getPlausibleBackwardConstraint() {
		return new ChainPrecedence(base, implied);
	}
}