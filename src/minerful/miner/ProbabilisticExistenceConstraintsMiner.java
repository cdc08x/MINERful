package minerful.miner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintMeasuresManager;
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
        Constraint[] minMultiCns = {}, maxMultiCns = {};
        Constraint init = null, end = null;
        		

        for (TaskChar pivot: tasksToQueryFor) {
        	localStats = this.globalStats.statsTable.get(pivot);

        	if (localStats.getTotalAmountOfOccurrences() > 0) {
	        	minMultiCns = this.discoverMinMultiplicityConstraints(pivot, localStats, this.globalStats.logSize, this.globalStats.numOfEvents);

	        	for (Constraint minMultiCn : minMultiCns) {
	        		this.updateConstraintInBag(constraintsBag, pivot, minMultiCn);
	        		if (hasValuesAboveThresholds(minMultiCn)) this.computedConstraintsAboveThresholds++;
	        	}	

	        	maxMultiCns = this.discoverMaxMultiplicityConstraints(pivot, localStats, this.globalStats.logSize, this.globalStats.numOfEvents);

	        	for (Constraint maxMultiCn : maxMultiCns) {
	        		this.updateConstraintInBag(constraintsBag, pivot, maxMultiCn);
	        		if (hasValuesAboveThresholds(maxMultiCn)) this.computedConstraintsAboveThresholds++;
	        	}	
	            
	        	init = this.discoverInitConstraint(pivot, localStats, this.globalStats.logSize, this.globalStats.numOfEvents);
	        	this.updateConstraintInBag(constraintsBag, pivot, init);
	            if (hasValuesAboveThresholds(init)) this.computedConstraintsAboveThresholds++;
	            
	            end = this.discoverEndConstraint(pivot, localStats, this.globalStats.logSize, this.globalStats.numOfEvents);
	        	this.updateConstraintInBag(constraintsBag, pivot, end);
	            if (hasValuesAboveThresholds(end)) this.computedConstraintsAboveThresholds++;
        	}
        }
        return constraintsBag;
    }
    
	protected Constraint updateConstraintInBag(ConstraintsBag constraintsBag,
			TaskChar indexingParam, Constraint discoveredCon) {
		Constraint con = constraintsBag.getOrAdd(indexingParam, discoveredCon);       
        con.getEventBasedMeasures().setConfidence(discoveredCon.getEventBasedMeasures().getConfidence());
        con.getEventBasedMeasures().setSupport(discoveredCon.getEventBasedMeasures().getSupport());
        con.getEventBasedMeasures().setCoverage(discoveredCon.getEventBasedMeasures().getCoverage());
		refineByComputingOtherMetricsThanEventBasedConfidence(con);
		con.setEvaluatedOnLog(true);
		return con;
	}

	/**
	 * Enriches the con constraint with support and coverage measures.
	 * Notice that we take existence constraints as if their activator is the trace-start event.
	 * Therefore, confidence is the same as support, and coverage is equal to 1.
	 * @param con The constraint to assign additional measures to.
	 * @return The updated constraint
	 */
    public static Constraint refineByComputingOtherMetricsThanEventBasedConfidence(Constraint con) {
    	con.getTraceBasedMeasures().setConfidence(con.getEventBasedMeasures().getConfidence()); // because we consider existence constraints as activated by the start of the trace, which occurs —suprirse surprise!— once per trace 
    	con.getTraceBasedMeasures().setSupport(con.getEventBasedMeasures().getConfidence()); // because we consider existence constraints as activated by the start of the trace, which occurs —suprirse surprise!— once per trace 
    	con.getTraceBasedMeasures().setCoverage(1.0); // coverage includes all traces in which the activator occurred at least once. So, always.
        
        return con;
    }

    @Override
    protected Constraint[] discoverMinMultiplicityConstraints(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize, long numOfEventsInLog) {
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

        Constraint atLe1 = new AtLeast1(base), atLe2 = new AtLeast2(base), atLe3 = new AtLeast3(base);
        
        atLe1.getEventBasedMeasures().setConfidence(ConstraintMeasuresManager.complementConfidence((double) zeroOccurrences / (double) testbedSize)); // because we consider existence constraints as activated by the start of the trace, which occurs —suprirse surprise!— once per trace 
        atLe2.getEventBasedMeasures().setConfidence(ConstraintMeasuresManager.complementConfidence((double) singleOrNoOccurrences / (double) testbedSize));
        atLe3.getEventBasedMeasures().setConfidence(ConstraintMeasuresManager.complementConfidence((double) upToTwoOccurrences / (double) testbedSize)); // because we consider existence constraints as activated by the start of the trace, which occurs —suprirse surprise!— once per trace 

        atLe1.getEventBasedMeasures().setSupport(((double) (testbedSize - zeroOccurrences) / (double) numOfEventsInLog));
        atLe2.getEventBasedMeasures().setSupport((double) (testbedSize - singleOrNoOccurrences) / (double) numOfEventsInLog);
        atLe3.getEventBasedMeasures().setSupport((double) (testbedSize - upToTwoOccurrences) / (double) numOfEventsInLog); 
        
        Constraint[] newCons = new Constraint[] {atLe1, atLe2, atLe3};

        for (Constraint con: newCons){
            con.getEventBasedMeasures().setCoverage((double)testbedSize / (double)numOfEventsInLog);
        }


        return newCons;
    }

    @Override
    protected Constraint[] discoverMaxMultiplicityConstraints(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize, long numOfEventsInLog) {
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
        
        Constraint abse = new Absence(base), atMo1 = new AtMost1(base), atMo2 = new AtMost2(base), atMo3 = new AtMost3(base);
        
        abse.getEventBasedMeasures().setConfidence((double) zeroOccurrences / (double) testbedSize);
        atMo1.getEventBasedMeasures().setConfidence((double) singleOrNoOccurrences / (double) testbedSize);
        atMo2.getEventBasedMeasures().setConfidence((double) upToTwoOccurrences / (double) testbedSize);
        atMo3.getEventBasedMeasures().setConfidence((double) upToThreeOccurrences / (double) testbedSize);

        abse.getEventBasedMeasures().setSupport((double) zeroOccurrences / (double) numOfEventsInLog);
        atMo1.getEventBasedMeasures().setSupport((double) singleOrNoOccurrences / (double) numOfEventsInLog);
        atMo2.getEventBasedMeasures().setSupport((double) upToTwoOccurrences / (double) numOfEventsInLog);
        atMo3.getEventBasedMeasures().setSupport((double) upToThreeOccurrences / (double) numOfEventsInLog);
        
        Constraint[] newCons = new Constraint[] {abse, atMo1, atMo2, atMo3};

        for (Constraint con: newCons){
            con.getEventBasedMeasures().setCoverage((double)testbedSize / (double)numOfEventsInLog);
        }
        
        return newCons;
    }

    @Override
    protected Constraint discoverInitConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize, long numOfEventsInLog) {
//    	if (!(localStats.repetitions.containsKey(0) && (localStats.repetitions.get(0) > 0))) {
    		if (localStats.getOccurrencesAsFirst() >= testbedSize) {
                return new Init(base);
            } else {
            	Constraint init = new Init(base);
            	init.getEventBasedMeasures().setConfidence((double) localStats.getOccurrencesAsFirst() / (double) testbedSize);
            	init.getEventBasedMeasures().setSupport((double) localStats.getOccurrencesAsFirst() / (double) numOfEventsInLog);
                init.getEventBasedMeasures().setCoverage((double)testbedSize / (double)numOfEventsInLog);
            	return init;
            }
//        }
//        return new Init(base, 0);
    }

    @Override
    protected Constraint discoverEndConstraint(TaskChar base,
            LocalStatsWrapper localStats, long testbedSize, long numOfEventsInLog) {
//        if (!(localStats.repetitions.containsKey(0) && localStats.repetitions.get(0) > 0)) {
            if (localStats.getOccurrencesAsLast() >= testbedSize) {
                return new End(base);
            } else {
            	Constraint end = new End(base);
            	end.getEventBasedMeasures().setConfidence((double) localStats.getOccurrencesAsLast() / (double) testbedSize);
            	end.getEventBasedMeasures().setSupport((double) localStats.getOccurrencesAsLast() / (double) numOfEventsInLog);
                end.getEventBasedMeasures().setCoverage((double)testbedSize / (double)numOfEventsInLog);
            	return end;
            }
//        }
//        return new End(base, 0);
    }
}
