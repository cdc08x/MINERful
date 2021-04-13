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
public class NotChainSuccession extends NegativeRelationConstraint {
    @Override
	public String getRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s]*)";
    }
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G((%1$s -> !X(%2$s)) & (%2$s -> !Y(%1$s)))"; // G((a -> !X(b)) & (b -> !Y(a)))
    }
    
    protected NotChainSuccession() {
    	super();
    }

    public NotChainSuccession(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public NotChainSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public NotChainSuccession(TaskCharSet param1, TaskCharSet param2,
			double support) {
		super(param1, param2, support);
	}

	public NotChainSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, ChainSuccession.class);
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
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
}