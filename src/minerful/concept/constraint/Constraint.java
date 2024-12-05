/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.existence.ExistenceConstraint;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.io.encdec.TaskCharEncoderDecoder;



public abstract class Constraint implements Comparable<Constraint> {
    protected TaskCharSet base;

	public final String type = this.getClass().getCanonicalName().substring(this.getClass().getCanonicalName().lastIndexOf('.') +1);

	protected boolean redundant = false;

	protected boolean conflicting = false;
	/**
	 * To be set to <code>true</code> if we want to state that the constraint is to be violated.
	 */
	protected boolean forbidden = false;
	protected ConstraintMeasuresManager evtBasedMeasures = new ConstraintMeasuresManager();
	protected ConstraintMeasuresManager trcBasedMeasures = new ConstraintMeasuresManager();
    protected Constraint constraintWhichThisIsBasedUpon;
	protected List<TaskCharSet> parameters;

	protected boolean silentToObservers = true;
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	protected Constraint() {
		this.parameters = new ArrayList<TaskCharSet>();
		this.evtBasedMeasures = new ConstraintMeasuresManager(this, this.pcs);
		this.trcBasedMeasures = new ConstraintMeasuresManager(this, this.pcs);
	}

	public Constraint(TaskChar... params) {
		this();
		this.setSilentToObservers(true);
		this.base = new TaskCharSet(params[0]);
		this.parameters = new ArrayList<TaskCharSet>(params.length);
		for (TaskChar param : params)
			this.parameters.add(new TaskCharSet(param));
		this.setSilentToObservers(false);
	}

	public Constraint(TaskCharSet... params) {
		this();
		this.setSilentToObservers(true);
		this.base = params[0];
		this.parameters = new ArrayList<TaskCharSet>(params.length);
		for (TaskCharSet param : params)
			this.parameters.add(param);
		this.setSilentToObservers(false);
	}
	
	public ConstraintMeasuresManager getEventBasedMeasures() {
		return this.evtBasedMeasures;
	}	
	
	public ConstraintMeasuresManager getTraceBasedMeasures() {
		return this.trcBasedMeasures;
	}

	
	@Override
    public String toString() {
		StringBuilder sBuil = new StringBuilder();
		
		sBuil.append(getTemplateName());
		sBuil.append("(");
		
		boolean firstParam = true;
		
		for (TaskCharSet param : getParameters()) {
			if (firstParam) { firstParam = false; }
			else { sBuil.append(", "); }
			sBuil.append(param);
		}
		
		sBuil.append(")");

		return sBuil.toString();
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constraint other = (Constraint) obj;
		int paramsComparison = this.compareParameters(other.getParameters());
		if (paramsComparison != 0)
			return false;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

    @Override
    public int compareTo(Constraint t) {
        int result = this.compareParameters(t.getParameters());
        return result;
    }
    
    protected int compareParameters(List<TaskCharSet> othersParameters) {
    	int	result = Integer.valueOf(this.parameters.size()).compareTo(othersParameters.size());
    	if (result == 0) {
    		for (int i = 0; i < this.parameters.size() && result == 0; i++) {
    			result = this.parameters.get(i).compareTo(othersParameters.get(i));
    		}
    	}
		return result;
	}

	public boolean isRedundant() {
		return this.redundant;
	}
	public boolean isConflicting() {
		return this.conflicting;
	}
	public boolean isForbidden() {
		return this.forbidden;
	}

	public void setRedundant(boolean redundant) {
		if (this.redundant != redundant) {
			boolean oldredundant = this.redundant;
			this.redundant = redundant;
			pcs.firePropertyChange(ConstraintChange.ChangedProperty.REDUNDANT.toString(), oldredundant, redundant);
		}
	}

	public void setConflicting(boolean conflicting) {
		if (this.conflicting != conflicting) {
			boolean oldconflicting = this.conflicting;	
			this.conflicting = conflicting;
			pcs.firePropertyChange(ConstraintChange.ChangedProperty.CONFLICTING.toString(), oldconflicting, conflicting);
		}
	}

	public void setForbidden(boolean forbidden) {
		if (this.conflicting != forbidden) {
			boolean oldforbidden = this.forbidden;	
			this.forbidden = forbidden;
			pcs.firePropertyChange(ConstraintChange.ChangedProperty.FORBIDDEN.toString(), oldforbidden, forbidden);
		}
	}

	public boolean isEvaluatedOnLog() {
		return evtBasedMeasures.evaluatedOnLog;
	}
	public void setEvaluatedOnLog(boolean evaluatedOnLog) {
		if (this.evtBasedMeasures.evaluatedOnLog != evaluatedOnLog) {
			boolean oldEvaluatedOnLog = this.evtBasedMeasures.evaluatedOnLog;
			this.evtBasedMeasures.evaluatedOnLog = evaluatedOnLog;
			pcs.firePropertyChange(ConstraintChange.ChangedProperty.EVALUATED_ON_LOG.toString(), oldEvaluatedOnLog, evaluatedOnLog);
		}
	}

    public int getHierarchyLevel() {
        return 0;
    }
    
    public String getTemplateName() {
    	return MetaConstraintUtils.getTemplateName(this);
    }
	
	public TaskCharSet getBase() {
		return this.base;
	}
	
	/**
	 * Returns the target parameters of this relation constraint. 
	 * @return The target parameters of this relation constraint.
	 */
	public TaskCharSet[] getTargets() {
		return new TaskCharSet[] { this.getBase() };
	}

	/**
	 * Returns the activation parameters of this relation constraint. 
	 * @return The activation parameters of this relation constraint.
	 */
	public TaskCharSet[] getActivators() {
		return null;
	}

	public List<TaskCharSet> getParameters() {
		return parameters;
	}

	public void setParameters(List<TaskCharSet> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the regular expression representing the semantics of this constraint.
	 * @return The regular expression representing the semantics of this constraint.
	 * @see Constraint#getRegularExpressionTemplate() getRegularExpressionTemplate()
	 */
	public String getRegularExpression() {
		return String.format(this.getRegularExpressionTemplate(), this.base.toPatternString(true));
	}

	/**
	 * Returns the LTLpf expression (in NuSMV syntax) representing the semantics of this constraint.
	 * @return The rLTLpf expression (in NuSMV syntax) representing the semantics of this constraint.
	 * @see Constraint#getLTLpfExpressionTemplate() getLTLpfExpressionTemplate()
	 */
	public String getLTLpfExpression() {
		return String.format(this.getLTLpfExpressionTemplate(), this.base.toLTLpfString());
	}

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	public String getNegativeRegularExpression() {
		return String.format(this.getNegativeRegularExpressionTemplate(), this.base.toPatternString(true));
	}//

	public String getNegativeLTLpfExpression() {
		return String.format(this.getNegativeLTLpfExpressionTemplate(), this.base.toLTLpfString());
	}//
	/////////////////////////////////////////////////////////////////////////////////////////////

	
	/**
	 * Reverses the order of parameters ()
	 */
	protected void reverseOrderOfParams() {
		Collections.reverse(this.parameters);
	}
	
	public abstract TaskCharSet getImplied();

	public abstract String getRegularExpressionTemplate();
	
	public abstract String getLTLpfExpressionTemplate();

	///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
	public abstract String getNegativeRegularExpressionTemplate();

	public abstract String getNegativeLTLpfExpressionTemplate();
	/////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isBranched() {
		return	(this.getBase() != null && this.getBase().size() > 1)
			||	(this.getImplied() != null && this.getImplied().size() > 1);
	}

    /**
	 * method checking only event based confidence (to check)
	 * @return
	 */
    public boolean isMoreInformativeThanGeneric() {
        if (!this.hasConstraintToBaseUpon())
            return true;
        Integer moreReliableThanGeneric = Double.valueOf((double)this.evtBasedMeasures.confidence).compareTo(constraintWhichThisIsBasedUpon.evtBasedMeasures.confidence);
		if (moreReliableThanGeneric == 0)
        	return constraintWhichThisIsBasedUpon.isMoreInformativeThanGeneric();
        return (moreReliableThanGeneric > 0);
    }

	public Set<TaskChar> getInvolvedTaskChars() {
		TreeSet<TaskChar> involvedChars = new TreeSet<TaskChar>();
		for (TaskCharSet param : this.parameters) {
			involvedChars.addAll(param.getSetOfTaskChars());
		}
		return involvedChars;
	}

	public Set<Character> getInvolvedTaskCharIdentifiers() {
		TreeSet<Character> involvedChars = new TreeSet<Character>();
		for (TaskCharSet param : this.parameters) {
			involvedChars.addAll(param.getListOfIdentifiers());
		}
		return involvedChars;
	}
	
	public void setConstraintWhichThisIsBasedUpon(Constraint constraintWhichThisIsBasedUpon) {
	    if (this.constraintWhichThisIsBasedUpon == null) {
	        if (constraintWhichThisIsBasedUpon.getHierarchyLevel() >= this.getHierarchyLevel()) {
	            throw new IllegalArgumentException(
	            		String.format("Wrong hierarchy provided: Can %1$s be sumsumed by %2$s?", this, constraintWhichThisIsBasedUpon));
	        }
	        this.constraintWhichThisIsBasedUpon = constraintWhichThisIsBasedUpon;
	    }
	}
	
	public boolean hasConstraintToBaseUpon() {
	    return this.constraintWhichThisIsBasedUpon != null;
	}

	public Constraint getConstraintWhichThisIsBasedUpon() {
	    return constraintWhichThisIsBasedUpon;
	}
	
	public boolean isChildOf(Constraint c) {
		Constraint baCon = this.suggestConstraintWhichThisShouldBeBasedUpon();
		if (baCon == null) {
			return false;
		}
		return c.equals(baCon);
	}

	public boolean isDescendantAlongSameBranchOf(Constraint c) {
		return	(
					this.isTemplateDescendantAlongSameBranchOf(c)
					&&	this.base.equals(c.base)
					&&	(	this.getImplied() == null && c.getImplied() == null
						||	this.getImplied().equals(c.getImplied())
						)
				);
	}

	public boolean isTemplateDescendantAlongSameBranchOf(Constraint c) {
		return	(
					this.getFamily() == c.getFamily()
					&&	this.getSubFamily() == c.getSubFamily()
					&&	this.getHierarchyLevel() > c.getHierarchyLevel()
				);
	}

	public boolean isDescendantOf(Constraint c) {
		return this.isChildOf(c) || isDescendantAlongSameBranchOf(c);
	}
    
    public abstract ConstraintFamily getFamily();

    public abstract <T extends ConstraintSubFamily> T getSubFamily();

    public abstract Constraint suggestConstraintWhichThisShouldBeBasedUpon();

    /**
     * Returns instances of all constraints that would be derived from this one.
     * By default, it returns the {@link #suggestConstraintWhichThisShouldBeBasedUpon() suggestConstraintWhichThisShouldBeBasedUpon()}
     * @return 
     */
    public Constraint[] suggestImpliedConstraints() {
    	return new Constraint[]{ this.suggestConstraintWhichThisShouldBeBasedUpon() };
    }

    public Constraint createConstraintWhichThisShouldBeBasedUpon() {
    	Constraint cns = suggestConstraintWhichThisShouldBeBasedUpon();
		if (cns != null) {
			cns.evtBasedMeasures.support = this.evtBasedMeasures.support;
			cns.evtBasedMeasures.confidence = this.evtBasedMeasures.confidence;
			cns.evtBasedMeasures.coverage = this.evtBasedMeasures.coverage;
		}
    	return cns;
    }

    /**
     * Returns <code>true</code> if this constraint is below thresholds; <code>false</code> otherwise.
     * @return <code>true</code> if this constraint is below thresholds; <code>false</code> otherwise.
     */
	public boolean isBelowThresholds() {
		return !this.evtBasedMeasures.isAboveThresholds() || !this.trcBasedMeasures.isAboveThresholds();
		//!false or !true --> true or false --> true
	}


	

    /**
     * Returns <code>true</code> if this constraint is redundant, below thresholds, or in conflict with other constraints; <code>false</code> otherwise.
     * @return <code>true</code> if this constraint is redundant, below thresholds, or in conflict with other constraints; <code>false</code> otherwise.
     */
	public boolean isMarkedForExclusion() {
		return this.isRedundant() || this.isBelowThresholds() || this.isConflicting();
		//change event based in trace based
	}


    /**
     * Returns <code>true</code> if this constraint is redundant, below thresholds, in conflict with other constraints, or forbidden; <code>false</code> otherwise.
     * @return <code>true</code> if this constraint is redundant, below thresholds, or in conflict with other constraints, or forbidden; or <code>false</code> otherwise.
     */
	public boolean isMarkedForExclusionOrForbidden() {
		return this.isMarkedForExclusion() || this.isForbidden();
	}
	
	public abstract Constraint getSymbolic();
	public abstract Constraint copy(TaskChar... taskChars);
	public abstract Constraint copy(TaskCharSet... taskCharSets);
	public abstract boolean checkParams(TaskChar... taskChars) throws IllegalArgumentException;
	public abstract boolean checkParams(TaskCharSet... taskCharSets) throws IllegalArgumentException;

	public VacuityAwareWildcardAutomaton getCheckAutomaton() {
		VacuityAwareWildcardAutomaton autom = new VacuityAwareWildcardAutomaton(
				this.toString(),
				this.getRegularExpression(), 
				TaskCharEncoderDecoder.getTranslationMap(this.getInvolvedTaskChars()));
		return autom;
	}
	
	/**
	 * Resets properties {@link #evtBasedMeasures.belowSupportThreshold belowSupportThreshold},
	 * {@link #evtBasedMeasures.belowConfidenceThreshold belowConfidenceThreshold},
	 * {@link #evtBasedMeasures.belowCoverageThreshold belowCoverageThreshold},
	 * {@link #conflicting conflicting}, {@link #redundant redundant}
	 * {@link #forbidden forbidden} to their default values.
	 */
	public void resetMarks() {
		this.evtBasedMeasures.belowSupportThreshold = false;
		this.evtBasedMeasures.belowConfidenceThreshold = false;
		this.evtBasedMeasures.belowCoverageThreshold = false;
		this.conflicting = false;
		this.redundant = false;
		this.forbidden = false;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

	public boolean isSilentToObservers() {
		return silentToObservers;
	}

	protected void setSilentToObservers(boolean silentToObservers) {
		this.silentToObservers = silentToObservers;
	}
	
	// protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	// 	if (this.getFamily().equals(ConstraintFamily.EXISTENCE)) {
	// 		this.base = this.getParameters().get(0);
	// 	}
	// }
}
