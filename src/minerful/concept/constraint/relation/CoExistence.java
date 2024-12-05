/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class CoExistence extends MutualRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s].*[%2$s].*)|([%2$s].*[%1$s].*))*[^%1$s%2$s]*";
    }
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G((%1$s -> (X(F(%2$s)) | Y(O(%2$s)))) & (%2$s -> (X(F(%1$s)) | Y(O(%1$s)))))"; // G((a -> (X(F(b)) | Y(O(b)))) & (b -> (X(F(a)) | Y(O(a)))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s][^%2$s]*)|([%2$s][^%1$s]*))?";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "G((%1$s -> !(X(F(%2$s)) | Y(O(%2$s)))) & (%2$s -> !(X(F(%1$s)) | Y(O(%1$s)))))"; // G((a -> !(X(F(b)) | Y(O(b)))) & (b -> !(X(F(a)) | Y(O(a)))))
	}
    
    protected CoExistence() {
    	super();
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.getBase(), forwardConstraint.getImplied());
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }

	public CoExistence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
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
	
	@Override
	public Constraint getSymbolic() {
		return new CoExistence(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}