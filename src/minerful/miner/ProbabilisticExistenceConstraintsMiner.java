package minerful.miner;

import java.util.Collection;
import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.RelationConstraint;
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

        	// Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it.
        	// Thus, it is perfectly useless to indagate over it!
        	if (localStats.getTotalAmountOfOccurrences() > 0) {
	        	Constraint participation = this.discoverParticipationConstraint(pivot, localStats, this.globalStats.logSize);
	        	pivotParticipationFraction = participation.getSupport();

	        	updateConstraint(constraintsBag, pivot, participation, participation.getSupport(), pivotParticipationFraction);

	        	Constraint atMostOne = this.discoverAtMostOnceConstraint(pivot, localStats, this.globalStats.logSize);
	        	updateConstraint(constraintsBag, pivot, atMostOne, atMostOne.getSupport(), pivotParticipationFraction);
	            
	        	Constraint init = this.discoverInitConstraint(pivot, localStats, this.globalStats.logSize);
	        	updateConstraint(constraintsBag, pivot, init, init.getSupport(), pivotParticipationFraction);
	            
	            Constraint end = this.discoverEndConstraint(pivot, localStats, this.globalStats.logSize);
	        	updateConstraint(constraintsBag, pivot, end, end.getSupport(), pivotParticipationFraction);
	            
	            if (hasValuesAboveThresholds(participation)) this.computedConstraintsAboveThresholds++;
	            if (hasValuesAboveThresholds(atMostOne)) this.computedConstraintsAboveThresholds++;
	            if (hasValuesAboveThresholds(init)) this.computedConstraintsAboveThresholds++;
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
    protected Constraint discoverParticipationConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
        long zeroAppearances = 0;
        if (localStats.repetitions.containsKey(0)) {
            zeroAppearances += localStats.repetitions.get(0);
        }
        double oppositeSupport =
                (double) zeroAppearances / (double) testbedSize;
        return new Participation(base, Constraint.complementSupport(oppositeSupport));
    }

    @Override
    protected Constraint discoverAtMostOnceConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize) {
        long appearancesAsUpToOne = 0;
        if (localStats.repetitions.containsKey(1)) {
            appearancesAsUpToOne += localStats.repetitions.get(1);
            if (localStats.repetitions.containsKey(0)) {
                appearancesAsUpToOne += localStats.repetitions.get(0);
               }
        }
        double support =
                (double) appearancesAsUpToOne / (double) testbedSize;
        return new AtMostOne(base, support);
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
