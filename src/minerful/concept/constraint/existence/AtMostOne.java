package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class AtMostOne extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s[^%1$s]*){0,1}[^%1$s]*";
	}

	protected AtMostOne() {
    	super();
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
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}
}