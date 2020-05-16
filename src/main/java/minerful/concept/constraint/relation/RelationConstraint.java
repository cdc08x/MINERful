/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

@XmlType
@XmlSeeAlso({MutualRelationConstraint.class,NegativeRelationConstraint.class,UnidirectionalRelationConstraint.class})
public abstract class RelationConstraint extends Constraint {
	public static enum ImplicationVerse {
		FORWARD,
		BACKWARD,
		BOTH
	}
	@XmlIDREF
//	@XmlTransient
	protected TaskCharSet implied;
	
	protected RelationConstraint() {
		super();
//		implied = null;
	}

    public RelationConstraint(TaskCharSet param1, TaskCharSet param2, double support) {
        super(support, param1, param2);
        super.setSilentToObservers(true);
        this.implied = param2;
        super.setSilentToObservers(false);
    }
    public RelationConstraint(TaskCharSet param1, TaskCharSet param2) {
        super(param1, param2);
        super.setSilentToObservers(true);
        this.implied = param2;
        super.setSilentToObservers(false);
    }
    public RelationConstraint(TaskChar param1, TaskChar param2, double support) {
    	super(support, param1, param2);
        super.setSilentToObservers(true);
        this.implied = new TaskCharSet(param2);
        super.setSilentToObservers(false);
    }
    public RelationConstraint(TaskChar param1, TaskChar param2) {
    	super(param1, param2);
        super.setSilentToObservers(true);
        this.implied = new TaskCharSet(param2);
        super.setSilentToObservers(false);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((implied == null) ? 0 : implied.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationConstraint other = (RelationConstraint) obj;
		if (implied == null) {
			if (other.implied != null)
				return false;
		} else if (!implied.equals(other.implied))
			return false;
		return true;
	}

    @Override
    public int compareTo(Constraint o) {
        int result = super.compareTo(o);
        if (result == 0) {
            if (o instanceof RelationConstraint) {
                RelationConstraint other = (RelationConstraint) o;
            	result = this.getFamily().compareTo(other.getFamily());
            	if (result == 0) {
            		result = this.getSubFamily().compareTo(other.getSubFamily());
            		if (result == 0) {
            			result = this.getImplicationVerse().compareTo(other.getImplicationVerse());
	            		if (result == 0) {
	            			result = this.getTemplateName().compareTo(other.getTemplateName());
	            			if (result != 0) {
	                            if (this.getClass().isInstance(o)) {
	                            	result = -1;
	                            } else if (o.getClass().isInstance(this)) {
	                            	result = +1;
	                            } else {
	                            	result = 0;
	                            }
	            			}
	            		}
            		}
            	}
            } else {
                result = 1;
            }
        }
        return result;
    }
    
    @Override
    public ConstraintFamily getFamily() {
        return ConstraintFamily.RELATION;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RelationConstraintSubFamily getSubFamily() {
        return RelationConstraintSubFamily.NONE;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    public boolean regardsTheSameChars(RelationConstraint relCon) {
        return
                    this.base.equals(relCon.base)
                &&  this.implied.equals(relCon.implied);
    }
    
    @Override
	public TaskCharSet getImplied() {
    	return this.implied;
    }

	@Override
	public String getRegularExpression() {
		return String.format(this.getRegularExpressionTemplate(), base.toPatternString(), implied.toPatternString());
	}
	
	public abstract ConstraintImplicationVerse getImplicationVerse();
	
	public boolean isActivationBranched() {
		return this.base.size() > 1 && this.implied.size() < 2;
	}

	public boolean isTargetBranched() {
		return this.implied.size() > 1 && this.base.size() < 2;
	}
	
	public boolean isBranchedBothWays() {
		return this.isActivationBranched() && this.isTargetBranched();
	}
	
	public boolean hasActivationSetStrictlyIncludingTheOneOf(Constraint c) {
		return
				this.isActivationBranched()
			&&	this.base.strictlyIncludes(c.getBase());
	}
	
	public boolean hasTargetSetStrictlyIncludingTheOneOf(Constraint c) {
		return
				this.isTargetBranched()
			&&	this.implied.strictlyIncludes(c.getImplied());
	}

	@Override
	public boolean isDescendantAlongSameBranchOf(Constraint c) {
		if (super.isDescendantAlongSameBranchOf(c) == false) {
			return false;
		}
		if (!(c instanceof RelationConstraint)) {
			return false;
		}
		RelationConstraint relaCon = ((RelationConstraint)c);
		return
				this.getImplicationVerse() == relaCon.getImplicationVerse()
			// FIXME This is a trick which could be inconsistent with possible model extensions
			||	relaCon.getClass().equals(RespondedExistence.class);
	}

	@Override
	public boolean isTemplateDescendantAlongSameBranchOf(Constraint c) {
		if (super.isTemplateDescendantAlongSameBranchOf(c) == false) {
			return false;
		}
		if (!(c instanceof RelationConstraint)) {
			return false;
		}
		RelationConstraint relaCon = ((RelationConstraint)c);
		return
				this.getImplicationVerse() == relaCon.getImplicationVerse()
			// FIXME This is a trick which could be inconsistent with possible model extensions
			||	relaCon.getClass().equals(RespondedExistence.class);
	}
	
	@Override
	protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (this.getFamily().equals(ConstraintFamily.RELATION)) {
				this.base = this.getParameters().get(0);
				this.implied = this.getParameters().get(1);
		}
	}

	@Override
	public String getRegularExpressionTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkParams(TaskChar... taskChars)
			throws IllegalArgumentException {
		if (taskChars.length != 2)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}

	@Override
	public boolean checkParams(TaskCharSet... taskCharSets)
			throws IllegalArgumentException {
		if (taskCharSets.length != 2)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}
	
	/**
	 * Returns the target parameter of this relation constraint. 
	 * @return The target parameter of this relation constraint.
	 */
	public TaskCharSet getTarget() {
		return this.getImplied();
	}

	/**
	 * Returns the activation parameter of this relation constraint. 
	 * @return The activation parameter of this relation constraint.
	 */
	public TaskCharSet getActivation() {
		return this.getBase();
	}
}