/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class AlternateSuccession extends Succession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*(%1$s[^%1$s^%2$s]*%2$s[^%1$s^%2$s]*)*[^%1$s^%2$s]*";
	}

    public AlternateSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        super(forwardConstraint, backwardConstraint, support);
    }

    public AlternateSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public AlternateSuccession(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public AlternateSuccession(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public AlternateSuccession(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}
	public AlternateSuccession(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
}