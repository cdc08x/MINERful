/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;

public class NotChainPrecedence extends NegativeRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s]*)"; // [^a]*(aa*[^ab][^a]*)*([^a]*|a*)
		return "[^%2$s]*([%2$s][%2$s]*[^%2$s%1$s][^%2$s]*)*([^%2$s]*|[%2$s]*)";
    }
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> !Y(%2$s))"; // G(b -> !Y(a))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
//		return "[^%2$s]*(%1$s%2$s[^%2$s]*)*[^%2$s]*";
//		return "[^%2$s]*([%1$s][%2$s][^%2$s]*){1,}[^%2$s]*";
		return "[^%1$s]*([%2$s][%1$s][^%1$s]*){1,}[^%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
//		return "G(%2$s -> Y(%1$s))"; // G(b -> Y(a))
		return "G(%1$s -> Y(%2$s))"; // G(b -> Y(a))
	}

    
    protected NotChainPrecedence() {
    	super();
    }

    public NotChainPrecedence(TaskChar param1, TaskChar param2) {
        super(param2, param1);
        super.reverseOrderOfParams();
    }
	public NotChainPrecedence(TaskCharSet param1, TaskCharSet param2) {
		super(param2, param1);
        super.reverseOrderOfParams();
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, ChainPrecedence.class);
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public Constraint suggestOpponentConstraint() {
		return new ChainPrecedence(implied, base);
	}

	@Override
    public ConstraintImplicationVerse getImplicationVerse() {
        return ConstraintImplicationVerse.BACKWARD;
    }

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotChainPrecedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotChainPrecedence(taskCharSets[0], taskCharSets[1]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new NotChainPrecedence(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}