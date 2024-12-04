/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class NotCoExistence extends NotSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s][^%2$s]*)|([%2$s][^%1$s]*))?";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G((%1$s -> !(X(F(%2$s)) | Y(O(%2$s)))) & (%2$s -> !(X(F(%1$s)) | Y(O(%1$s)))))"; // G((a -> !(X(F(b)) | Y(O(b)))) & (b -> !(X(F(a)) | Y(O(a)))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s].*[%2$s].*)|([%2$s].*[%1$s].*)){1,}[^%1$s%2$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "G((%1$s -> (X(F(%2$s)) | Y(O(%2$s)))) & (%2$s -> (X(F(%1$s)) | Y(O(%1$s)))))"; // G((a -> (X(F(b)) | Y(O(b)))) & (b -> (X(F(a)) | Y(O(a)))))
	}

  	
	protected NotCoExistence() {
		super();
	}

    public NotCoExistence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NotCoExistence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpponent(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, CoExistence.class);
    }

	@Override
	public RelationConstraint getPossibleForwardConstraint() {
		return new NotRespondedExistence(base, implied);
	}

	@Override
	public RelationConstraint getPossibleBackwardConstraint() {
		return new NotRespondedExistence(implied, base);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotSuccession(base, implied);
	}

	@Override
	public Constraint suggestOpponentConstraint() {
		return new CoExistence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotCoExistence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotCoExistence(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new NotCoExistence(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}