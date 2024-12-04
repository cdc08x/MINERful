package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class AtMost3 extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){0,3}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> X(G(%1$s -> X(G(%1$s -> X(G(!%1$s)))))))"; // G(a -> X(G(a -> X(G( a -> X(G(!a)))))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){4,}[^%1$s]*";
	} // this expression is equivalent to a non-existing AtLeast4 Constraint

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "F(%1$s & X(F(%1$s & X(F(%1$s & X(F(%1$s)))))))";
	}

    
	protected AtMost3() {
    	super();
    }

	public AtMost3(TaskChar param1) {
		super(param1);
	}
	public AtMost3(TaskCharSet param1) {
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
		return new AtMost3(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AtMost3(taskCharSets[0]);
	}
	@Override
	public Constraint getSymbolic() {
		return new AtMost3(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}