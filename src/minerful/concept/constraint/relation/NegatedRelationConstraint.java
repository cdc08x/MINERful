/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public abstract class NegatedRelationConstraint extends RelationConstraint {
	@XmlTransient
	public static final int NOT_FAMILY_ID = -1;
    
    @XmlTransient
    protected RelationConstraint opponent;

    protected NegatedRelationConstraint() {
    	super();
    }
    
    public NegatedRelationConstraint(TaskChar base, TaskChar implied, double support) {
        super(base, implied, support);
    }
    public NegatedRelationConstraint(TaskChar base, TaskChar implied) {
        super(base, implied);
    }
    public NegatedRelationConstraint(TaskCharSet base, TaskCharSet implied,
			double support) {
		super(base, implied, support);
	}
	public NegatedRelationConstraint(TaskCharSet base, TaskCharSet implied) {
		super(base, implied);
	}

	@Override
    public int getFamily() {
        return NOT_FAMILY_ID;
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
}
