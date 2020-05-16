/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.miner;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Set;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainSuccession;
import minerful.concept.constraint.relation.NotCoExistence;
import minerful.concept.constraint.relation.NotSuccession;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.concept.constraint.relation.RespondedExistence;
import minerful.concept.constraint.relation.Response;
import minerful.concept.constraint.relation.Succession;
import minerful.miner.stats.GlobalStatsTable;
import minerful.miner.stats.LocalStatsWrapper;
import minerful.miner.stats.StatsCell;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

public class ProbabilisticRelationConstraintsMiner extends RelationConstraintsMiner {
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
        // Inizialisation
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
        double	supportForRespondedExistence = 0.0,
                supportForResponse = 0.0,
                supportForAlternateResponse = 0.0,
                supportForChainResponse = 0.0,
                supportForPrecedence = 0.0,
                supportForAlternatePrecedence = 0.0,
                supportForChainPrecedence = 0.0,
                supportForCoExistence = 0.0,
                supportForSuccession = 0.0,
                supportForAlternateSuccession = 0.0,
                supportForChainSuccession = 0.0,
                supportForNotCoExistence = 0.0,
                supportForNotSuccession = 0.0,
                supportForNotChainSuccession = 0.0,

		        pivotParticipationFraction = 0.0,
		        searchedParticipationFraction = 0.0;
        StatsCell   interplayStats = null,
                    reversedInterplayStats = null;
        Set<Constraint>
        	relaCons = //super.makeTemporarySet(
//        		MetaConstraintUtils.howManyPossibleRelationConstraints(this.tasksToQueryFor.size(), this.taskCharArchive.size())),
        		super.makeNavigableSet(),
        	nuRelaCons = super.makeNavigableSet();
        	
        LocalStatsWrapper
                pivotLocalStats = globalStats.statsTable.get(pivotTask),
                searchedLocalStats = null;
        long   pivotAppearances = pivotLocalStats.getTotalAmountOfOccurrences(),
                searchedAppearances = 0L;

        // For each other character
        for (TaskChar searchedTask : pivotLocalStats.interplayStatsTable.keySet()) {
        	nuRelaCons = super.makeNavigableSet();
            pivotParticipationFraction = this.computeParticipationFraction(pivotTask, pivotLocalStats, globalStats.logSize);

            if (!searchedTask.equals(pivotTask)) {
                searchedLocalStats = globalStats.statsTable.get(searchedTask);
                interplayStats = pivotLocalStats.interplayStatsTable.get(searchedTask);
                reversedInterplayStats = searchedLocalStats.interplayStatsTable.get(pivotTask);
                searchedAppearances = searchedLocalStats.getTotalAmountOfOccurrences();
                searchedParticipationFraction = this.computeParticipationFraction(searchedTask, searchedLocalStats, globalStats.logSize);
                supportForRespondedExistence =
                        computeSupportForRespondedExistence(interplayStats, pivotAppearances);
                supportForResponse =
                        computeSupportForResponse(interplayStats, pivotAppearances);
                supportForAlternateResponse =
                        computeSupportForAlternateResponse(interplayStats, pivotAppearances);
                supportForChainResponse =
                        computeSupportForChainResponse(interplayStats, pivotAppearances);
                supportForPrecedence =
                        computeSupportForPrecedence(interplayStats, pivotAppearances);
                supportForAlternatePrecedence =
                        computeSupportForAlternatePrecedence(interplayStats, pivotAppearances);
                supportForChainPrecedence =
                        computeSupportForChainPrecedence(interplayStats, pivotAppearances);
                supportForCoExistence =
                        computeSupportForCoExistence(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForSuccession =
                        computeSupportForSuccession(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForAlternateSuccession =
                        computeSupportForAlternateSuccession(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForChainSuccession =
                        computeSupportForChainSuccession(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForNotCoExistence =
                        computeSupportForNotCoExistence(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForNotSuccession =
                        computeSupportForNotSuccession(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);
                supportForNotChainSuccession =
                        computeSupportForNotChainSuccession(interplayStats, reversedInterplayStats, pivotAppearances + searchedAppearances);

                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new RespondedExistence(pivotTask, searchedTask), supportForRespondedExistence, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new Response(pivotTask, searchedTask), supportForResponse, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new AlternateResponse(pivotTask, searchedTask), supportForAlternateResponse, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new ChainResponse(pivotTask, searchedTask), supportForChainResponse, pivotParticipationFraction, searchedParticipationFraction));

                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new Precedence(searchedTask, pivotTask), supportForPrecedence, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new AlternatePrecedence(searchedTask, pivotTask), supportForAlternatePrecedence, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new ChainPrecedence(searchedTask, pivotTask), supportForChainPrecedence, pivotParticipationFraction, searchedParticipationFraction));

                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new CoExistence(pivotTask, searchedTask), supportForCoExistence, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new Succession(pivotTask, searchedTask), supportForSuccession, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new AlternateSuccession(pivotTask, searchedTask), supportForAlternateSuccession, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new ChainSuccession(pivotTask, searchedTask), supportForChainSuccession, pivotParticipationFraction, searchedParticipationFraction));

                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new NotCoExistence(pivotTask, searchedTask), supportForNotCoExistence, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new NotSuccession(pivotTask, searchedTask), supportForNotSuccession, pivotParticipationFraction, searchedParticipationFraction));
                nuRelaCons.add(this.updateConstraint(constraintsBag, pivotTask, new NotChainSuccession(pivotTask, searchedTask), supportForNotChainSuccession, pivotParticipationFraction, searchedParticipationFraction));

//                precedo.setConstraintWhichThisIsBasedUpon(responExi);
//                altPrecedo.setConstraintWhichThisIsBasedUpon(precedo);
//                chainPrecedo.setConstraintWhichThisIsBasedUpon(altPrecedo);
//                respo.setConstraintWhichThisIsBasedUpon(responExi);
//                altRespo.setConstraintWhichThisIsBasedUpon(respo);
//                chainRespo.setConstraintWhichThisIsBasedUpon(altRespo);
//                
//                successio.setConstraintWhichThisIsBasedUpon(coExi);
//                altSuccessio.setConstraintWhichThisIsBasedUpon(successio);
//                chainSuccessio.setConstraintWhichThisIsBasedUpon(altSuccessio);
//                
//                notSuccessio.setConstraintWhichThisIsBasedUpon(notChainSuccessio);
//                notCoExi.setConstraintWhichThisIsBasedUpon(notSuccessio);
//                
//                notCoExi.setOpposedTo(coExi);
//                notSuccessio.setOpposedTo(successio);
//                notChainSuccessio.setOpposedTo(chainSuccessio);
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
            
            relaCons.addAll(nuRelaCons);
        }
        return relaCons;
    }

	protected Constraint updateConstraint(ConstraintsBag constraintsBag,
			TaskChar indexingParam, Constraint searchedCon,
			double support, double pivotParticipationFraction, double searchedParticipationFraction) {
		Constraint con = constraintsBag.getOrAdd(indexingParam, searchedCon);
		con.setSupport(support);
		con.setEvaluatedOnLog(true);
		refineByComputingRelevanceMetrics(con, pivotParticipationFraction, searchedParticipationFraction);
		return con;
	}

	private double computeSupportForNotChainSuccession(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        return Constraint.complementSupport(
                this.computeSupportForChainSuccession(
                interplayStats, reversedInterplayStats, sumOfAppearances));
    }

    private double computeSupportForNotSuccession(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        return Constraint.complementSupport(
                this.computeSupportForSuccession(
                interplayStats, reversedInterplayStats, sumOfAppearances));
    }

    private double computeSupportForNotCoExistence(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        return Constraint.complementSupport(
                this.computeSupportForCoExistence(
                interplayStats, reversedInterplayStats, sumOfAppearances));
    }

    private double computeSupportForChainSuccession(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        if (interplayStats.distances.get(1) != null && reversedInterplayStats.distances.get(-1) != null) {
            double support = interplayStats.distances.get(1);
            support += reversedInterplayStats.distances.get(-1);
            support /= sumOfAppearances;
            return support;
        } else {
            return 0;
        }
    }

    private double computeSupportForAlternateSuccession(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        double antiSupport = interplayStats.betweenOnwards;
        antiSupport += interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport += reversedInterplayStats.betweenBackwards;
        antiSupport += reversedInterplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= sumOfAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForSuccession(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport += reversedInterplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= sumOfAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForCoExistence(
            StatsCell interplayStats, StatsCell reversedInterplayStats, long sumOfAppearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedAtAll();
        antiSupport += reversedInterplayStats.howManyTimesItNeverAppearedAtAll();
        antiSupport /= sumOfAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForChainPrecedence(
            StatsCell interplayStats, long appearances) {
        if (interplayStats.distances.get(-1) != null) {
            double support = interplayStats.distances.get(-1);
            support /= appearances;
            return support;
        } else {
            return 0;
        }
    }

    private double computeSupportForAlternatePrecedence(
            StatsCell interplayStats, long appearances) {
        double antiSupport = interplayStats.betweenBackwards;
        antiSupport += interplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= appearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForPrecedence(
            StatsCell interplayStats, long appearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= appearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForChainResponse(
            StatsCell interplayStats, long appearances) {
        if (interplayStats.distances.get(1) != null) {
            double support = interplayStats.distances.get(1);
            support /= appearances;
            return support;
        } else {
            return 0;
        }
    }

    private double computeSupportForAlternateResponse(
            StatsCell interplayStats, long appearances) {
        double antiSupport = interplayStats.betweenOnwards;
        antiSupport += interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport /= appearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForResponse(
            StatsCell interplayStats, long appearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport /= appearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForRespondedExistence(
            StatsCell interplayStats, long appearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedAtAll();
        antiSupport /= appearances;
        return Constraint.complementSupport(antiSupport);
    }

    @Override
    protected Set<Constraint> refineRelationConstraints(Set<Constraint> setOfConstraints) {
        return null;
    }
    
    public static RelationConstraint refineByComputingConfidenceLevel(RelationConstraint relCon, double pivotParticipationFraction, double searchedParticipationFraction) {
    	if (relCon.getSubFamily() == RelationConstraintSubFamily.COUPLING || relCon.getSubFamily() == RelationConstraintSubFamily.NEGATIVE) {
    		relCon.setConfidence(relCon.getSupport() * (pivotParticipationFraction < searchedParticipationFraction ? pivotParticipationFraction : searchedParticipationFraction));
    	} else if (relCon.getImplicationVerse() == ConstraintImplicationVerse.BACKWARD) {
    		relCon.setConfidence(relCon.getSupport() * searchedParticipationFraction);
    	} else {
    		relCon.setConfidence(relCon.getSupport() * pivotParticipationFraction);
    	}
		return relCon;
    }
    
    public static RelationConstraint refineByComputingRelevanceMetrics(Constraint con, double pivotParticipationFraction, double searchedParticipationFraction) {
    	RelationConstraint relCon = (RelationConstraint) con;
    	relCon = refineByComputingConfidenceLevel(relCon, pivotParticipationFraction, searchedParticipationFraction);
    	if (relCon.getSubFamily() != RelationConstraintSubFamily.NEGATIVE || relCon instanceof NotChainSuccession || relCon instanceof NotSuccession) {
    		relCon.setInterestFactor(
    				relCon.getSupport()
    				*
    				pivotParticipationFraction
    				*
    				searchedParticipationFraction
    		);
    	} else {
    		relCon.setInterestFactor(
    				relCon.getSupport()
    				*
    				( pivotParticipationFraction > searchedParticipationFraction
    					?	pivotParticipationFraction * (1.0 - searchedParticipationFraction)
    					:	searchedParticipationFraction * (1.0 - pivotParticipationFraction)
    				)
    		);
    	}
    	return relCon;
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