/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class NotChainSuccession extends NegatedRelationConstraint {
    protected RelationConstraint opposedTo;

    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s%1$s*[^%1$s^%2$s][^%1$s]*)*([^%1$s]*|%1$s)";
    }

    public NotChainSuccession(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public NotChainSuccession(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public NotChainSuccession(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}

	public NotChainSuccession(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, ChainSuccession.class);
    }
}
