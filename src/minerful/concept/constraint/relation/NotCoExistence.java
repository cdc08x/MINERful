/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class NotCoExistence extends NotSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*((%1$s[^%2$s]*)|(%2$s[^%1$s]*))?";
	}

    public NotCoExistence(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public NotCoExistence(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public NotCoExistence(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public NotCoExistence(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, CoExistence.class);
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new NotSuccession(base, implied);
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
		return new CoExistence(base, implied);
	}
}
