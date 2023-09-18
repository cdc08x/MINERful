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
public class ChainPrecedence extends AlternatePrecedence {
    
	@Override
	public String getRegularExpressionTemplate() {
//		return "[^%2$s]*(%1$s%2$s[^%2$s]*)*[^%2$s]*";
		return "[^%1$s]*([%2$s][%1$s][^%1$s]*)*[^%1$s]*";
	}
	
	@Override
	public String getLTLpfExpressionTemplate() {
		return "G(%1$s -> Y(%2$s))"; // G(b -> Y(a))
	}

	
	protected ChainPrecedence() {
		super();
	}

    public ChainPrecedence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public ChainPrecedence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AlternatePrecedence(implied, base);
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ChainPrecedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ChainPrecedence(taskCharSets[0], taskCharSets[1]);
	}
}