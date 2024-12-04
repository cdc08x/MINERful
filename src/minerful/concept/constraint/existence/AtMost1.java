package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class AtMost1 extends AtMost2 {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){0,1}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> X(G(!%1$s)))"; // G(a -> X(G(!a)))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){2,}[^%1$s]*";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "F(%1$s & X(F(%1$s)))"; // F(a & X(F(a)))
	}

    
	protected AtMost1() {
    	super();
    }

	public AtMost1(TaskChar param1) {
		super(param1);
	}
	public AtMost1(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtMost2(base);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new AtMost1(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AtMost1(taskCharSets[0]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new AtMost1(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}