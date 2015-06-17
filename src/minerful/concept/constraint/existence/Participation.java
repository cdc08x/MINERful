package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class Participation extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){1,}[^%1$s]*";
	}

	public Participation(TaskChar param1, double support) {
		super(param1, support);
	}
	public Participation(TaskChar param1) {
		super(param1);
	}
	public Participation(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public Participation(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint getConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}
}
