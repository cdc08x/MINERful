package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class AtMostTwo extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){0,2}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G( %1$s -> X(G( %1$s -> X(G(!%1$s)) )) )"; // G( a -> X(G( a -> X(G(!a)) )) )
    }
    
	protected AtMostTwo() {
    	super();
    }

	public AtMostTwo(TaskChar param1, double support) {
		super(param1, support);
	}
	public AtMostTwo(TaskChar param1) {
		super(param1);
	}
	public AtMostTwo(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public AtMostTwo(TaskCharSet param1) {
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

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new AtMostTwo(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AtMostTwo(taskCharSets[0]);
	}
}