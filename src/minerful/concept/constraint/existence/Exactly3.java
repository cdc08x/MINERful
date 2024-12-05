package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class Exactly3 extends AtLeast2 { // Multiple inheritance is not allowed in Java, but there should be AtMostTwo too, here
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){3,3}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
//    	return "G( ( F(%1$s & X(F(%1$s))) | O(%1$s & Y(O(%1$s))) ) & ( %1$s -> X(G( %1$s -> X(G(!%1$s)) )) ) )";
    	return "!%1$s U (%1$s & X(!%1$s U (%1$s & X(!%1$s U (%1$s & X(G(!%1$s)))))))"; // !a U (a & X(!a U (a & X(!a U (a & X(G(!a)))))))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "([^%1$s]*([%1$s][^%1$s]*){4,}[^%1$s]*)|([^%1$s]*([%1$s][^%1$s]*){0,2}[^%1$s]*)";
	}
	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
//    	return "G( ( F(%1$s & X(F(%1$s))) | O(%1$s & Y(O(%1$s))) ) & ( %1$s -> X(G( %1$s -> X(G(!%1$s)) )) ) )";
		return "%1$s U (!%1$s & X(%1$s U (!%1$s & X(%1$s U (!%1$s & X(G(%1$s)))))))"; // !a U (a & X(!a U (a & X(!a U (a & X(G(!a)))))))
	}


	protected Exactly3() {
    	super();
    }

	public Exactly3(TaskChar param1) {
		super(param1);
	}
	public Exactly3(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeast3(getBase());
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new Exactly3(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new Exactly3(taskCharSets[0]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new Exactly3(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}