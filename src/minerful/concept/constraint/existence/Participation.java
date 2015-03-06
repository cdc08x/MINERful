package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class Participation extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){1,}[^%1$s]*";
	}

	public Participation(TaskChar base, double support) {
		super(base, support);
	}
	public Participation(TaskChar base) {
		super(base);
	}
	public Participation(TaskCharSet base, double support) {
		super(base, support);
	}
	public Participation(TaskCharSet base) {
		super(base);
	}

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}
}
