/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class Init extends Participation {
	@Override
	public String getRegularExpressionTemplate() {
		return "%1$s.*";
	}
	
    public Init(TaskChar base, double support) {
		super(base, support);
	}
	public Init(TaskChar base) {
        super(base);
    }
	public Init(TaskCharSet base, double support) {
		super(base, support);
	}
	public Init(TaskCharSet base) {
		super(base);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new Participation(this.base);
	}
}