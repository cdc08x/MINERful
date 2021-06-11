package minerful.miner;

import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMost1;
import minerful.concept.constraint.existence.AtMost2;
import minerful.concept.constraint.existence.AtMost3;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Absence;
import minerful.concept.constraint.existence.AtLeast1;
import minerful.concept.constraint.existence.AtLeast3;
import minerful.concept.constraint.existence.AtLeast2;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public class ProbabilisticExistenceConstraintsMiner extends ExistenceConstraintsMiner {

    public ProbabilisticExistenceConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
        super(globalStats, taskCharArchive, tasksToQueryFor);
    }
    
    @Override
    public ConstraintsBag discoverConstraints(ConstraintsBag constraintsBag) {
        if (constraintsBag == null) {
            constraintsBag = new ConstraintsBag(this.tasksToQueryFor);
        }
        LocalStatsWrapper localStats = null;
        double pivotParticipationFraction = 0.0;

        for (TaskChar pivot: tasksToQueryFor) {
        	localStats = this.globalStats.statsTable.get(pivot);

        	if (localStats.getTotalAmountOfOccurrences() > 0) {
	        	Constraint[] minMultiCns = this.discoverMinMultiplicityConstraints(pivot, localStats, this.globalStats.logSize);
	        	pivotParticipationFraction = minMultiCns[0].getSupport();

	        	for (Constraint minMultiCn : minMultiCns) {
	        		updateConstraint(constraintsBag, pivot, minMultiCn, minMultiCn.getSupport(), pivotParticipationFraction);
	        		if (hasValuesAboveThresholds(minMultiCn)) this.computedConstraintsAboveThresholds++;
	        	}	

	        	Constraint[] maxMultiCns = this.discoverMaxMultiplicityConstraints(pivot, localStats, this.globalStats.logSize);

	        	for (Constraint maxMultiCn : maxMultiCns) {
	        		updateConstraint(constraintsBag, pivot, maxMultiCn, maxMultiCn.getSupport(), pivotParticipationFraction);
	        		if (hasValuesAboveThresholds(maxMultiCn)) this.computedConstraintsAboveThresholds++;
	        	}	
	            
	        	Constraint init = this.discoverInitConstraint(pivot, localStats, this.globalStats.logSize);
	        	updateConstraint(constraintsBag, pivot, init, init.getSupport(), pivotParticipationFraction);
	            if (hasValuesAboveThresholds(init)) this.computedConstraintsAboveThresholds++;
	            
	            Constraint end = this.discoverEndConstraint(pivot, localStats, this.globalStats.logSize);
	        	updateConstraint(constraintsBag, pivot, end, end.getSupport(), pivotParticipationFraction);
	            if (hasValuesAboveThresholds(end)) this.computedConstraintsAboveThresholds++;
        	}
        }
        return constraintsBag;
    }

	protected Constraint updateConstraint(ConstraintsBag constraintsBag,
			TaskChar indexingParam, Constraint searchedCon,
			double support, double pivotParticipationFraction) {
		Constraint con = constraintsBag.getOrAdd(indexingParam, searchedCon);
		con.setSupport(support);
		con.setEvaluatedOnLog(true);
		refineByComputingRelevanceMetrics(con, pivotParticipationFraction);
		return con;
	}

    public static Constraint refineByComputingRelevanceMetrics(Constraint con, double pivotParticipationFraction) {
    	con.setConfidence(con.getSupport() * pivotParticipationFraction);
    	con.setInterestFactor(con.getSupport() * pivotParticipationFraction * pivotParticipationFraction);
    	return con;
    }

    @Override
    protected Constraint[] discoverMinMultiplicityConstraints(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
        long zeroOccurrences = 0, singleOrNoOccurrences = 0, upToTwoOccurrences = 0;
        if (localStats.repetitions.containsKey(0)) {
            zeroOccurrences = localStats.repetitions.get(0);
        }
        if (localStats.repetitions.containsKey(1)) {
        	singleOrNoOccurrences = zeroOccurrences + localStats.repetitions.get(1);
        }	else singleOrNoOccurrences = zeroOccurrences;
        if (localStats.repetitions.containsKey(2)) {
        	upToTwoOccurrences = singleOrNoOccurrences + localStats.repetitions.get(2);
        }	else upToTwoOccurrences = singleOrNoOccurrences;
        
        return new Constraint[] {
        		new AtLeast1(base, Constraint.complementSupport((double) zeroOccurrences / (double) testbedSize)),
        		new AtLeast2(base, Constraint.complementSupport((double) singleOrNoOccurrences / (double) testbedSize)),
        		new AtLeast3(base, Constraint.complementSupport((double) upToTwoOccurrences / (double) testbedSize)),
        };
    }

    @Override
    protected Constraint[] discoverMaxMultiplicityConstraints(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
    	long zeroOccurrences = 0, singleOrNoOccurrences = 0, upToTwoOccurrences = 0, upToThreeOccurrences = 0;
    	if (localStats.repetitions.containsKey(0)) {
    		zeroOccurrences = localStats.repetitions.get(0);
    	}
        if (localStats.repetitions.containsKey(1)) {
        	singleOrNoOccurrences = zeroOccurrences + localStats.repetitions.get(1);
        }	else singleOrNoOccurrences = zeroOccurrences;
        if (localStats.repetitions.containsKey(2)) {
        	upToTwoOccurrences = singleOrNoOccurrences + localStats.repetitions.get(2);
        }	else upToTwoOccurrences = singleOrNoOccurrences;
        if (localStats.repetitions.containsKey(3)) {
        	upToThreeOccurrences = upToTwoOccurrences + localStats.repetitions.get(3);
        }	else upToThreeOccurrences = upToTwoOccurrences;
        return new Constraint[] {
        		new Absence(base, ((double) zeroOccurrences / (double) testbedSize)),
        		new AtMost1(base, ((double) singleOrNoOccurrences / (double) testbedSize)),
        		new AtMost2(base, ((double) upToTwoOccurrences / (double) testbedSize)),
        		new AtMost3(base, ((double) upToThreeOccurrences / (double) testbedSize)),
        };
    }

    @Override
    protected Constraint discoverInitConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
//    	if (!(localStats.repetitions.containsKey(0) && (localStats.repetitions.get(0) > 0))) {
    		if (localStats.getAppearancesAsFirst() >= testbedSize) {
                return new Init(base);
            } else {
                return new Init(base, ((double) localStats.getAppearancesAsFirst() / (double) testbedSize));
            }
//        }
//        return new Init(base, 0);
    }

    @Override
    protected Constraint discoverEndConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
//        if (!(localStats.repetitions.containsKey(0) && localStats.repetitions.get(0) > 0)) {
            if (localStats.getAppearancesAsLast() >= testbedSize) {
                return new End(base);
            } else {
                return new End(base, ((double) localStats.getAppearancesAsLast() / (double) testbedSize));
            }
//        }
//        return new End(base, 0);
    }
}
