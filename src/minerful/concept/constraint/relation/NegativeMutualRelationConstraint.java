package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

public abstract class NegativeMutualRelationConstraint extends MutualRelationConstraint implements NegativeRelationConstraintInterface {
    protected RelationConstraint opponent;

	public NegativeMutualRelationConstraint() {
		super();
	}
	
	public NegativeMutualRelationConstraint(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	public NegativeMutualRelationConstraint(TaskChar param1, TaskChar param2) {
		super(param1, param2);
	}

	@Override
	public RelationConstraintSubFamily getSubFamily() {
	    return RelationConstraintSubFamily.NEGATIVE_MUTUAL;
	}

	@Override
	public int getHierarchyLevel() {
	    return super.getHierarchyLevel() + 1;
	}
    
	protected void setOpponent(RelationConstraint opposedTo, Class<?> expectedClass) {
        if (!opposedTo.getClass().equals(expectedClass))
            throw new IllegalArgumentException("Illegal opponent constraint");
        this.opponent = opposedTo;
    }
    
    @Override
	public boolean isMoreReliableThanTheOpponent() {
        if (!this.hasOpponent())
            throw new IllegalStateException("No opponent constraint is set");
        return this.evtBasedMeasures.support > opponent.getEventBasedMeasures().getSupport();
    }
    
    @Override
    public boolean hasOpponent() {
        return this.opponent != null;
    }

    @Override
	public RelationConstraint getOpponent() {
        return opponent;
    }

    @Override
	public void setOpponent(RelationConstraint opponent) {
		this.opponent = opponent;
	}

	@Override
	public abstract Constraint suggestOpponentConstraint();

}