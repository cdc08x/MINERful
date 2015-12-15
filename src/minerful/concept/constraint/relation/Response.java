/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;

@XmlRootElement
public class Response extends RespondedExistence {

    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*(%1$s.*%2$s)*[^%1$s]*";
		// [^a]*(a.*b)*[^a]*
    }
    
    protected Response() {
    	super();
    }

    public Response(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public Response(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public Response(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public Response(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public ConstraintImplicationVerse getImplicationVerse() {
        return ConstraintImplicationVerse.FORWARD;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    @Override
    public Integer getMinimumExpectedDistance() {
    	if (this.isExpectedDistanceConfidenceIntervalProvided())
    		return (int)Math.max(1, StrictMath.round(expectedDistance - confidenceIntervalMargin));
    	return null;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new RespondedExistence(base, implied);
	}
}