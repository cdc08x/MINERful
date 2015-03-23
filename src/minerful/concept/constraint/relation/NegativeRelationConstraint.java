/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;

public abstract class NegativeRelationConstraint extends RelationConstraint {
    @XmlTransient
    protected RelationConstraint opponent;

    protected NegativeRelationConstraint() {
    	super();
    }
    
    public NegativeRelationConstraint(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public NegativeRelationConstraint(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public NegativeRelationConstraint(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}
	public NegativeRelationConstraint(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public ConstraintFamily getFamily() {
        return ConstraintFamily.NEGATIVE;
    }
    
	@Override
    public ImplicationVerse getImplicationVerse() {
    	return ImplicationVerse.BOTH;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }
    
    protected void setOpponent(RelationConstraint opposedTo, Class<?> expectedClass) {
        if (!opposedTo.getClass().equals(expectedClass))
            throw new IllegalArgumentException("Illegal opponent constraint");
        this.opponent = opposedTo;
    }
    
    public boolean isMoreReliableThanTheOpponent() {
        if (!this.hasOpponent())
            throw new IllegalStateException("No opponent constraint is set");
        return this.support > opponent.support;
    }
    
    public boolean hasOpponent() {
        return this.opponent != null;
    }

    public RelationConstraint getOpponent() {
        return opponent;
    }

    public void setOpponent(RelationConstraint opponent) {
		this.opponent = opponent;
	}

	public abstract Constraint getSupposedOpponentConstraint();
}
