/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;

@XmlRootElement
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
	@XmlTransient
    public final TaskCharSet base;
	@XmlElement
    public double support;
	@XmlElement
	public double confidence;
	@XmlElement
	public double interestFactor;
	@XmlAttribute
	public final String type = this.getClass().getCanonicalName().substring(this.getClass().getCanonicalName().lastIndexOf('.') +1);
	@XmlAttribute
	public boolean redundant = false;
	@XmlTransient
    private Constraint constraintWhichThisIsBasedUpon;
	
	protected Constraint() {
		this.base = null;
	}

	public Constraint(TaskChar base) {
		this(base, DEFAULT_SUPPORT);
	}
	public Constraint(TaskCharSet base) {
		this(base, DEFAULT_SUPPORT);
	}
    
    private void checkSupport() {
        if (        support < MIN_SUPPORT
                ||  support > MAX_SUPPORT) {
            throw new IllegalArgumentException("Provided support for " + this.toString() + " out of range: " + support);
        }
        return;
    }

	public Constraint(TaskChar base, double support) {
		this.checkSupport();
		this.base = new TaskCharSet(base);
		this.support = support;
	}
    public Constraint(TaskCharSet base, double support) {
    	this.checkSupport();
        this.base = base;
        this.support = support;
    }

	@Override
    public String toString() {
//      return " \"" + getRegularExpression() + "\" " + this.getName();
		
		String strRep = getName() + "(";
		
		if(base.getTaskChars().length>1)
			strRep += "{";
		Iterator<TaskChar> tcIter = base.getTaskCharsCollection().iterator();
		while (tcIter.hasNext()) {
			strRep += tcIter.next().taskClass;
			if(tcIter.hasNext())
				strRep += ",";
		}
		if(base.getTaskChars().length>1)
			strRep += "}";	
		
		
		if(this.getImplied() != null) {
			strRep += ";";
			
			if(this.getImplied().getTaskChars().length>1)
				strRep += "{";
			tcIter = this.getImplied().getTaskCharsCollection().iterator();
			while (tcIter.hasNext()) {
				strRep += tcIter.next().taskClass;
				if(tcIter.hasNext())
					strRep += ",";
			}
			if(this.getImplied().getTaskChars().length>1)
				strRep += "}";	

		}
		
		strRep += ")";
		
		strRep += " support=" + support;
		strRep += ", confidence=" + confidence;
		
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Constraint other = (Constraint) obj;
        if (this.base != other.base && (this.base == null || !this.base.equals(other.base))) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Constraint t) {
        int result = this.base.compareTo(t.base);
        return result;
    }
    
    public boolean isOfInterest(double minimumInterestFactor) {
    	return interestFactor >= minimumInterestFactor;
    }
    
	public boolean isConfident(double minimumConfidenceLevel) {
		return confidence >= minimumConfidenceLevel;
	}
    
    public boolean hasSufficientSupport(double threshold) {
    	return this.support >= threshold;
    }
    public boolean hasSufficientConfidence(double threshold) {
    	return this.confidence >= threshold;
    }
    public boolean hasSufficientInterestFactor(double threshold) {
    	return this.interestFactor >= threshold;
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

    public static double complementSupport(double support) {
        return MAX_SUPPORT - support;
    }

    public int getHierarchyLevel() {
        return 0;
    }
    
    public boolean hasMaximumSupport() {
    	return this.support == MAX_SUPPORT;
    }
    
    public String getName() {
    	return this.getClass().getCanonicalName().substring(this.getClass().getCanonicalName().lastIndexOf('.') + 1);
    }
	public boolean hasLeastSupport() {
		return this.support == MIN_SUPPORT;
	}
	
	public TaskCharSet getBase() {
		return this.base;
	}
	
	public String getRegularExpression() {
		return String.format(this.getRegularExpressionTemplate(), this.base.toPatternString(true));
	}
	
	public abstract TaskCharSet getImplied();
	
	public abstract String getRegularExpressionTemplate();

	public boolean isBranched() {
		return this.base.size() > 1;
	}

    
    public boolean isMoreReliableThanGeneric() {
        if (!this.hasConstraintToBaseUpon())
            return true;
        Integer moreReliableThanGeneric = new Double(this.support).compareTo(constraintWhichThisIsBasedUpon.support);
        if (moreReliableThanGeneric == 0)
        	return constraintWhichThisIsBasedUpon.isMoreReliableThanGeneric();
        return (moreReliableThanGeneric > 0);
    }

	public abstract Collection<TaskChar> getInvolvedTaskChars();

	
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
    
    public abstract ConstraintFamily getFamily();
    
    public abstract ConstraintSubFamily getSubFamily();
    
    public abstract Constraint getConstraintWhichThisShouldBeBasedUpon();
}