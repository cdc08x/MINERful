package minerful.miner;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.miner.stats.GlobalStatsTable;

import org.apache.log4j.Logger;

public abstract class ConstraintsMiner implements IConstraintsMiner {
	
	protected static Logger logger;

	protected GlobalStatsTable globalStats;
	protected TaskCharArchive taskCharArchive;
	
	protected Double supportThreshold = null;
	protected Double confidenceThreshold = null;
	protected Double interestFactorThreshold = null;
	
	protected long computedConstraintsAboveThresholds = 0;
	
    public ConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive) {
        this.globalStats = globalStats;
        this.taskCharArchive = taskCharArchive;
        if (logger == null)
    		logger = Logger.getLogger(ConstraintsMiner.class.getCanonicalName());
    }

	@Override
	public Double getSupportThreshold() {
		return supportThreshold;
	}
	@Override
	public void setSupportThreshold(Double supportThreshold) {
		this.supportThreshold = supportThreshold;
	}

	@Override
	public Double getConfidenceThreshold() {
		return confidenceThreshold;
	}
	@Override
	public void setConfidenceThreshold(Double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}

	@Override
	public Double getInterestFactorThreshold() {
		return interestFactorThreshold;
	}
	@Override
	public void setInterestFactorThreshold(Double interestFactorThreshold) {
		this.interestFactorThreshold = interestFactorThreshold;
	}

	@Override
	public TaskCharRelatedConstraintsBag discoverConstraints() {
		return this.discoverConstraints(null);
	}

    static int computeHeuristicSizeForHashSets(int supposedCapacity) {
    	return supposedCapacity * 2;
    }
    
    static Set<Constraint> makeTemporarySet(int supposedCapacity) {
    	return new HashSet<Constraint>(computeHeuristicSizeForHashSets(supposedCapacity));
    }
    
    static Set<Constraint> makeTemporarySet() {
    	return new TreeSet<Constraint>();
    }
    
    static SortedSet<Constraint> makeNavigableSet(Set<Constraint> temporarySet) {
    	return new TreeSet<Constraint>(temporarySet);
    }

    static SortedSet<Constraint> makeNavigableSet() {
    	return new TreeSet<Constraint>();
    }

	@Override
	public boolean hasSufficientSupport(Constraint c) {
		return (this.supportThreshold == null ? true : c.hasSufficientSupport(this.supportThreshold));
	}
	@Override
	public boolean hasSufficientConfidence(Constraint c) {
		return (this.confidenceThreshold == null ? true : c.hasSufficientConfidence(this.confidenceThreshold));
	}
	@Override
	public boolean hasSufficientInterestFactor(Constraint c) {
		return (this.interestFactorThreshold == null ? true : c.hasSufficientSupport(this.interestFactorThreshold));
	}
	@Override
	public boolean hasValuesAboveThresholds(Constraint c) {
		return this.hasSufficientSupport(c) && this.hasSufficientConfidence(c) && this.hasSufficientInterestFactor(c);
	}
	
	@Override
	public long getComputedConstraintsAboveTresholds() {
		return computedConstraintsAboveThresholds;
	}
}