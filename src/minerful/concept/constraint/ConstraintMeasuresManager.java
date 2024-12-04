package minerful.concept.constraint;

import java.beans.PropertyChangeSupport;

import minerful.concept.constraint.existence.ExistenceConstraint;
import minerful.concept.constraint.relation.RelationConstraint;

public class ConstraintMeasuresManager {
    public static final double MIN_SUPPORT = 0;
    public static final double MAX_SUPPORT = 1.0;
	public static final double DEFAULT_SUPPORT = MAX_SUPPORT;
	public static final double MIN_COVERAGE = MIN_SUPPORT;
	public static final double MAX_COVERAGE = MAX_SUPPORT;
	public static final double DEFAULT_COVERAGE = MAX_COVERAGE;
	public static final double MIN_CONFIDENCE = MIN_SUPPORT;
	public static final double MAX_CONFIDENCE = MAX_SUPPORT;
	public static final double DEFAULT_CONFIDENCE = MAX_CONFIDENCE;
    public static final double MIN_FITNESS = 0;
    public static final double MAX_FITNESS = 1.0;
    public static final double UNKNOWN_FITNESS = Double.MIN_VALUE;
    /** The default value for a constraint's fitness is {@link #UNKNOWN_FITNESS UNKNOWN_FITNESS} */
	public static final double DEFAULT_FITNESS = UNKNOWN_FITNESS;
    public static final double RANGE_FOR_SUPPORT = (MAX_SUPPORT - MIN_SUPPORT);

	public double support = DEFAULT_SUPPORT;
	public double confidence = DEFAULT_CONFIDENCE;
	public double coverage = DEFAULT_COVERAGE;
	public double fitness = DEFAULT_FITNESS;
	public boolean evaluatedOnLog = false;
	public boolean belowSupportThreshold = false;
	public boolean belowConfidenceThreshold = false;
	public boolean belowCoverageThreshold = false;
	public boolean belowFitnessThreshold = false;
	private PropertyChangeSupport pcs;
	private Constraint cns;

	protected ConstraintMeasuresManager() {
	}
	
    public ConstraintMeasuresManager(Constraint cns, PropertyChangeSupport pcs) {
		this.pcs = pcs;
		this.cns = cns;
	}

	/**
     * Returns the difference between {@link #MAX_SUPPORT MAX_SUPPORT} and the given support. 
     * @param support The support to be complemented
     * @return The difference between {@link #MAX_SUPPORT MAX_SUPPORT} and the given support.
     */
    public static double complementSupport(double support) {
        return MAX_SUPPORT - support;
    }

    /**
     * Returns the difference between {@link #MAX_CONFIDENCE MAX_CONFIDENCE} and the given confidence. 
     * @param confidence The confidence to be complemented
     * @return The difference between {@link #MAX_CONFIDENCE MAX_CONFIDENCE} and the given confidence.
     */
    public static double complementConfidence(double confidence) {
        return MAX_CONFIDENCE - confidence;
    }

    /**
     * Returns the difference between {@link #MAX_COVERAGE MAX_INTEREST_FACTOR} and the given coverage. 
     * @param coverage The coverage to be complemented
     * @return The difference between {@link #MAX_COVERAGE MAX_INTEREST_FACTOR} and the given coverage.
     */
    public static double complementCoverage(double coverage) {
        return MAX_COVERAGE - coverage;
    }
    
    
    /**
     * Returns <code>true</code> if and only if the {@link #support support} of this constraint is equal to {@link #MAX_SUPPORT MAX_SUPPORT}.
     * @return <code>true</code> if the support of this constraint is equal to {@link #MAX_SUPPORT MAX_SUPPORT}, <code>false</code> otherwise.
     */
    public boolean hasMaximumSupport() {
    	return this.support == MAX_SUPPORT;
    }
    /**
     * Returns <code>true</code> if and only if the {@link #support support} of this constraint is equal to {@link ConstraintMeasuresManager#MIN_SUPPORT ConstraintMeasuresStruct.MIN_SUPPORT}.
     * @return <code>true</code> if the support of this constraint is equal to {@link ConstraintMeasuresManager#MIN_SUPPORT ConstraintMeasuresStruct.MIN_SUPPORT}, <code>false</code> otherwise.
     */
	public boolean hasLeastSupport() {
		return this.support == ConstraintMeasuresManager.MIN_SUPPORT;
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
     * Returns <code>true</code> if and only if the {@link #confidence confidence} of this constraint is equal to {@link #MAX_CONFIDENCE MAX_CONFIDENCE}.
     * @return <code>true</code> if the support of this constraint is equal to {@link #MAX_CONFIDENCE MAX_CONFIDENCE}, <code>false</code> otherwise.
     */
    public boolean hasMaximumConfidence() {
    	return this.confidence == MAX_CONFIDENCE;
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
     * Returns <code>true</code> if and only if the {@link #coverage coverage} of this constraint is equal to or higher than the given threshold.
	 * @param threshold The threshold to which the {@link #coverage coverage} of this constraint is compared
     * @return <code>true</code> if the {@link #coverage coverage} of this constraint is equal to or higher than the given threshold, <code>false</code> otherwise.
	 */
    public boolean hasSufficientCoverage(double threshold) {
    	return this.coverage >= threshold;
    }
    public boolean isAboveThresholds() {
    	return !( this.belowSupportThreshold || this.belowConfidenceThreshold || this.belowCoverageThreshold);
    }

	public boolean isBelowSupportThreshold() {
		return belowSupportThreshold;
	}
	public void setBelowSupportThreshold(boolean belowSupportThreshold) {
		if (belowSupportThreshold != this.belowSupportThreshold) {
			boolean oldbelowSupportThreshold = this.belowSupportThreshold;
			this.belowSupportThreshold = belowSupportThreshold;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.BELOW_SUPPORT_THRESHOLD.toString(), oldbelowSupportThreshold, belowSupportThreshold);
		}
	}

	public boolean isBelowConfidenceThreshold() {
		return belowConfidenceThreshold;
	}
	public void setBelowConfidenceThreshold(boolean belowConfidenceThreshold) {
		if (belowConfidenceThreshold != this.belowConfidenceThreshold) {
			boolean oldbelowConfidenceThreshold = this.belowConfidenceThreshold;
			this.belowConfidenceThreshold = belowConfidenceThreshold;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.BELOW_CONFIDENCE_THRESHOLD.toString(), oldbelowConfidenceThreshold, belowConfidenceThreshold);
		}
	}

	public boolean isBelowCoverageThreshold() {
		return belowCoverageThreshold;
	}
	public void setBelowCoverageThreshold(boolean belowCoverageThreshold) {
		if (belowCoverageThreshold != this.belowCoverageThreshold) {
			boolean oldbelowCoverageThreshold = this.belowCoverageThreshold;
			this.belowCoverageThreshold = belowCoverageThreshold;			
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.BELOW_COVERAGE_THRESHOLD.toString(), oldbelowCoverageThreshold, belowCoverageThreshold);
		}
	}

	public boolean isBelowFitnessThreshold() {
		return belowFitnessThreshold;
	}
	public void setBelowFitnessThreshold(boolean belowFitnessThreshold) {
		if (belowFitnessThreshold != this.belowSupportThreshold) {
			boolean oldbelowFitnessThreshold = this.belowFitnessThreshold;

			this.belowFitnessThreshold = belowFitnessThreshold;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.BELOW_FITNESS_THRESHOLD.toString(), oldbelowFitnessThreshold, belowFitnessThreshold);
		}
	}
	
	public double getSupport() {
		return support;
	}
	public void setSupport(double support) {
		if (this.support != support) {
			this.checkSupport(support);
			double oldSupport = this.support;
			this.support = support;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.SUPPORT.toString(), oldSupport, support);
		}
	}

	public double getConfidence() {
		return confidence;
	}
	public void setConfidence(double confidence) {
		if (this.confidence != confidence) {
			this.checkConfidence(confidence);
			double oldConfidence = this.confidence;
			this.confidence = confidence;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.CONFIDENCE.toString(), oldConfidence, confidence);
		}
	}

	public double getCoverage() {
		return coverage;
	}
	public void setCoverage(double coverage) {
		if (this.coverage != coverage) {
			this.checkCoverage(coverage);
			double oldCoverage = this.coverage;
			this.coverage = coverage;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.INTEREST_FACTOR.toString(), oldCoverage, coverage);
		}
	}

	public Double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		if (this.fitness != fitness) {
			this.checkFitness(fitness);
			double oldFitness = this.fitness;
			this.fitness = fitness;
			this.pcs.firePropertyChange(ConstraintChange.ChangedProperty.FITNESS.toString(), oldFitness, fitness);
		}
	}
	public boolean isFitnessComputed() {
		return fitness != UNKNOWN_FITNESS;
	}


    private boolean checkSupport(double support) {
        if (support < ConstraintMeasuresManager.MIN_SUPPORT || support > ConstraintMeasuresManager.MAX_SUPPORT) {
            throw new IllegalArgumentException("Provided support for " + this.cns + " out of range: " + support);
        }
        return true;
    }
    private boolean checkConfidence(double confidence) {
        if (confidence < ConstraintMeasuresManager.MIN_CONFIDENCE || confidence > ConstraintMeasuresManager.MAX_CONFIDENCE) {
            throw new IllegalArgumentException("Provided confidence level for " + this.cns + " out of range: " + confidence);
        }
        return true;
    }
    private boolean checkCoverage(double coverage) {
        if (coverage < ConstraintMeasuresManager.MIN_COVERAGE || coverage > ConstraintMeasuresManager.MAX_COVERAGE) {
            throw new IllegalArgumentException("Provided coverage for " + this.cns + " out of range: " + coverage);
        }
        return true;
    }
    private boolean checkFitness(double fitness) {
        if (fitness < ConstraintMeasuresManager.MIN_FITNESS || fitness > ConstraintMeasuresManager.MAX_FITNESS) {
            throw new IllegalArgumentException("Provided fitness for " + this.cns + " out of range: " + fitness);
        }
        return true;
    }

    /**
     * Sets all measures to the respective minima.
     */
	public void setAllMeasuresToMinimum() {
		this.confidence = MIN_CONFIDENCE;
		this.support = MIN_SUPPORT;
		this.coverage = MIN_COVERAGE;
		this.fitness = UNKNOWN_FITNESS;
	}

}