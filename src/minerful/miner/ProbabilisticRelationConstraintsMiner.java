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
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintSubFamily;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateResponse;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainPrecedence;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.CouplingRelationConstraint;
import minerful.concept.constraint.relation.NegativeRelationConstraint;
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

    public ProbabilisticRelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive) {
        super(globalStats, taskCharArchive);
        this.foreseeingDistances = false;
    }

    public ProbabilisticRelationConstraintsMiner(GlobalStatsTable globalStats, TaskCharArchive taskCharArchive, boolean foreseeingDistances) {
    	super(globalStats, taskCharArchive);
    	this.foreseeingDistances = foreseeingDistances;
    }
    
    @Override
    public TaskCharRelatedConstraintsBag discoverConstraints(TaskCharRelatedConstraintsBag constraintsBag) {
        // Inizialization
        if (constraintsBag == null) {
            constraintsBag = new TaskCharRelatedConstraintsBag(taskCharArchive.getTaskChars());
        }
        LocalStatsWrapper auxLocalStats = null;
        Set<Constraint> auxRelCons = super.makeTemporarySet(
        		MetaConstraintUtils.howManyPossibleConstraints(this.taskCharArchive.howManyTaskChars()));
        for (TaskChar tChUnderAnalysis : this.taskCharArchive.getTaskChars()) {
            auxLocalStats = this.globalStats.statsTable.get(tChUnderAnalysis.identifier);
            // Avoid the famous rule: EX FALSO QUOD LIBET! Meaning: if you have no occurrence of a character, each constraint is potentially valid on it. Thus, it is perfectly useless to indagate over it -- and believe me, if you remove this check, it actually happens you have every possible restrictive constraint as valid in the list!
            if (auxLocalStats.getTotalAmountOfAppearances() > 0) {
                auxRelCons.addAll(
                 this.discoverRelationConstraints(tChUnderAnalysis));
            }
        }
        for (Constraint relCon : auxRelCons) {
            constraintsBag.add(relCon.base, relCon);
        }
        return constraintsBag;
    }

    // Very very rough: a little statistical analysis on the trend would be better
    @Override
    protected Set<? extends Constraint> discoverRelationConstraints(TaskChar pivotChr) {
        double	supportForRespondedExistence = 0.0,
                supportForReversedRespondedExistence = 0.0,
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
        TaskChar
                searchedChr = null;
        Character pivot = pivotChr.identifier;
        StatsCell   interplayStats = null,
                    reversedInterplayStats = null;
        Set<Constraint>
        	relaCons = super.makeTemporarySet(
        		MetaConstraintUtils.howManyPossibleRelationConstraints(this.taskCharArchive.howManyTaskChars())),
        	nuRelaCons = super.makeNavigableSet();
        	
        LocalStatsWrapper
                pivotLocalStats = globalStats.statsTable.get(pivot),
                searchedLocalStats = null;
        long   pivotAppearances = pivotLocalStats.getTotalAmountOfAppearances(),
                searchedAppearances = 0L;

        // For each other character
        for (Character searched : pivotLocalStats.localStatsTable.keySet()) {
        	nuRelaCons = super.makeNavigableSet();
            pivotChr = this.taskCharArchive.getTaskChar(pivot);
            pivotParticipationFraction = this.computeParticipationFraction(pivotChr, pivotLocalStats, globalStats.logSize);

            if (!searched.equals(pivot)) {
                searchedLocalStats = globalStats.statsTable.get(searched);
                interplayStats = pivotLocalStats.localStatsTable.get(searched);
                reversedInterplayStats = searchedLocalStats.localStatsTable.get(pivot);
                searchedChr = this.taskCharArchive.getTaskChar(searched);
                searchedAppearances = searchedLocalStats.getTotalAmountOfAppearances();
                searchedParticipationFraction = this.computeParticipationFraction(searchedChr, searchedLocalStats, globalStats.logSize);
                supportForRespondedExistence =
                        computeSupportForRespondedExistence(interplayStats, pivotAppearances);
                supportForReversedRespondedExistence =
                        computeSupportForRespondedExistence(reversedInterplayStats, searchedAppearances);
                supportForResponse =
                        computeSupportForResponse(interplayStats, pivotAppearances);
                supportForAlternateResponse =
                        computeSupportForAlternateResponse(interplayStats, pivotAppearances);
                supportForChainResponse =
                        computeSupportForChainResponse(interplayStats, pivotAppearances);
                supportForPrecedence =
                        computeSupportForPrecedence(reversedInterplayStats, searchedAppearances);
                supportForAlternatePrecedence =
                        computeSupportForAlternatePrecedence(reversedInterplayStats, searchedAppearances);
                supportForChainPrecedence =
                        computeSupportForChainPrecedence(reversedInterplayStats, searchedAppearances);
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
                RespondedExistence responExi =
                        new RespondedExistence(pivotChr, searchedChr, supportForRespondedExistence);
                RespondedExistence revResponExi =
                        new RespondedExistence(searchedChr, pivotChr, supportForReversedRespondedExistence);
                nuRelaCons.add(responExi);
                Response respo =
                        new Response(pivotChr, searchedChr, supportForResponse);
                nuRelaCons.add(respo);
                AlternateResponse altRespo =
                        new AlternateResponse(pivotChr, searchedChr, supportForAlternateResponse);
                nuRelaCons.add(altRespo);
                ChainResponse chainRespo =
                        new ChainResponse(pivotChr, searchedChr, supportForChainResponse);
                nuRelaCons.add(chainRespo);
                Precedence precedo =
                        new Precedence(pivotChr, searchedChr, supportForPrecedence);
                nuRelaCons.add(precedo);
                AlternatePrecedence altPrecedo =
                        new AlternatePrecedence(pivotChr, searchedChr, supportForAlternatePrecedence);
                nuRelaCons.add(altPrecedo);
                ChainPrecedence chainPrecedo =
                        new ChainPrecedence(pivotChr, searchedChr, supportForChainPrecedence);
                nuRelaCons.add(chainPrecedo);
                
                CouplingRelationConstraint coExi = new CoExistence(
                        responExi,
                        revResponExi,
                        supportForCoExistence);
                nuRelaCons.add(coExi);
                Succession successio = new Succession(
                        respo,
                        precedo,
                        supportForSuccession);
                nuRelaCons.add(successio);
                AlternateSuccession altSuccessio = new AlternateSuccession(
                        altRespo,
                        altPrecedo,
                        supportForAlternateSuccession);
                nuRelaCons.add(altSuccessio);
                ChainSuccession chainSuccessio = new ChainSuccession(
                        chainRespo,
                        chainPrecedo,
                        supportForChainSuccession);
                nuRelaCons.add(chainSuccessio);
                
                NotCoExistence notCoExi =
                        new NotCoExistence(pivotChr, searchedChr, supportForNotCoExistence);
                nuRelaCons.add(notCoExi);
                NotSuccession notSuccessio =
                        new NotSuccession(pivotChr, searchedChr, supportForNotSuccession);
                nuRelaCons.add(notSuccessio);
                NotChainSuccession notChainSuccessio =
                        new NotChainSuccession(pivotChr, searchedChr, supportForNotChainSuccession);
                nuRelaCons.add(notChainSuccessio);
                
                precedo.setConstraintWhichThisIsBasedUpon(revResponExi);
                altPrecedo.setConstraintWhichThisIsBasedUpon(precedo);
                chainPrecedo.setConstraintWhichThisIsBasedUpon(altPrecedo);
                respo.setConstraintWhichThisIsBasedUpon(responExi);
                altRespo.setConstraintWhichThisIsBasedUpon(respo);
                chainRespo.setConstraintWhichThisIsBasedUpon(altRespo);
                
                successio.setConstraintWhichThisIsBasedUpon(coExi);
                altSuccessio.setConstraintWhichThisIsBasedUpon(successio);
                chainSuccessio.setConstraintWhichThisIsBasedUpon(altSuccessio);
                
                notSuccessio.setConstraintWhichThisIsBasedUpon(notChainSuccessio);
                notCoExi.setConstraintWhichThisIsBasedUpon(notSuccessio);
                
                notCoExi.setOpposedTo(coExi);
                notSuccessio.setOpposedTo(successio);
                notChainSuccessio.setOpposedTo(chainSuccessio);
            }

            Iterator<Constraint> constraintsIterator = nuRelaCons.iterator();
            RelationConstraint currentConstraint = null;
            while (constraintsIterator.hasNext()) {
            	currentConstraint = (RelationConstraint) constraintsIterator.next();
            	refineByComputingRelevanceMetrics(currentConstraint, pivotParticipationFraction, searchedParticipationFraction);
            	if (this.isForeseeingDistances()) {
            		if (currentConstraint.getSubFamily() == ConstraintSubFamily.PRECEDENCE)
            			refineByComputingDistances(currentConstraint, searchedLocalStats, pivot);
            		else
            			refineByComputingDistances(currentConstraint, pivotLocalStats, searched);
            	}
            	
            	if (hasValuesAboveThresholds(currentConstraint)) this.computedConstraintsAboveThresholds++;
            }
            
            relaCons.addAll(nuRelaCons);
        }
        return relaCons;
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
            StatsCell reversedInterplayStats, long searchedAppearances) {
        if (reversedInterplayStats.distances.get(-1) != null) {
            double support = reversedInterplayStats.distances.get(-1);
            support /= searchedAppearances;
            return support;
        } else {
            return 0;
        }
    }

    private double computeSupportForAlternatePrecedence(
            StatsCell reversedInterplayStats, long searchedAppearances) {
        double antiSupport = reversedInterplayStats.betweenBackwards;
        antiSupport += reversedInterplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= searchedAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForPrecedence(
            StatsCell reversedInterplayStats, long searchedAppearances) {
        double antiSupport = reversedInterplayStats.howManyTimesItNeverAppearedBackwards();
        antiSupport /= searchedAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForChainResponse(
            StatsCell interplayStats, long pivotAppearances) {
        if (interplayStats.distances.get(1) != null) {
            double support = interplayStats.distances.get(1);
            support /= pivotAppearances;
            return support;
        } else {
            return 0;
        }
    }

    private double computeSupportForAlternateResponse(
            StatsCell interplayStats, long pivotAppearances) {
        double antiSupport = interplayStats.betweenOnwards;
        antiSupport += interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport /= pivotAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForResponse(
            StatsCell interplayStats, long pivotAppearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedOnwards();
        antiSupport /= pivotAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    private double computeSupportForRespondedExistence(
            StatsCell interplayStats, long pivotAppearances) {
        double antiSupport = interplayStats.howManyTimesItNeverAppearedAtAll();
        antiSupport /= pivotAppearances;
        return Constraint.complementSupport(antiSupport);
    }

    @Override
    protected Set<Constraint> refineRelationConstraints(Set<Constraint> setOfConstraints) {
        return null;
    }
    
    public static RelationConstraint refineByComputingConfidenceLevel(RelationConstraint relCon, double pivotParticipationFraction, double searchedParticipationFraction) {
    	if (relCon.getFamily() == ConstraintFamily.COUPLING || relCon.getFamily() == ConstraintFamily.NEGATIVE) {
    		relCon.confidence = relCon.support * (pivotParticipationFraction < searchedParticipationFraction ? pivotParticipationFraction : searchedParticipationFraction);
    	} else if (relCon.getSubFamily() == ConstraintSubFamily.PRECEDENCE) {
    		relCon.confidence = relCon.support * searchedParticipationFraction;
    	} else {
    		relCon.confidence = relCon.support * pivotParticipationFraction;
    	}
		return relCon;
    }
    
    public static RelationConstraint refineByComputingRelevanceMetrics(RelationConstraint relCon, double pivotParticipationFraction, double searchedParticipationFraction) {
    	relCon = refineByComputingConfidenceLevel(relCon, pivotParticipationFraction, searchedParticipationFraction);
    	if (relCon.getFamily() != ConstraintFamily.NEGATIVE || relCon instanceof NotChainSuccession || relCon instanceof NotSuccession) {
    		relCon.interestFactor =
    				relCon.support
    				*
    				pivotParticipationFraction
    				*
    				searchedParticipationFraction;
    	} else {
    		relCon.interestFactor =
    				relCon.support
    				*
    				( pivotParticipationFraction > searchedParticipationFraction
    					?	pivotParticipationFraction * (1.0 - searchedParticipationFraction)
    					:	searchedParticipationFraction * (1.0 - pivotParticipationFraction)
    				);
    	}
    	return relCon;
    }

    private static RelationConstraint refineByComputingDistances(
			RelationConstraint relCon,
			LocalStatsWrapper implyingLocalStats, Character implied) {
    	if (relCon instanceof RespondedExistence) {
    		RespondedExistence resEx = (RespondedExistence)relCon;
	    	SummaryStatistics distancesSumStats = new SummaryStatistics();
	    	NavigableMap<Integer, Integer> distancesMap = implyingLocalStats.localStatsTable.get(implied).distances;
	    	
	    	switch (resEx.getSubFamily()) {
			case RESPONSE:
				distancesMap = distancesMap.tailMap(0, false).headMap(StatsCell.NEVER_ONWARDS, false);
				for (Integer distance : distancesMap.keySet()) {
					if (distance != StatsCell.NEVER_EVER) {
						for (int i = 0; i < distancesMap.get(distance); i++) {
							distancesSumStats.addValue(distance);
						}
					}
				}
				break;
			case PRECEDENCE:
				distancesMap = distancesMap.tailMap(StatsCell.NEVER_BACKWARDS, false).headMap(0, false);
				for (Integer distance : distancesMap.keySet()) {
					if (distance != StatsCell.NEVER_EVER) {
						for (int i = 0; i < distancesMap.get(distance); i++) {
							distancesSumStats.addValue(distance);
						}
					}
				}
				break;
			case NONE:
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