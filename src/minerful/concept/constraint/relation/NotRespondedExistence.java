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
public class NotRespondedExistence extends NotSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*(([%1$s][^%2$s]*)|([%2$s][^%1$s]*))?";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> !(X(F(%2$s)) | Y(O(%2$s))))"; // G(a -> !(X(F(b)) | Y(O(b))))
    }
  	
	protected NotRespondedExistence() {
		super();
	}

    public NotRespondedExistence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NotRespondedExistence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, RespondedExistence.class);
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotSuccession(base, implied);
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
		return new RespondedExistence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotRespondedExistence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotRespondedExistence(taskCharSets[0], taskCharSets[1]);
	}
}