/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class NotRespondedExistence extends NotResponse { // TODO This kind of inheritance is partial. See below (suggestConstraint... method)
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s][^%2$s]*)|([%2$s][^%1$s]*))?";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> !(X(F(%2$s)) | Y(O(%2$s))))"; // G(a -> !(X(F(b)) | Y(O(b))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s]*(([%1$s].*[%2$s].*)|([%2$s].*[%1$s].*)){1,}[^%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "G(%1$s -> (X(F(%2$s)) | Y(O(%2$s)))"; // G(a -> (X(F(b)) | Y(O(b)))
	}
  	
	protected NotRespondedExistence() {
		super();
	}

    public NotRespondedExistence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NotRespondedExistence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpponent(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, RespondedExistence.class);
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		// FIXME Actually, NotRespondedExistence entails NotPrecedence(implied, base) too. The current web of methods does not allow it.
		return new NotResponse(base, implied);
	}

	@Override
	public Constraint suggestOpponentConstraint() {
		return new RespondedExistence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotRespondedExistence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotRespondedExistence(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new NotRespondedExistence(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}