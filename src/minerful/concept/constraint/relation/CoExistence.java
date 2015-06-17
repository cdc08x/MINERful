/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class CoExistence extends CouplingRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*((%1$s.*%2$s.*)|(%2$s.*%1$s.*))*[^%1$s^%2$s]*";
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        this(forwardConstraint.base, forwardConstraint.implied, support);
        if (!this.ckeckConsistency(forwardConstraint, backwardConstraint)) {
            throw new IllegalArgumentException("Illegal constraints combination: provided " + forwardConstraint + " and " + backwardConstraint + " resp. as forward and backward constraints of " + this);
        }
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.base, forwardConstraint.implied);
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }

    
    public CoExistence(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public CoExistence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}
	public CoExistence(TaskChar param1, TaskChar param2, double support) {
		super(param1, param2, support);
	}
	public CoExistence(TaskChar param1, TaskChar param2) {
		super(param1, param2);
	}

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public RespondedExistence getPlausibleForwardConstraint() {
		return new RespondedExistence(base, implied);
	}

	@Override
	public RespondedExistence getPlausibleBackwardConstraint() {
		return new RespondedExistence(implied, base);
	}
}