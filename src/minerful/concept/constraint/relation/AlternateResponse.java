/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class AlternateResponse extends Response {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*%2$s[^%1$s]*)*[^%1$s]*";
	}

    public AlternateResponse(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public AlternateResponse(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public AlternateResponse(TaskCharSet param1, TaskCharSet param2,
			double support) {
		super(param1, param2, support);
	}
	public AlternateResponse(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return new Response(base, implied);
	}
}