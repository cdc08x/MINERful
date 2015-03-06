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
            throw new IllegalArgumentException("Illegal constraints combination");
        }
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.base, forwardConstraint.implied);
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }

    
    public CoExistence(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public CoExistence(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}
	public CoExistence(TaskChar base, TaskChar implied, double support) {
		super(base, implied, support);
	}
	public CoExistence(TaskChar base, TaskChar implied) {
		super(base, implied);
	}

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public Constraint getSupposedForwardConstraint() {
		return new RespondedExistence(base, implied);
	}

	@Override
	public Constraint getSupposedBackwardConstraint() {
		return new RespondedExistence(implied, base);
	}
}