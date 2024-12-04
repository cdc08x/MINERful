/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public class End extends AtLeast1 {
	@Override
	public String getRegularExpressionTemplate() {
		return ".*[%1$s]";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(F(%1$s))"; // "G(F(a))"
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() { return ".*[^%1$s]"; }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		return "G(F(!%1$s)"; // "G(F(a))"
	}


	protected End() {
    	super();
    }

	public End(TaskChar param1) {
        super(param1);
    }
	public End(TaskCharSet param1) {
		super(param1);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeast1(this.base);
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.POSITION;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new End(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new End(taskCharSets[0]);
	}
	
	@Override
	public Constraint getSymbolic() {
		return new End(TaskChar.SYMBOLIC_TASKCHARS[0]);
	}
}