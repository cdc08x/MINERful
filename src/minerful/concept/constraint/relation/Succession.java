/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class Succession extends CoExistence {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*([%1$s].*[%2$s])*[^%1$s%2$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G((%1$s -> X(F(%2$s))) & (%2$s -> Y(O(%1$s))))"; // G((a -> X(F(b))) & (b -> Y(O(a))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
    // FIXME To be verified
	@Override
	public String getViolatingRegularExpressionTemplate() {
		//return "[^%1$s%2$s]*(([%1$s][^%2$s]*)|([^%1$s*][%2$s])){1,}[^%1$s%2$s]*";
		return "([^%1$s]*([%1$s][^%2$s]*){1,}[^%1$s%2$s]*)|(([^%1$s]*[%2$s]){1,}[^%2$s]*)";
	}

	@Override
	public String getViolatingLTLpfExpressionTemplate() {
		//return "F((%1$s & X(G(!%2$s))) | (%2$s & Z(H(!%1$s))))"; // F((a & X(G(!b))) | (b & Z(H(!a))))
		return "F(%1$s & X(G(!%2$s))) | F(%2$s & Y(H(!%1$s)))"; //F(a & X(G(!b))) | F(b & Y(H(!a)))
	}
  
  
    protected Succession() {
		super();
	}

    public Succession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public Succession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public Succession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    @Override
    protected boolean ckeckConsistency(
            RelationConstraint forwardConstraint,
            RelationConstraint backwardConstraint) {
        return super.ckeckConsistency(forwardConstraint, backwardConstraint);
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new CoExistence(base, implied);
	}

	@Override
	public Response getPossibleForwardConstraint() {
		return new Response(base, implied);
	}

	@Override
	public Precedence getPossibleBackwardConstraint() {
		return new Precedence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new Succession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new Succession(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new Succession(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}

}