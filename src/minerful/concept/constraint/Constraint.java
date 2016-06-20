/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.existence.ExistenceConstraint;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.io.encdec.TaskCharEncoderDecoder;

@XmlRootElement(name="constraint")
@XmlSeeAlso({RelationConstraint.class,ExistenceConstraint.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Constraint implements Comparable<Constraint> {
	@XmlTransient
    public static final double MIN_SUPPORT = 0;
	@XmlTransient
    public static final double MAX_SUPPORT = 1.0;
	@XmlTransient
	public static final double DEFAULT_SUPPORT = MAX_SUPPORT;
	@XmlTransient
	public static final double MIN_INTEREST_FACTOR = MIN_SUPPORT;
	@XmlTransient
	public static final double MAX_INTEREST_FACTOR = MAX_SUPPORT;
	@XmlTransient
	public static final double DEFAULT_INTEREST_FACTOR = MIN_INTEREST_FACTOR;
	@XmlTransient
	public static final double MIN_CONFIDENCE = MIN_SUPPORT;
	@XmlTransient
	public static final double MAX_CONFIDENCE = MAX_SUPPORT;
	@XmlTransient
	public static final double DEFAULT_CONFIDENCE = MIN_CONFIDENCE;
	@XmlTransient
    public static final double RANGE_FOR_SUPPORT = (MAX_SUPPORT - MIN_SUPPORT);
	@XmlIDREF
//	@XmlTransient
    protected TaskCharSet base;
	@XmlElement
    public double support;
	@XmlElement
	public double confidence;
	@XmlElement
	public double interestFactor;
	@XmlAttribute
	public boolean evaluatedOnLog = false;
	@XmlTransient
	public final String type = this.getClass().getCanonicalName().substring(this.getClass().getCanonicalName().lastIndexOf('.') +1);
	@XmlTransient
	public boolean redundant = false;
	@XmlTransient
	public boolean conflicting = false;
	@XmlTransient
	public boolean belowSupportThreshold = false;
	@XmlTransient
	public boolean belowConfidenceThreshold = false;
	@XmlTransient
	public boolean belowInterestFactorThreshold = false;
	@XmlTransient
    protected Constraint constraintWhichThisIsBasedUpon;
	@XmlElementWrapper(name="parameters")
	@XmlElement(name="parameter")
	protected List<TaskCharSet> parameters;
	
	protected Constraint() {
//		this.base = null;
		this.parameters = new ArrayList<TaskCharSet>();
	}

	public Constraint(TaskChar param) {
		this(param, DEFAULT_SUPPORT);
	}
	public Constraint(TaskCharSet param) {
		this(param, DEFAULT_SUPPORT);
	}
    
    private void checkSupport() {
        if (        support < MIN_SUPPORT
                ||  support > MAX_SUPPORT) {
            throw new IllegalArgumentException("Provided support for " + this.toString() + " out of range: " + support);
        }
        return;
    }

	public Constraint(TaskChar param, double support) {
		this.checkSupport();
		this.base = new TaskCharSet(param);
		this.support = support;
		this.parameters = new ArrayList<TaskCharSet>(1);
		this.parameters.add(this.base);
	}

    public Constraint(TaskCharSet param, double support) {
    	this.checkSupport();
        this.base = param;
        this.support = support;
		this.parameters = new ArrayList<TaskCharSet>(1);
		this.parameters.add(this.base);
    }

	@Override
    public String toString() {
		StringBuilder sBuil = new StringBuilder();
		
		sBuil.append(getName());
		sBuil.append("(");
		
		boolean firstParam = true;
		
		for (TaskCharSet param : this.parameters) {
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
    	int	result = new Integer(this.parameters.size()).compareTo(othersParameters.size());
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

	private boolean isConflicting() {
		return this.conflicting;
	}
	
	public double getSupport() {
		return support;
	}
	public void setSupport(double support) {
		this.support = support;
	}

	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getInterestFactor() {
		return interestFactor;
	}
	public void setInterestFactor(double interestFactor) {
		this.interestFactor = interestFactor;
	}

	/**
     * Returns <code>true</code> if and only if the {@link #support support} of this constraint is equal to or higher than the given threshold.
	 * @param threshold The threshold to which the {@link #support support} of this constraint is compared
     * @return <code>true</code> if the {@link #support support} of this constraint is equal to or higher than the given threshold, <code>false</code> otherwise.
	 */
    public boolean hasSufficientSupport(double threshold) {
    	return this.support >= threshold;
    }
	/**
     * Returns <code>true</code> if and only if the {@link #confidence confidence} of this constraint is equal to or higher than the given threshold.
	 * @param threshold The threshold to which the {@link #confidence confidence} of this constraint is compared
     * @return <code>true</code> if the {@link #confidence confidence} of this constraint is equal to or higher than the given threshold, <code>false</code> otherwise.
	 */
    public boolean hasSufficientConfidence(double threshold) {
    	return this.confidence >= threshold;
    }
	/**
     * Returns <code>true</code> if and only if the {@link #interestFactor interest factor} of this constraint is equal to or higher than the given threshold.
	 * @param threshold The threshold to which the {@link #interestFactor interest factor} of this constraint is compared
     * @return <code>true</code> if the {@link #interestFactor interestFactor} of this constraint is equal to or higher than the given threshold, <code>false</code> otherwise.
	 */
    public boolean hasSufficientInterestFactor(double threshold) {
    	return this.interestFactor >= threshold;
    }
    public boolean isAboveThresholds() {
    	return !( this.belowSupportThreshold || this.belowConfidenceThreshold || this.belowInterestFactorThreshold);
    }

    public boolean hasReasonableSupport(double threshold) {
        return	(
        			(threshold == MAX_SUPPORT)
        		?	(support - threshold == 0)
				:		Math.abs(this.getRelativeSupport(threshold)) >= MIN_SUPPORT
        			&&	Math.abs(this.getRelativeSupport(threshold)) <= MAX_SUPPORT
        );
    }

    public double getRelativeSupport(double threshold) {
        if (MAX_SUPPORT - threshold == 0) {
            if (support - threshold == 0) {
                return MAX_SUPPORT;
            } else {
                return MIN_SUPPORT;
            }
        }
        return ((support - threshold) / (MAX_SUPPORT - threshold));
    }

    /**
     * Returns the difference between {@link #MAX_SUPPORT MAX_SUPPORT} and the given support. 
     * @param support The support to complement
     * @return The difference between {@link #MAX_SUPPORT MAX_SUPPORT} and the given support.
     */
    public static double complementSupport(double support) {
        return MAX_SUPPORT - support;
    }

    public int getHierarchyLevel() {
        return 0;
    }
    
    /**
     * Returns <code>true</code> if and only if the {@link #support support} of this constraint is equal to {@link #MAX_SUPPORT MAX_SUPPORT}.
     * @return <code>true</code> if the support of this constraint is equal to {@link #MAX_SUPPORT MAX_SUPPORT}, <code>false</code> otherwise.
     */
    public boolean hasMaximumSupport() {
    	return this.support == MAX_SUPPORT;
    }
    /**
     * Returns <code>true</code> if and only if the {@link #support support} of this constraint is equal to {@link #MIN_SUPPORT MIN_SUPPORT}.
     * @return <code>true</code> if the support of this constraint is equal to {@link #MIN_SUPPORT MIN_SUPPORT}, <code>false</code> otherwise.
     */
	public boolean hasLeastSupport() {
		return this.support == MIN_SUPPORT;
	}
    
    public String getName() {
    	return this.getClass().getCanonicalName().substring(this.getClass().getCanonicalName().lastIndexOf('.') + 1);
    }
	
	public TaskCharSet getBase() {
		return this.base;
	}

//	public void setBase(TaskCharSet base) {
//		this.parameters.set(0, base);
//	}
	
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
	
	public abstract TaskCharSet getImplied();

	public abstract String getRegularExpressionTemplate();

	public boolean isBranched() {
		return	(this.getBase() != null && this.getBase().size() > 1)
			||	(this.getImplied() != null && this.getImplied().size() > 1);
	}

    
    public boolean isMoreInformativeThanGeneric() {
        if (!this.hasConstraintToBaseUpon())
            return true;
        Integer moreReliableThanGeneric = new Double(this.support).compareTo(constraintWhichThisIsBasedUpon.support);
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

	
	public void setConstraintWhichThisIsBasedUpon(Constraint constraintWhichThisIsBasedUpon) {
	    if (this.constraintWhichThisIsBasedUpon == null) {
	        if (constraintWhichThisIsBasedUpon.getHierarchyLevel() >= this.getHierarchyLevel()) {
	            throw new IllegalArgumentException("Wrong hierarchy provided");
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
		return baCon.equals(c);
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
    
    public Constraint createConstraintWhichThisShouldBeBasedUpon() {
    	Constraint cns = suggestConstraintWhichThisShouldBeBasedUpon();
    	if (cns != null) {
	    	cns.support = this.support;
	    	cns.confidence = this.confidence;
	    	cns.interestFactor = this.interestFactor;
    	}
    	return cns;
    }

	public boolean isMarkedForExclusion() {
		return this.isRedundant() || !this.isAboveThresholds() || this.isConflicting();
	}
	
	public abstract Constraint copy(TaskChar... taskChars);
	public abstract Constraint copy(TaskCharSet... taskCharSets);
	public abstract boolean checkParams(TaskChar... taskChars) throws IllegalArgumentException;
	public abstract boolean checkParams(TaskCharSet... taskCharSets) throws IllegalArgumentException;

	public VacuityAwareWildcardAutomaton getCheckAutomaton() {
		VacuityAwareWildcardAutomaton autom = new VacuityAwareWildcardAutomaton(
				this.getRegularExpression(), TaskCharEncoderDecoder.getTranslationMap(this.getInvolvedTaskChars()));
		return autom;
	}
	
	protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (this.getFamily().equals(ConstraintFamily.EXISTENCE)) {
			this.base = this.getParameters().get(0);
		}
	}
}