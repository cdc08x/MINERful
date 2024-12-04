package minerful.concept.constraint.existence;


import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class AtMost2 extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){0,2}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> X(G(%1$s -> X(G(!%1$s)))))"; // G(a -> X(G( a -> X(G(!a)))))
    }
    
	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){3,}[^%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "F(%1$s & X(F(%1$s & X(F(%1$s)))))"; // F(a & X(F(a & X(F(a)))))
	}

	protected AtMost2() {
    	super();
    }

	public AtMost2(TaskChar param1) {
		super(param1);
	}
	public AtMost2(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtMost3(base);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
	
	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new AtMost2(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AtMost2(taskCharSets[0]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new AtMost2(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}