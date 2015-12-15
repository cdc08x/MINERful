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
public class AlternateSuccession extends Succession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*(%1$s[^%1$s^%2$s]*%2$s[^%1$s^%2$s]*)*[^%1$s^%2$s]*";
	}
	
	protected AlternateSuccession() {
		super();
	}

    public AlternateSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        super(forwardConstraint, backwardConstraint, support);
    }

    public AlternateSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public AlternateSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public AlternateSuccession(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public AlternateSuccession(TaskCharSet param1, TaskCharSet param2,
			double support) {
		super(param1, param2, support);
	}
	public AlternateSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new Succession(base, implied);
	}

	@Override
	public AlternateResponse getPlausibleForwardConstraint() {
		return new AlternateResponse(base, implied);
	}

	@Override
	public AlternatePrecedence getPlausibleBackwardConstraint() {
		return new AlternatePrecedence(base, implied);
	}
}