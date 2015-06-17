package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class AtMostOne extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){0,1}[^%1$s]*";
	}

	public AtMostOne(TaskChar param1, double support) {
		super(param1, support);
	}
	public AtMostOne(TaskChar param1) {
		super(param1);
	}
	public AtMostOne(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public AtMostOne(TaskCharSet param1) {
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