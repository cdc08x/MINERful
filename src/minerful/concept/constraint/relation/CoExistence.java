/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public class CoExistence extends RelationConstraint {
    public static final int CO_FAMILY_ID = 2;
    
    @Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s^%2$s]*((%1$s.*%2$s.*)|(%2$s.*%1$s.*))*[^%1$s^%2$s]*";
    }
    
    protected RespondedExistence forwardConstraint;
    protected RespondedExistence backwardConstraint;
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        this(forwardConstraint.base, forwardConstraint.implied, support);
        if (!this.ckeckConsistency(forwardConstraint, backwardConstraint)) {
            throw new IllegalArgumentException("Illegal constraints combination");
        }
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }
    
    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.base, forwardConstraint.implied);
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }

    
    public CoExistence(TaskCharSet base, TaskCharSet implied, double support) {
		super(base, implied, support);
	}
	public CoExistence(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}
	public CoExistence(TaskChar base, TaskChar implied, double support) {
		super(base, implied, support);
	}
	public CoExistence(TaskChar base, TaskChar implied) {
		super(base, implied);
	}
    
	@Override
    public ImplicationVerse getImplicationVerse() {
    	return ImplicationVerse.BOTH;
    }

	@Override
    public int getFamily() {
        return CO_FAMILY_ID;
    }

    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }

    public RespondedExistence getForwardConstraint() {
        return forwardConstraint;
    }

    public RespondedExistence getBackwardConstraint() {
        return backwardConstraint;
    }
    
    public void setImplyingConstraints(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
		this.forwardConstraint = forwardConstraint;
		this.backwardConstraint = backwardConstraint;
	}

	public boolean isMoreReliableThanTheImplyingConstraints() {
        return  this.support >= forwardConstraint.support &&
                this.support >= backwardConstraint.support;
    }

    protected boolean ckeckConsistency(
            RespondedExistence forwardConstraint,
            RespondedExistence backwardConstraint) {
        return      forwardConstraint.base.equals(backwardConstraint.implied)
                &&  forwardConstraint.implied.equals(backwardConstraint.base)
                &&  this.getHierarchyLevel() == forwardConstraint.getHierarchyLevel()
                &&  this.getHierarchyLevel() == backwardConstraint.getHierarchyLevel();
    }

    public boolean hasImplyingConstraints() {
        return  this.forwardConstraint != null &&
                this.backwardConstraint != null;
    }
}