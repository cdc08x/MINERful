/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class End extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return ".*%1$s";
	}

	public End(TaskChar base) {
        super(base);
    }
	public End(TaskChar base, double support) {
		super(base, support);
	}
	public End(TaskCharSet base, double support) {
		super(base, support);
	}
	public End(TaskCharSet base) {
		super(base);
	}
}