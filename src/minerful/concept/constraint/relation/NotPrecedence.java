/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

@XmlRootElement
public class NotPrecedence extends NotChainPrecedence {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%2$s]*)*[^%1$s%2$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%2$s -> !Y(H(%1$s)))"; // G(b -> !Y(H(a)))
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
	public Constraint getSupposedOpponentConstraint() {
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
}