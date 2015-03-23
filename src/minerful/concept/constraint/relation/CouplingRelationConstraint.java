package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;

public abstract class CouplingRelationConstraint extends RelationConstraint {

	protected RelationConstraint forwardConstraint;
	protected RelationConstraint backwardConstraint;

	public CouplingRelationConstraint() {
		super();
	}

	public CouplingRelationConstraint(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}

	public CouplingRelationConstraint(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	public CouplingRelationConstraint(TaskChar base, TaskChar implied,
			double support) {
		super(base, implied, support);
	}

	public CouplingRelationConstraint(TaskChar base, TaskChar implied) {
		super(base, implied);
	}

	@Override
	public ImplicationVerse getImplicationVerse() {
		return ImplicationVerse.BOTH;
	}

	@Override
	public ConstraintFamily getFamily() {
	    return ConstraintFamily.COUPLING;
	}

	@Override
	public int getHierarchyLevel() {
	    return super.getHierarchyLevel() + 1;
	}

	public RelationConstraint getForwardConstraint() {
	    return forwardConstraint;
	}

	public RelationConstraint getBackwardConstraint() {
	    return backwardConstraint;
	}
	
	public boolean hasForwardConstraint() {
	    return forwardConstraint != null;
	}
	
	public boolean hasBackwardConstraint() {
	    return backwardConstraint != null;
	}

	public abstract Constraint getSupposedForwardConstraint();
	public abstract Constraint getSupposedBackwardConstraint();

	public void setImplyingConstraints(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
		this.forwardConstraint = forwardConstraint;
		this.backwardConstraint = backwardConstraint;
	}

	public boolean isMoreReliableThanTheImplyingConstraints() {
	    return	this.support >= forwardConstraint.support &&
	            this.support >= backwardConstraint.support;
	}

	protected boolean ckeckConsistency(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
	    return     forwardConstraint.base.equals(backwardConstraint.implied)
	            &&  forwardConstraint.implied.equals(backwardConstraint.base)
	            &&  this.getHierarchyLevel() == forwardConstraint.getHierarchyLevel()
	            &&  this.getHierarchyLevel() == backwardConstraint.getHierarchyLevel();
	}

	public boolean hasImplyingConstraints() {
		return	this.forwardConstraint != null &&
				this.backwardConstraint != null;
	}

	public void setForwardConstraint(RelationConstraint forwardConstraint) {
		this.forwardConstraint = forwardConstraint;
	}
	public void setBackwardConstraint(RelationConstraint backwardConstraint) {
		this.backwardConstraint = backwardConstraint;
	}
}