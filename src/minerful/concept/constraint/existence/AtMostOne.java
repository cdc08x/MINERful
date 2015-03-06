package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class AtMostOne extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){0,1}[^%1$s]*";
	}

	public AtMostOne(TaskChar base, double support) {
		super(base, support);
	}
	public AtMostOne(TaskChar base) {
		super(base);
	}
	public AtMostOne(TaskCharSet base, double support) {
		super(base, support);
	}
	public AtMostOne(TaskCharSet base) {
		super(base);
	}
}