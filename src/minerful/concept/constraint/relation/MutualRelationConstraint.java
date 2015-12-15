package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

@XmlType
@XmlSeeAlso({CoExistence.class})
public abstract class MutualRelationConstraint extends RelationConstraint {
	@XmlTransient
	protected RelationConstraint forwardConstraint;
	@XmlTransient
	protected RelationConstraint backwardConstraint;

	public MutualRelationConstraint() {
		super();
	}

	public MutualRelationConstraint(TaskCharSet param1, TaskCharSet param2,
			double support) {
		super(param1, param2, support);
	}

	public MutualRelationConstraint(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	public MutualRelationConstraint(TaskChar param1, TaskChar param2,
			double support) {
		super(param1, param2, support);
	}

	public MutualRelationConstraint(TaskChar param1, TaskChar param2) {
		super(param1, param2);
	}

	@Override
	public ConstraintImplicationVerse getImplicationVerse() {
		return ConstraintImplicationVerse.BOTH;
	}

	@Override
	public RelationConstraintSubFamily getSubFamily() {
	    return RelationConstraintSubFamily.COUPLING;
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

	public void setImplyingConstraints(RelationConstraint forwardConstraint, RelationConstraint backwardConstraint) {
		this.forwardConstraint = forwardConstraint;
		this.backwardConstraint = backwardConstraint;
	}

	public boolean isAsInformativeAsTheImplyingConstraints() {
	    return	this.support >= forwardConstraint.support &&
	            this.support >= backwardConstraint.support;
	}

	public boolean isMoreInformativeThanAnyOfImplyingConstraints() {
	    return	this.support >= forwardConstraint.support ||
	            this.support >= backwardConstraint.support;
	}

	public boolean isMoreInformativeThanForwardConstraint() {
	    return	this.support >= forwardConstraint.support;
	}

	public boolean isMoreInformativeThanBackwardConstraints() {
	    return	this.support >= backwardConstraint.support;
	}

	protected boolean ckeckConsistency(RelationConstraint forwardConstraint, RelationConstraint backwardConstraint) {
	    return     forwardConstraint.getParameters().containsAll(backwardConstraint.getParameters())
	    		&&	backwardConstraint.getParameters().containsAll(forwardConstraint.getParameters())
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

	public RelationConstraint getPlausibleForwardConstraint() {
		if (this.hasForwardConstraint())
			return this.getForwardConstraint();
		return null;
	}

	public RelationConstraint getPlausibleBackwardConstraint() {
		if (this.hasBackwardConstraint())
			return this.getBackwardConstraint();
		return null;
	}
}