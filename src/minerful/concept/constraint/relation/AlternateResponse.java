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

    public AlternateResponse(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public AlternateResponse(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public AlternateResponse(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}
	public AlternateResponse(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
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