package minerful.concept.constraint.relation;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily;

public abstract class CouplingRelationConstraint extends RelationConstraint {

	protected RespondedExistence forwardConstraint;
	protected RespondedExistence backwardConstraint;

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
	    return ConstraintFamily.CO_FAMILY_ID;
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

	protected boolean ckeckConsistency(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
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