/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class NotPrecedence extends NotChainPrecedence {
	@Override
	public String getRegularExpressionTemplate() {
		//return "([%2$s].*[%1$s])*[^%1$s]*"; // ([^a].*[b])*[^b]*
		return "[^%2$s]*([%2$s][^%1$s]*)*[^%2$s%1$s]*"; 
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> !Y(H(%2$s)))"; // G(b -> !Y(H(a)))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
//		return "[^%2$s]*([%1$s].*[%2$s]){1,}[^%2$s]*"; // [^b]*([a].*[b])*[^b]*
		return "[^%1$s]*([%2$s].*[%1$s]){1,}[^%1$s]*"; // [^b]*([a].*[b])*[^b]*

	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "G(%1$s -> Y(O(%2$s)))"; // G(b -> Y(O(a)))
	}
	
	
	protected NotPrecedence() {
		super();
	}

    public NotPrecedence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NotPrecedence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, Precedence.class);
    }
    

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotChainPrecedence(implied, base);
	}

	@Override
	public Constraint suggestOpponentConstraint() {
		return new Precedence(implied, base);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotPrecedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotPrecedence(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new NotPrecedence(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}

}