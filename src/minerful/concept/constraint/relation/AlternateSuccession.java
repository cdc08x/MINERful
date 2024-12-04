/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class AlternateSuccession extends Succession {
	@Override
	public String getRegularExpressionTemplate() {
//		return "[^%1$s%2$s]*([%1$s][^%1$s%2$s]*[%2$s][^%1$s%2$s]*)*[^%1$s%2$s]*";
		return "[^%2$s%1$s]*([%2$s][^%2$s%1$s]*[%1$s][^%2$s%1$s]*)*[^%2$s%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	//return "G((%1$s -> X(!%1$s U %2$s)) & (%2$s -> Y(!%2$s S %1$s)))"; // G((a -> X(!a U b)) & (b -> Y(!b S a)))
		return "G((%2$s -> X(!%2$s U %1$s)) & (%1$s -> Y(!%1$s S %2$s)))";
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
//		return "[^%1$s%2$s]*([%1$s][^%1$s%2$s]*([%1$s]|[%2$s])*[^%1$s%2$s]*[%2$s]){1,}[^%1$s%2$s]*";
		return "[^%2$s%1$s]*([%2$s][^%2$s%1$s]*([%2$s]|[%1$s])*[^%2$s%1$s]*[%1$s]){1,}[^%2$s%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
	//	return "G((%1$s -> !X(%2$s)) & (%2$s -> !Y(%1$s)))"; // G((a -> !X(b)) & (b -> !Y(a)))
		return "G((%2$s -> !X(%1$s)) & (%1$s -> !Y(%2$s)))"; // G((a -> !X(b)) & (b -> !Y(a)))
	}

	
	protected AlternateSuccession() {
		super();
	}

    public AlternateSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public AlternateSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public AlternateSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new Succession(base, implied);
	}

	@Override
	public AlternateResponse getPossibleForwardConstraint() {
		return new AlternateResponse(base, implied);
	}

	@Override
	public AlternatePrecedence getPossibleBackwardConstraint() {
		return new AlternatePrecedence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new AlternateSuccession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AlternateSuccession(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new AlternateSuccession(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}