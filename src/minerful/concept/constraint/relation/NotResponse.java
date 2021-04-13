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
public class NotResponse extends NotChainResponse {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%2$s]*)*[^%1$s%2$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> !X(F(%2$s)))"; // G(a -> !X(F(b)))
    }
	
	protected NotResponse() {
		super();
	}

    public NotResponse(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public NotResponse(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public NotResponse(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public NotResponse(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, Response.class);
    }
    

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotChainResponse(base, implied);
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
		return new Response(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotResponse(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotResponse(taskCharSets[0], taskCharSets[1]);
	}
}