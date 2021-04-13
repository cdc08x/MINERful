package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class AtLeastTwo extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){2,}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "F(%1$s & X(F(%1$s)))"; // F(a & X(F(a)))
    }
 
    protected AtLeastTwo() {
    	super();
    }
	
	public AtLeastTwo(TaskChar param1, double support) {
		super(param1, support);
	}

	public AtLeastTwo(TaskChar param1) {
		super(param1);
	}

	public AtLeastTwo(TaskCharSet param1, double support) {
		super(param1, support);
	}

	public AtLeastTwo(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeastOne(this.base);
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new AtLeastTwo(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new AtLeastTwo(taskCharSets[0]);
	}
	
	
}
