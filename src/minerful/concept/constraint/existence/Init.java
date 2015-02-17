/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class Init extends ExistenceConstraint {
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
}