/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;

public class NotChainSuccession extends NegativeMutualRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
		//return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s]*)";
		return "[^%2$s]*([%2$s][%2$s]*[^%2$s%1$s][^%2$s]*)*([^%2$s]*|[%2$s]*)";
    }
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	//return "G((%1$s -> !X(%2$s)) & (%2$s -> !Y(%1$s)))"; // G((a -> !X(b)) & (b -> !Y(a)))
		return "G((%2$s -> !X(%1$s)) & (%1$s -> !Y(%2$s)))";
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
//		return "[^%1$s%2$s]*([%1$s][%2$s][^%1$s%2$s]*){1,}[^%1$s%2$s]*";
		return "[^%2$s%1$s]*([%2$s][%1$s][^%2$s%1$s]*){1,}[^%2$s%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
//		return "G((%1$s -> X(%2$s)) & (%2$s -> Y(%1$s)))"; // G((a -> X(b)) & (b -> Y(a)))
		return "G((%2$s -> X(%1$s)) & (%1$s -> Y(%2$s)))"; // G((a -> X(b)) & (b -> Y(a)))
	}

    
    protected NotChainSuccession() {
    	super();
    }

    public NotChainSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NotChainSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
	@Override
    public void setOpponent(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, ChainSuccession.class);
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public RelationConstraint getPossibleForwardConstraint() {
		return new NotChainResponse(base, implied);
	}

	@Override
	public RelationConstraint getPossibleBackwardConstraint() {
		return new NotChainPrecedence(base, implied);
	}

	@Override
	public Constraint suggestOpponentConstraint() {
		return new ChainSuccession(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotChainSuccession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotChainSuccession(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new NotChainSuccession(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}