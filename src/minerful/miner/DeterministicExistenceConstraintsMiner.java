package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.AtLeast1;
import minerful.concept.constraint.existence.AtMost1;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public class DeterministicExistenceConstraintsMiner extends ExistenceConstraintsMiner {

	public DeterministicExistenceConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
		super(globalStats, taskCharArchive, tasksToQueryFor);
	}

	@Override
	protected Constraint[] discoverMinMultiplicityConstraints(TaskChar base, LocalStatsWrapper localStats, long testbedSize) {
	    for (int num: localStats.repetitions.keySet()) {
	        if (num > 0)
	            return new Constraint[] {new AtLeast1(base)};
	    }
		return null;
	}

	@Deprecated
	protected int guessLeastExistenceConstraint(LocalStatsWrapper localStats) {
	    // Did the character ever miss from the testbed case?
		if (localStats.repetitions.containsKey(0))
			return 0;
		return 1;
	    // Very very rough: a little statistical analysis on the trend would be better
	}

	@Override
	protected Constraint[] discoverMaxMultiplicityConstraints(TaskChar base, LocalStatsWrapper localStats, long testbedSize) {
		if (localStats.repetitions.containsKey(0))
			return null;
		return new Constraint[] {new AtMost1(base)};
		// Very very rough: a little statistical analysis on the trend would work better
	}

	@Deprecated
	protected int guessMaximumExistenceConstraint(LocalStatsWrapper localStats) {
	    for (int num: localStats.repetitions.keySet()) {
	        if (num > 1)
	            return Integer.MAX_VALUE;
	    }
	    return 1;
	    // Very very rough: a little statistical analysis on the trend would be better
	}

	@Override
	protected Constraint discoverInitConstraint(TaskChar base, LocalStatsWrapper localStats, long testbedSize) {
	    Constraint init = null;
	    if (!(localStats.repetitions.containsKey(0) && localStats.repetitions.get(0) > 0)) {
	        if (localStats.getAppearancesAsFirst() >= this.globalStats.logSize) {
	            return new Init(base);
	        }
	    }
	    return init;
	}
	
	@Override
	protected Constraint discoverEndConstraint(TaskChar base, LocalStatsWrapper localStats, long testbedSize) {
	    Constraint end = null;
	    if (!(localStats.repetitions.containsKey(0) && localStats.repetitions.get(0) > 0)) {
	        if (localStats.getAppearancesAsLast() >= this.globalStats.logSize) {
	            return new End(base);
	        }
	    }
	    return end;
	}
}