/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import java.util.Collection;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;

public abstract class RelationConstraint extends Constraint {
	public static enum ImplicationVerse {
		FORWARD,
		BACKWARD,
		BOTH
	}
	@XmlElement
    public final TaskCharSet implied;
	
	protected RelationConstraint() {
		super();
		this.implied = null;
	}

    public RelationConstraint(TaskCharSet base, TaskCharSet implied, double support) {
        super(base, support);
        this.implied = implied;
    }
    public RelationConstraint(TaskCharSet base, TaskCharSet implied) {
        super(base);
        this.implied = implied;
    }
    public RelationConstraint(TaskChar base, TaskChar implied, double support) {
        super(base, support);
        this.implied = new TaskCharSet(implied);
    }
    public RelationConstraint(TaskChar base, TaskChar implied) {
        super(base);
        this.implied = new TaskCharSet(implied);
    }

    @Override
    public String toString() {
        return super.toString()
                + "(" + base + ", " + implied + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final RelationConstraint other = (RelationConstraint) obj;
        if (this.implied != other.implied && (this.implied == null || !this.implied.equals(other.implied))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Constraint t) {
    	// this.base.compareTo(t.base)
        int result = super.compareTo(t);
        if (result == 0) {
            if (t instanceof RelationConstraint) {
                RelationConstraint other = (RelationConstraint) t;
                if (this.implied.equals(other.implied)) {
                	result = this.getFamily().compareTo(other.getFamily());
                	if (result == 0) {
                		result = this.getSubFamily().compareTo(other.getSubFamily());
                		if (result == 0) {
                			result = this.getName().compareTo(other.getName());
                			if (result != 0) {
                                if (this.getClass().isInstance(t)) {
                                	result = -1;
                                } else if (t.getClass().isInstance(this)) {
                                	result = +1;
                                } else {
                                	result = 0;
                                }
                			}
                		}
                	}
                } else {
                	result = this.implied.compareTo(other.implied);
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
    @Override
    public ConstraintSubFamily getSubFamily() {
        return ConstraintSubFamily.NONE;
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
		return String.format(this.getRegularExpressionTemplate(), base.toPatternString(true), implied.toPatternString(true));
	}
	
	@Override
	public Collection<TaskChar> getInvolvedTaskChars() {
		TreeSet<TaskChar> involvedChars = new TreeSet<TaskChar>();
		involvedChars.addAll(this.base.getTaskCharsCollection());
		involvedChars.addAll(this.implied.getTaskCharsCollection());
		return involvedChars;
	}
	
	public abstract ImplicationVerse getImplicationVerse();
	
	public boolean isInBranched() {
		return this.base.size() > 1 && this.implied.size() < 2;
	}

	public boolean isOutBranched() {
		return this.implied.size() > 1 && this.implied.size() < 2;
	}
	
	public boolean isBranchedBothWays() {
		return this.isInBranched() && this.isOutBranched();
	}
	
	@Override
	public boolean isBranched() {
		return this.isInBranched() || this.isOutBranched();
	}
}