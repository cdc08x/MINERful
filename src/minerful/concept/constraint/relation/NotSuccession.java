/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class NotSuccession extends NotChainSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%2$s]*)*[^%1$s^%2$s]*";
	}

    public NotSuccession(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public NotSuccession(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public NotSuccession(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public NotSuccession(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, Succession.class);
    }
    

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new NotChainSuccession(base, implied);
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
		return new Succession(base, implied);
	}
}