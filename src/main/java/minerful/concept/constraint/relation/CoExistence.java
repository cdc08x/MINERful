/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

@XmlRootElement
public class CoExistence extends MutualRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s].*[%2$s].*)|([%2$s].*[%1$s].*))*[^%1$s%2$s]*";
    }
    
    protected CoExistence() {
    	super();
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        this(forwardConstraint.getBase(), forwardConstraint.getImplied(), support);
        if (!this.ckeckConsistency(forwardConstraint, backwardConstraint)) {
            throw new IllegalArgumentException("Illegal constraints combination: provided " + forwardConstraint + " and " + backwardConstraint + " resp. as forward and backward constraints of " + this);
        }
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.getBase(), forwardConstraint.getImplied());
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
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public RespondedExistence getPossibleForwardConstraint() {
		return new RespondedExistence(base, implied);
	}

	@Override
	public RespondedExistence getPossibleBackwardConstraint() {
		return new RespondedExistence(implied, base);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new CoExistence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new CoExistence(taskCharSets[0], taskCharSets[1]);
	}
}