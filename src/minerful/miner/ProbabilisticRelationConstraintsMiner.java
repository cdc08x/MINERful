/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.NotChainPrecedence;
import minerful.concept.constraint.relation.NotChainResponse;
import minerful.concept.constraint.relation.NotPrecedence;
import minerful.concept.constraint.relation.NotRespondedExistence;
import minerful.concept.constraint.relation.NotResponse;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

public class ProbabilisticRelationConstraintsMiner extends RelationConstraintsMiner {
	/**
	 * The measures with which constraint interestingness is assessed.
	 */
	public static class ConstraintMeasures {
		public double support;
		public double confidence;
		public double coverage;
	}

	private static Logger logger = Logger.getLogger(ProbabilisticRelationConstraintsMiner.class.getCanonicalName());
	
	private final boolean foreseeingDistances;

    public ProbabilisticRelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor) {
        super(globalStats, taskCharArchive, tasksToQueryFor);
        this.foreseeingDistances = false;
    }

    public ProbabilisticRelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, Set<TaskChar> tasksToQueryFor, boolean foreseeingDistances) {
    	super(globalStats, taskCharArchive, tasksToQueryFor);
    	this.foreseeingDistances = foreseeingDistances;
    }
    
    @Override
    public ConstraintsBag discoverConstraints(ConstraintsBag constraintsBag) {
        // Initialisation
        if (constraintsBag == null) {
            constraintsBag = new ConstraintsBag(tasksToQueryFor);
        }
        LocalStatsWrapper auxLocalStats = null;
//        Set<Constraint> auxRelCons = super.makeTemporarySet(
//        		MetaConstraintUtils.howManyPossibleConstraints(tasksToQueryFor.size(), taskCharArchive.size()));
//        for (TaskChar tChUnderAnalysis : tasksToQueryFor) {
//            auxLocalStats = this.globalStats.statsTable.get(tChUnderAnalysis);
//            // Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
//            if (auxLocalStats.getTotalAmountOfOccurrences() > 0) {
//                auxRelCons.addAll(
//                 this.discoverRelationConstraints(tChUnderAnalysis));
//            }
//        }
//        for (Constraint relCon : auxRelCons) {
//            constraintsBag.add(relCon.base, relCon);
//        }
//        return constraintsBag;
        for (TaskChar tChUnderAnalysis : tasksToQueryFor) {
        	auxLocalStats = this.globalStats.statsTable.get(tChUnderAnalysis);
        	// Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
        	if (auxLocalStats.getTotalAmountOfOccurrences() > 0) {
        		this.discoverRelationConstraints(tChUnderAnalysis, constraintsBag);
        	}
        }
        return constraintsBag;
    }

    // Very very rough: a little statistical analysis on the trend would be better
    @Override
    public Set<? extends Constraint> discoverRelationConstraints(TaskChar pivotTask, ConstraintsBag constraintsBag) {
        double	pivotParticipationFraction = 0.0,
		        searchedParticipationFraction = 0.0;
        StatsCell   interplayStats = null,
                    reversedInterplayStats = null;
        Set<Constraint> nuRelaCons = super.makeNavigableSet();
        Constraint[] nuCons = {};
        	
        LocalStatsWrapper
                pivotLocalStats = globalStats.statsTable.get(pivotTask),
                searchedLocalStats = null;
        long   pivotOccurrences = pivotLocalStats.getTotalAmountOfOccurrences(),
                searchedOccurrences = 0L;
        
        // For each other character
        for (TaskChar searchedTask : pivotLocalStats.interplayStatsTable.keySet()) {
        	nuRelaCons = super.makeNavigableSet();
            pivotParticipationFraction = pivotLocalStats.getTotalAmountOfTracesWithOccurrence();

            if (!searchedTask.equals(pivotTask)) {
                searchedLocalStats = globalStats.statsTable.get(searchedTask);
                interplayStats = pivotLocalStats.interplayStatsTable.get(searchedTask);
                reversedInterplayStats = searchedLocalStats.interplayStatsTable.get(pivotTask);

                // TODO Make this customisable
                nuCons = new Constraint[]{
                		constraintsBag.getOrAdd(pivotTask, new RespondedExistence(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new Response(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new AlternateResponse(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new ChainResponse(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new Precedence(searchedTask, pivotTask)),
                		constraintsBag.getOrAdd(pivotTask, new AlternatePrecedence(searchedTask, pivotTask)),
                		constraintsBag.getOrAdd(pivotTask, new ChainPrecedence(searchedTask, pivotTask)),
                		constraintsBag.getOrAdd(pivotTask, new NotRespondedExistence(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new NotResponse(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new NotChainResponse(pivotTask, searchedTask)),
                		constraintsBag.getOrAdd(pivotTask, new NotPrecedence(searchedTask, pivotTask)),
                		constraintsBag.getOrAdd(pivotTask, new NotChainPrecedence(searchedTask, pivotTask)),
                };
                
                for (Constraint nuCon : nuCons) {
                	nuRelaCons.add(
                			this.measureConstraint(
	                					nuCon,
	                					pivotLocalStats,
	                					interplayStats,
	                					searchedLocalStats,
	                					globalStats.numOfEvents,
	                					globalStats.logSize)
                			);
                	nuCon.setEvaluatedOnLog(true);
                }
            }

            Iterator<Constraint> constraintsIterator = nuRelaCons.iterator();
            RelationConstraint currentConstraint = null;
            while (constraintsIterator.hasNext()) {
            	currentConstraint = (RelationConstraint) constraintsIterator.next();
            	if (this.isForeseeingDistances()) {
            		if (currentConstraint.getImplicationVerse() == ConstraintImplicationVerse.BACKWARD)
            			refineByComputingDistances(currentConstraint, searchedLocalStats, pivotTask);
            		else
            			refineByComputingDistances(currentConstraint, pivotLocalStats, searchedTask);
            	}
            	
            	if (hasValuesAboveThresholds(currentConstraint)) this.computedConstraintsAboveThresholds++;
            }

        }
        return nuRelaCons;
    }
	
	private Constraint measureConstraint(
			Constraint nuCon,
			LocalStatsWrapper pivotLocalStats,
			StatsCell interplayStats,
			LocalStatsWrapper searchedLocalStats,
			long numOfEventsInLog,
			long numOfTracesInLog) {
		Class<? extends Constraint> conClass = nuCon.getClass();
		double	satEvtNum = 0.0,
				satTrcNum = 0.0;
		long	pivotOccurrences = pivotLocalStats.getTotalAmountOfOccurrences(),
				tracesWithPivot = pivotLocalStats.getTotalAmountOfTracesWithOccurrence();
		if (conClass.equals(RespondedExistence.class)) {
			satEvtNum = pivotLocalStats.getTotalAmountOfOccurrences() - interplayStats.howManyTimesItNeverOccurredAtAll();
			satTrcNum = tracesWithPivot - interplayStats.inHowManyTracesItNeverOccurredAtAll();
		}
		else if (conClass.equals(Response.class)) {
			satEvtNum = pivotOccurrences - interplayStats.howManyTimesItNeverOccurredOnwards();
			satTrcNum = tracesWithPivot - interplayStats.inHowManyTracesItNeverOccurredOnwards();
		}
		else if (conClass.equals(AlternateResponse.class)) {
			satEvtNum = pivotOccurrences - interplayStats.howManyTimesItNeverOccurredOnwards() - interplayStats.inBetweenRepsOnwards;
			satTrcNum = tracesWithPivot - interplayStats.inHowManyTracesItNeverOccurredOnwards() - interplayStats.tracesWithInBetweenRepsOnwards;
		}
		else if (conClass.equals(ChainResponse.class)) {
			if (interplayStats.distances.get(1) != null) {
				satEvtNum = interplayStats.distances.get(1);
			}
			if (interplayStats.distancesPerTrace.get(1) != null) {
				satTrcNum = interplayStats.distancesPerTrace.get(1);
			}
		}
		else if (conClass.equals(Precedence.class)) {
			satEvtNum = pivotOccurrences - interplayStats.howManyTimesItNeverOccurredBackwards();
			satTrcNum = tracesWithPivot - interplayStats.inHowManyTracesItNeverOccurredBackwards();
		}
		else if (conClass.equals(AlternatePrecedence.class)) {
			satEvtNum = pivotOccurrences - interplayStats.howManyTimesItNeverOccurredBackwards() - interplayStats.inBetweenRepsBackwards;
			satTrcNum = tracesWithPivot - interplayStats.inHowManyTracesItNeverOccurredBackwards() - interplayStats.tracesWithInBetweenRepsBackwards;
		}
		else if (conClass.equals(ChainPrecedence.class)) {
			if (interplayStats.distances.get(-1) != null) {
				satEvtNum = interplayStats.distances.get(-1);
			}
			if (interplayStats.distancesPerTrace.get(-1) != null) {
				satTrcNum = interplayStats.distancesPerTrace.get(-1);
			}
		}
		else if (conClass.equals(NotRespondedExistence.class)) {
			satEvtNum = interplayStats.howManyTimesItNeverOccurredAtAll();
			satTrcNum = interplayStats.inHowManyTracesItNeverOccurredAtAll();
		}
		else if (conClass.equals(NotResponse.class)) {
			satEvtNum = interplayStats.howManyTimesItNeverOccurredOnwards();
			satTrcNum = interplayStats.inHowManyTracesItNeverOccurredOnwards();
		}
		else if (conClass.equals(NotChainResponse.class)) {
			satEvtNum = pivotOccurrences;
			satTrcNum = tracesWithPivot;
			if (interplayStats.distances.get(1) != null) {
				satEvtNum -= interplayStats.distances.get(1);
			}
			if (interplayStats.distancesPerTrace.get(1) != null) {
				satTrcNum -= interplayStats.distancesPerTrace.get(1);
			}
		}
		else if (conClass.equals(NotPrecedence.class)) {
			satEvtNum = interplayStats.howManyTimesItNeverOccurredBackwards();
			satTrcNum = interplayStats.inHowManyTracesItNeverOccurredBackwards();
		}
		else if (conClass.equals(NotChainPrecedence.class)) {
			satEvtNum = pivotOccurrences;
			satTrcNum = tracesWithPivot;
			if (interplayStats.distances.get(-1) != null) {
				satEvtNum -= interplayStats.distances.get(-1);
			}
			if (interplayStats.distancesPerTrace.get(-1) != null) {
				satTrcNum -= interplayStats.distancesPerTrace.get(-1);
			}
		}
		else {
			throw new IllegalArgumentException("The computation of interestingness measures for the given class (" + conClass.getSimpleName() + ") is not (yet!) possible");
		}
		
//		logger.trace(String.format("There are %2$f satisfying occurrences for %1$s. The support is thus %2$f / %3$d = %4$f",
//				constraint, satOccurs, numOfEventsInLog, satOccurs / numOfEventsInLog));
		nuCon.getEventBasedMeasures().setSupport(satEvtNum / numOfEventsInLog);
		nuCon.getTraceBasedMeasures().setSupport(satTrcNum / numOfTracesInLog);
//		logger.trace(String.format("There are %2$f satisfying occurrences for %1$s. The confidence is thus %2$f / %3$d = %4$f",
//				constraint, satOccurs, pivotOccurrences, satOccurs / pivotOccurrences));
		nuCon.getEventBasedMeasures().setConfidence(satEvtNum / pivotOccurrences);
		nuCon.getTraceBasedMeasures().setConfidence(satTrcNum / tracesWithPivot);
//		logger.trace(String.format("The pivot participation fraction for %1$s is %3$f. The interest factor is thus %2$f * %3$f = %4$f",
//				constraint, constraint.getConfidence(), pivotParticipationFraction, constraint.getConfidence() * pivotParticipationFraction));
		nuCon.getEventBasedMeasures().setCoverage((double)pivotOccurrences / numOfEventsInLog);
		nuCon.getTraceBasedMeasures().setCoverage((double)tracesWithPivot / numOfTracesInLog);
		
		return nuCon;
	}

    @Override
    protected Set<Constraint> refineRelationConstraints(Set<Constraint> setOfConstraints) {
        return null;
    }

    private static RelationConstraint refineByComputingDistances(
			RelationConstraint relCon,
			LocalStatsWrapper implyingLocalStats, TaskChar implied) {
    	if (relCon instanceof RespondedExistence) {
    		RespondedExistence resEx = (RespondedExistence)relCon;
	    	SummaryStatistics distancesSumStats = new SummaryStatistics();
	    	NavigableMap<Integer, Integer> distancesMap = implyingLocalStats.interplayStatsTable.get(implied).distances;
	    	
	    	// FIXME Watch out, this is by chance so: one thing is saying that the implication verse is from the second parameter towards the first, another thing is to state that the temporal constraint is exerted on the occurrence onwards or backwards
	    	switch (resEx.getImplicationVerse()) {
			case FORWARD:
				distancesMap = distancesMap.tailMap(0, false).headMap(StatsCell.NEVER_ONWARDS, false);
				for (Integer distance : distancesMap.keySet()) {
					if (distance != StatsCell.NEVER_EVER) {
						for (int i = 0; i < distancesMap.get(distance); i++) {
							distancesSumStats.addValue(distance);
						}
					}
				}
				break;
			case BACKWARD:
				distancesMap = distancesMap.tailMap(StatsCell.NEVER_BACKWARDS, false).headMap(0, false);
				for (Integer distance : distancesMap.keySet()) {
					if (distance != StatsCell.NEVER_EVER) {
						for (int i = 0; i < distancesMap.get(distance); i++) {
							distancesSumStats.addValue(distance);
						}
					}
				}
				break;
			case BOTH:
				distancesMap = distancesMap.tailMap(StatsCell.NEVER_BACKWARDS, false).headMap(StatsCell.NEVER_ONWARDS, false);
				for (Integer distance : distancesMap.keySet()) {
					if (distance != StatsCell.NEVER_EVER) {
						for (int i = 0; i < distancesMap.get(distance); i++) {
							distancesSumStats.addValue(distance);
						}
					}
				}
			default:
				break;
			}

	    	if (distancesSumStats.getN() > 1) {
		    	resEx.expectedDistance = distancesSumStats.getMean();
		    	double tFactor = new TDistribution(distancesSumStats.getN()-1).cumulativeProbability(0.05);
		    	resEx.confidenceIntervalMargin = tFactor * distancesSumStats.getStandardDeviation();
	    	}
    	}
    	
		return relCon;
	}

    public boolean isForeseeingDistances() {
		return foreseeingDistances;
	}

}