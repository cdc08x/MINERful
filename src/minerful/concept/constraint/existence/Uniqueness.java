package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class Uniqueness extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){0,1}[^%1$s]*";
	}

	public Uniqueness(TaskChar base, double support) {
		super(base, support);
	}
	public Uniqueness(TaskChar base) {
		super(base);
	}
	public Uniqueness(TaskCharSet base, double support) {
		super(base, support);
	}
	public Uniqueness(TaskCharSet base) {
		super(base);
	}
}