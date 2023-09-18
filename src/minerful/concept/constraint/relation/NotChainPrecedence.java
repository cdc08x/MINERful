/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;

@XmlRootElement
public class NotChainPrecedence extends NegativeRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s]*)";
    }
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%2$s -> !Y(%1$s))"; // G(b -> !Y(a))
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
	public Constraint getSupposedOpponentConstraint() {
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
}