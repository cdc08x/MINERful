/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class Init extends Participation {
	@Override
	public String getRegularExpressionTemplate() {
		return "%1$s.*";
	}
	
    public Init(TaskChar param1, double support) {
		super(param1, support);
	}
	public Init(TaskChar param1) {
        super(param1);
    }
	public Init(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public Init(TaskCharSet param1) {
		super(param1);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new Participation(this.base);
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.POSITION;
	}
}