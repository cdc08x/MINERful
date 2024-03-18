/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

public abstract class NegativeRelationConstraint extends RelationConstraint {
    protected RelationConstraint opponent;

    protected NegativeRelationConstraint() {
    	super();
    }
    public NegativeRelationConstraint(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
	public NegativeRelationConstraint(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public ConstraintImplicationVerse getImplicationVerse() {
    	return ConstraintImplicationVerse.BOTH;
    }
	
    @Override
	public RelationConstraintSubFamily getSubFamily() {
		return RelationConstraintSubFamily.NEGATIVE;
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
        return this.evtBasedMeasures.support > opponent.getEventBasedMeasures().getSupport();
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
