package minerful.miner;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.miner.stats.GlobalStatsTable;

public abstract class AbstractConstraintsMiner implements ConstraintsMiner {
	
	protected static Logger logger;

	protected GlobalStatsTable globalStats;
	protected TaskCharArchive taskCharArchive;
	protected Set<TaskChar> tasksToQueryFor;
	
	protected Double supportThreshold = null;
	protected Double confidenceThreshold = null;
	protected Double coverageThreshold = null;	
	protected Double trcSupportThreshold = null;
	protected Double trcConfidenceThreshold = null;
	protected Double trcCoverageThreshold = null;
	
	protected long computedConstraintsAboveThresholds = 0;

	
    public AbstractConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
        this.globalStats = globalStats;
        this.taskCharArchive = taskCharArchive;
        this.tasksToQueryFor = tasksToQueryFor;
        if (logger == null)
    		logger = Logger.getLogger(AbstractConstraintsMiner.class.getCanonicalName());
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
	public Double getCoverageThreshold() {
		return coverageThreshold;
	}
	@Override
	public void setCoverageThreshold(Double coverageThreshold) {
		this.coverageThreshold = coverageThreshold;
	}
	@Override
	public Double getTrcSupportThreshold() {
		return trcSupportThreshold;
	}
	public void setTrcSupportThreshold(Double trcSupportThreshold) {
		this.trcSupportThreshold = trcSupportThreshold;
	}
	@Override
	public Double getTrcConfidenceThreshold() {
		return trcConfidenceThreshold;
	}
	@Override
	public void setTrcConfidenceThreshold(Double trcConfidenceThreshold) {
		this.trcConfidenceThreshold = trcConfidenceThreshold;
	}
	@Override
	public Double getTrcCoverageThreshold() {
		return trcCoverageThreshold;
	}
	@Override
	public void setTrcCoverageThreshold(Double trcCoverageThreshold) {
		this.trcCoverageThreshold = trcCoverageThreshold;
	}

	@Override
	public Set<TaskChar> getTasksToQueryFor() {
		return tasksToQueryFor;
	}

	@Override
	public ConstraintsBag discoverConstraints() {
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
	public boolean hasSufficientEvtSupport(Constraint c) {
		return (this.supportThreshold == null ? true : c.getEventBasedMeasures().hasSufficientSupport(this.supportThreshold));
	}
	@Override
	public boolean hasSufficientEvtConfidence(Constraint c) {
		return (this.confidenceThreshold == null ? true : c.getEventBasedMeasures().hasSufficientConfidence(this.confidenceThreshold));
	}
	@Override
	public boolean hasSufficientEvtCoverage(Constraint c) {
		return (this.coverageThreshold == null ? true : c.getEventBasedMeasures().hasSufficientCoverage(this.coverageThreshold));
	}

	@Override
	public boolean hasSufficientTrcSupport(Constraint c) {
		return (this.trcSupportThreshold == null ? true : c.getTraceBasedMeasures().hasSufficientSupport(this.trcSupportThreshold));
	}
	@Override
	public boolean hasSufficientTrcConfidence(Constraint c) {
		return (this.trcConfidenceThreshold == null ? true : c.getTraceBasedMeasures().hasSufficientConfidence(this.trcConfidenceThreshold));
	}
	@Override
	public boolean hasSufficientTrcCoverage(Constraint c) {
		return (this.trcCoverageThreshold == null ? true : c.getTraceBasedMeasures().hasSufficientCoverage(this.trcCoverageThreshold));
	}
	
	@Override
	public boolean hasValuesAboveThresholds(Constraint c) {
		return this.hasSufficientEvtSupport(c) && this.hasSufficientEvtConfidence(c) && this.hasSufficientEvtCoverage(c) &&
				this.hasSufficientTrcSupport(c) && this.hasSufficientTrcConfidence(c) && this.hasSufficientTrcCoverage(c);
	}
	
	@Override
	public long getComputedConstraintsAboveTresholds() {
		return computedConstraintsAboveThresholds;
	}
}