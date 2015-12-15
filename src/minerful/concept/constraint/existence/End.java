/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

@XmlRootElement
public class End extends Participation {
	@Override
	public String getRegularExpressionTemplate() {
		return ".*%1$s";
	}

	protected End() {
    	super();
    }

	public End(TaskChar param1) {
        super(param1);
    }
	public End(TaskChar param1, double support) {
		super(param1, support);
	}
	public End(TaskCharSet param1, double support) {
		super(param1, support);
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
		return new Participation(this.base);
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.POSITION;
	}
}