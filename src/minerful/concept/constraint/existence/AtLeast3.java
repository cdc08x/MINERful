package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class AtLeast3 extends AtLeast2 {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){3,}[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "F(%1$s & X(F(%1$s & X(F(%1$s)))))"; // F(a & X(F(a & X(F(a)))))
    }
 
    protected AtLeast3() {
    	super();
    }
	
	public AtLeast3(TaskChar param1) {
		super(param1);
	}
	public AtLeast3(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AtLeast2(this.base);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new AtLeast3(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new AtLeast3(taskCharSets[0]);
	}
	
	
}
