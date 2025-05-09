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
		return "[^%1$s%2$s]*([%1$s][%2$s][^%1$s%2$s]*)*[^%1$s%2$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G((%1$s -> X(%2$s)) & (%2$s -> Y(%1$s)))"; // G((a -> X(b)) & (b -> Y(a)))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
    // FIXME To be verified
	@Override
	public String getViolatingRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
//		return "[^%1$s%2$s]*(([%1$s][^%2$s]*){1,})|(([^%1$s]*[%2$s]){1,})([^%1$s%2$s]*|([%1$s]|[%2$s])*)";
		//return "[^%1$s^%2$s]*(([%1$s^%2$s])|([^%1$s%2$s])){1,}[^%1$s^%2$s]"; // TODO test this one
		return "([^%1$s]*(([%1$s][^%2$s]*)|([%1$s][^%2$s][^%1$s]*)){1,})|((([^%1$s]*[%2$s]){1,}[^%2$s]*)|([^%1$s][%2$s][^%2$s]*){1,})";
	}

	@Override
	public String getViolatingLTLpfExpressionTemplate() {
		//return "F((%1$s & !X(%2$s)) | (%2$s & !Y(%1$s)))"; // G((a & !X(b)) | (b & !Y(a)))
		return "F(%1$s & !X(%2$s)) | F(%2$s & !Y(%1$s))"; //F(a & !X(b)) | F(b & !Y(a))
	}
	
	
    protected ChainSuccession() {
		super();
	}

    public ChainSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public ChainSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public ChainSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AlternateSuccession(base, implied);
	}

	@Override
	public ChainResponse getPossibleForwardConstraint() {
		return new ChainResponse(base, implied);
	}

	@Override
	public ChainPrecedence getPossibleBackwardConstraint() {
		return new ChainPrecedence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ChainSuccession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ChainSuccession(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new ChainSuccession(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}