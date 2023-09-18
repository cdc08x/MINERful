package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class AtLeast2 extends AtLeast1 {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){2,}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "F(%1$s & X(F(%1$s)))"; // F(a & X(F(a)))
    }
 
    protected AtLeast2() {
    	super();
    }
	public AtLeast2(TaskChar param1) {
		super(param1);
	}
	public AtLeast2(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeast1(this.base);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new AtLeast2(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new AtLeast2(taskCharSets[0]);
	}
	
	
}
