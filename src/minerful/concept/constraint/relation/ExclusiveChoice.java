/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class ExclusiveChoice extends MutualRelationConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "([^%2$s]*[%1$s][^%2$s]*) | ([^%1$s]*[%2$s][^%1$s]*)"; //  ([^b]*[a][^b]*) | ([^a]*[b][^a]*)
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G( (F(%1$s) | O(%1$s)) <-> !(F(%2$s) | O(%2$s)) )"; // G( (F(a) | O(a)) <-> !(F(b) | O(b)) )
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeRegularExpressionTemplate() {
		return "([^%2$s]*[%1$s][^%2$s]*)|([^%1$s]*[%2$s][^%1$s]*)";
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getNegativeLTLpfExpressionTemplate() {
		// return "F( a | b )"; // F( a | b )
		return "G( (F(%1$s) | O(%1$s)) <-> (F(%2$s) | O(%2$s)) )"; // G( (F(a) | O(a)) <-> (F(b) | O(b)) )
	}

  	
	protected ExclusiveChoice() {
		super();
	}

    public ExclusiveChoice(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public ExclusiveChoice(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotCoExistence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ExclusiveChoice(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ExclusiveChoice(taskCharSets[0], taskCharSets[1]);
	}
	
	
	@Override
	public Constraint getSymbolic() {
		return new ExclusiveChoice(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}