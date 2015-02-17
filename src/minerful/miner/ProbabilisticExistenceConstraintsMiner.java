package minerful.miner;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.Init;
import minerful.concept.constraint.existence.Participation;
import minerful.concept.constraint.existence.Uniqueness;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;

public class ProbabilisticExistenceConstraintsMiner extends ExistenceConstraintsMiner {

    public ProbabilisticExistenceConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive) {
        super(globalStats, taskCharArchive);
    }
    
    @Override
    public TaskCharRelatedConstraintsBag discoverConstraints(TaskCharRelatedConstraintsBag constraintsBag) {
        if (constraintsBag == null) {
            constraintsBag = new TaskCharRelatedConstraintsBag(this.taskCharArchive.getTaskChars());
        }
        LocalStatsWrapper localStats = null;
        TaskChar base = null;
        double baseParticipationFraction = 0.0;

        for (Character character: this.globalStats.alphabet) {
        	localStats = this.globalStats.statsTable.get(character);

        	base = taskCharArchive.getTaskChar(character);

        	// Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it.
        	// Thus, it is perfectly useless to indagate over it!
        	if (localStats.getTotalAmountOfAppearances() > 0) {
	        	Constraint participation = this.discoverParticipationConstraint(base, localStats, this.globalStats.logSize);
	        	baseParticipationFraction = participation.support;

	        	refineByComputingInterestLevels(participation, baseParticipationFraction);
	    		constraintsBag.add(base, participation);

	            Constraint uniqueness = this.discoverUniquenessConstraint(base, localStats, this.globalStats.logSize);
	            refineByComputingInterestLevels(uniqueness, baseParticipationFraction);
	        	constraintsBag.add(base, uniqueness);
	            
	            Constraint init = this.discoverInitConstraint(base, localStats, this.globalStats.logSize);
	            refineByComputingInterestLevels(init, baseParticipationFraction);
	            constraintsBag.add(base, init);
	            
	            Constraint end = this.discoverEndConstraint(base, localStats, this.globalStats.logSize);
	            refineByComputingInterestLevels(end, baseParticipationFraction);
	            constraintsBag.add(base, end);
	            
	            if (hasValuesAboveThresholds(participation)) this.computedConstraintsAboveThresholds++;
	            if (hasValuesAboveThresholds(uniqueness)) this.computedConstraintsAboveThresholds++;
	            if (hasValuesAboveThresholds(init)) this.computedConstraintsAboveThresholds++;
	            if (hasValuesAboveThresholds(end)) this.computedConstraintsAboveThresholds++;
        	}
        }
        return constraintsBag;
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
    protected Constraint discoverUniquenessConstraint(TaskChar base,
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
        return new Uniqueness(base, support);
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

    public static Constraint refineByComputingConfidenceLevel(Constraint con, double baseParticipationFraction) {
    	con.confidence = con.support * baseParticipationFraction;
    	return con;
    }
    
    public static Constraint refineByComputingInterestLevels(Constraint con, double baseParticipationFraction) {
    	refineByComputingConfidenceLevel(con, baseParticipationFraction);
    	con.interestFactor = con.support * baseParticipationFraction * baseParticipationFraction;
    	return con;
    }
}
