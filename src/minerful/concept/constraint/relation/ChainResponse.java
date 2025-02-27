/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public class ChainResponse extends AlternateResponse {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][%2$s][^%1$s]*)*[^%1$s]*";
	}
    
    @Override
    public String getLTLpfExpressionTemplate() {
    	return "G(%1$s -> X(%2$s))"; // G(a -> X(b))
    }

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getViolatingRegularExpressionTemplate() {
//		return "[^%1$s]*([%1$s][%1$s]*[^%1$s%2$s][^%1$s]*)*([^%1$s]*|[%1$s])";
		//return "[^%1$s]*([%1$s][%2$s][^%1$s]*)*([%1$s][^%2$s]){1,}([^%1$s]*|[%1$s]*)";
		//[^a]*([a][^b]){1,}[^a]*
		return "[^%1$s]*(([%1$s][^%2$s]*)|([%1$s][^%2$s][^%1$s]*)){1,}";

	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	@Override
	public String getViolatingLTLpfExpressionTemplate() {
		//return "G(%1$s -> !X(%2$s))"; // G(a -> !X(b))
		return "F(%1$s & !X(%2$s))"; //F(a & !X(b))
	}
 	
 	
	protected ChainResponse() {
		super();
	}

    public ChainResponse(TaskChar param1, TaskChar 	param2) {
        super(param1, param2);
    }
	public ChainResponse(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AlternateResponse(base, implied);
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ChainResponse(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ChainResponse(taskCharSets[0], taskCharSets[1]);
	}
	
	
	@Override
	public Constraint getSymbolic() {
		return new ChainResponse(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
	}
}