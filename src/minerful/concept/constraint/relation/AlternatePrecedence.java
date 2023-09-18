/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

@XmlRootElement
public class AlternatePrecedence extends Precedence {
	@Override
	public String getRegularExpressionTemplate() {
//		return "[^%2$s]*(%1$s[^%2$s]*%2$s[^%2$s]*)*[^%2$s]*";
//		return "[^%1$s]*([%2$s][^%1$s]*[%1$s])*[^%1$s]*";
		return "[^%1$s]*([%2$s][^%1$s]*[%1$s][^%1$s]*)*[^%1$s]*";
	}
	   
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> Y(!%1$s S %2$s))"; // G(b -> Y(!b S a))
    }
	
	protected AlternatePrecedence() {
		super();
	}

    public AlternatePrecedence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public AlternatePrecedence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new Precedence(implied, base);
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new AlternatePrecedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AlternatePrecedence(taskCharSets[0], taskCharSets[1]);
	}
}