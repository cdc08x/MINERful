/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import java.util.Collection;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;

public abstract class RelationConstraint extends Constraint {
	public static enum ImplicationVerse {
		FORWARD,
		BACKWARD,
		BOTH
	}
	@XmlTransient
    public static final int RELATION_CONSTRAINT_FAMILY_ID = 0;
	@XmlTransient
    public static final int NO_SUB_FAMILY_ID = 0;
	@XmlTransient
    private RelationConstraint constraintWhichThisIsBasedUpon;

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
                    if (this.getFamily() > other.getFamily()) {
                        return 1;
                    } else if (this.getFamily() < other.getFamily()) {
                        return -1;
                    } else {
                        if (this.getSubFamily() > other.getSubFamily()) {
                            return 1;
                        } else if (this.getSubFamily() < other.getSubFamily()) {
                            return -1;
                        } else {
                            if (this.getName().compareTo(other.getName()) != 0) {
                                if (this.getClass().isInstance(t)) {
                                    return -1;
                                } else if (t.getClass().isInstance(this)) {
                                    return +1;
                                } else {
                                    return 0;
                                }
                            } else {
                                return this.getName().compareTo(other.getName());
                            }
                        }
                    }
                } else {
                    return this.implied.compareTo(other.implied);
                }
            }
            return 1;
        }
        return result;
    }
    
    public int getFamily() {
        return RELATION_CONSTRAINT_FAMILY_ID;
    }
    
    public int getSubFamily() {
        return NO_SUB_FAMILY_ID;
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

    public RelationConstraint getConstraintWhichThisIsBasedUpon() {
        return constraintWhichThisIsBasedUpon;
    }

    public void setConstraintWhichThisIsBasedUpon(RelationConstraint constraintWhichThisIsBasedUpon) {
        if (this.constraintWhichThisIsBasedUpon == null) {
            if (constraintWhichThisIsBasedUpon.getHierarchyLevel() >= this.getHierarchyLevel())
            {
                throw new IllegalArgumentException("Wrong hierarchy provided");
            }
            this.constraintWhichThisIsBasedUpon = constraintWhichThisIsBasedUpon;
        }
    }
    
    public boolean isMoreReliableThanGeneric() {
        if (!this.hasConstraintToBeBasedUpon())
            return true;
        Integer moreReliableThanGeneric = new Double(this.support).compareTo(constraintWhichThisIsBasedUpon.support);
        if (moreReliableThanGeneric == 0)
        	return constraintWhichThisIsBasedUpon.isMoreReliableThanGeneric();
        return (moreReliableThanGeneric > 0);
    }
    
    public boolean hasConstraintToBeBasedUpon() {
        return this.constraintWhichThisIsBasedUpon != null;
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