package minerful.postprocessing.pruning;

import java.util.Collection;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.MutualRelationConstraint;
import minerful.concept.constraint.relation.NegativeRelationConstraint;
import minerful.utils.MessagePrinter;

public class SubsumptionHierarchyMarker {
	private static final String HIERARCHY_CODE = "'SH-check'";
	private static MessagePrinter logger = MessagePrinter.getInstance(SubsumptionHierarchyMarker.class.getCanonicalName());

	private int numberOfMarkedConstraints = 0;
	private boolean checking = false;
	
	private ConstraintsBag constraintsBag = null;
	private SubsumptionHierarchyMarkingPolicy policy = null;

	public SubsumptionHierarchyMarker() {
		this.policy = SubsumptionHierarchyMarkingPolicy.EAGER_ON_SUPPORT_OVER_HIERARCHY;
	}

	public SubsumptionHierarchyMarker(ConstraintsBag constraintsBag) {
		this();
		this.setConstraintsBag(constraintsBag);
	}

	public void setConstraintsBag(ConstraintsBag constraintsBag) {
		this.constraintsBag = constraintsBag;
	}

	public ConstraintsBag getConstraintsBag() {
		return this.constraintsBag;
	}

	public int getNumberOfMarkedConstraints() {
		return numberOfMarkedConstraints;
	}

	public boolean isChecking() {
		return checking;
	}

	public ConstraintsBag markSubsumptionRedundantConstraints() {
		return this.markSubsumptionRedundantConstraints(constraintsBag.getTaskChars());
	}

	public ConstraintsBag markSubsumptionRedundantConstraints(Collection<TaskChar> targetTaskChars) {
		if (this.constraintsBag == null)
			throw new IllegalStateException("Constraints bag not initialized");
		
		this.numberOfMarkedConstraints = 0;
		this.checking = true;
		
        // exploit the ordering
        MutualRelationConstraint coExiCon = null;
        NegativeRelationConstraint noRelCon = null;

        for (TaskChar key : targetTaskChars) {
            for (Constraint currCon : constraintsBag.getConstraintsOf(key)) {
            	if (!currCon.isRedundant()) {
            		// If the policy is to be eager wrt the hierarchy subsumptions, no matter the support, this is the way to go
            		if (this.policy.equals(SubsumptionHierarchyMarkingPolicy.EAGER_ON_HIERARCHY_OVER_SUPPORT)) {
            			markGenealogyAsRedundant(currCon.getConstraintWhichThisIsBasedUpon(), currCon, key, constraintsBag);
            		} else {
            			// Otherwise, eliminate those constraints that are in the hierarchy behind the current one, if...
		                if (currCon.hasConstraintToBaseUpon()) {
		                	// ... if the current one has the same support of all others
		                    if (currCon.isMoreInformativeThanGeneric()) {
		                    	logger.trace(
		                    			"Removing the genealogy of {1}, starting with {0}, because {1} is subsumed by {0} and more informative than the whole genalogy", 
		                    			currCon.getConstraintWhichThisIsBasedUpon(),
		                    			currCon
		                    	);
		                        markGenealogyAsRedundant(currCon.getConstraintWhichThisIsBasedUpon(), currCon, key, constraintsBag);
		                    } else {
		                    	// If we want to be "conservative" (namely, a higher support justifies the removal of more strict constraints, this is the way to go
		                    	if (this.policy.equals(SubsumptionHierarchyMarkingPolicy.EAGER_ON_SUPPORT_OVER_HIERARCHY)) {
		                    		logger.trace(
			                    			"Removing {0} because {1} has a higher support and {0} is subsumed by it",
			                    			currCon,
			                    			currCon.getConstraintWhichThisIsBasedUpon());
//		                        	constraintsBag.remove(key, currCon);
		                    		this.markAsRedundant(currCon);
		                    	}
		                    }
		                }
            		}
	                if (currCon.getSubFamily() == RelationConstraintSubFamily.COUPLING) {
	                	if (this.policy.equals(SubsumptionHierarchyMarkingPolicy.EAGER_ON_HIERARCHY_OVER_SUPPORT)) {
	                		this.markAsRedundant(coExiCon.getForwardConstraint());
	                		this.markAsRedundant(coExiCon.getBackwardConstraint());
	            		} else {
		                    coExiCon = (MutualRelationConstraint) currCon;
		                    if (coExiCon.hasImplyingConstraints()) {
		                        if (coExiCon.isAsInformativeAsTheImplyingConstraints()) {
		                        	logger.trace("Removing {0}" +
		                        			", which is the forward, and {1}" +
		                        			", which is the backward, because {2}" +
		                        			" is the Mutual Relation referring to them and more informative",
		                        			coExiCon.getForwardConstraint(),
		                        			coExiCon.getBackwardConstraint(),
		                        			coExiCon);
	                        	// constraintsBag.remove(key, coExiCon.getForwardConstraint());
		                        	this.markAsRedundant(coExiCon.getForwardConstraint());
	                        	// constraintsBag.remove(key, coExiCon.getBackwardConstraint());
		                        	this.markAsRedundant(coExiCon.getBackwardConstraint());
//	                        } else if (coExiCon.isMoreReliableThanAnyOfImplyingConstraints()){
//	                        	// Remove the weaker, if any
//	                        	if (coExiCon.isMoreReliableThanForwardConstraint()) {
//	                        		nuBag.remove(key, coExiCon.getForwardConstraint());
//	                        	} else {
//	                        		nuBag.remove(key, coExiCon.getBackwardConstraint());
//	                        	}
		                        } else {
		                        	if (this.policy.equals(SubsumptionHierarchyMarkingPolicy.EAGER_ON_SUPPORT_OVER_HIERARCHY)) {
//	                        	constraintsBag.remove(key, coExiCon);
		                        		this.markAsRedundant(coExiCon);
		                        	}
		                        }
		                    }
	                    }
	                }
	                if (currCon.getSubFamily() == RelationConstraintSubFamily.NEGATIVE) {
	                    noRelCon = (NegativeRelationConstraint) currCon;
	                    if (noRelCon.hasOpponent()) {
	                        if (noRelCon.isMoreReliableThanTheOpponent()) {
	                        	logger.trace("Removing {0}" +
	                        			" because {1} is the opponent of {0}" +
	                        			" but less supported",
	                        			noRelCon.getOpponent(),
	                        			noRelCon);
//	                            constraintsBag.remove(key, noRelCon.getOpponent());
	                        	this.markAsRedundant(noRelCon.getOpponent());
	                        } else {
	                        	logger.trace("Removing {0}" +
	                        			" because {0} is the opponent of {1}" +
	                        			" but less supported",
	                        			noRelCon,
	                        			noRelCon.getOpponent());
//	                            constraintsBag.remove(key, noRelCon);
	                        	this.markAsRedundant(noRelCon);
	                        }
	                    }
	                }
            	}
            }
        }
        this.checking = false;

        return constraintsBag;
    }
	
	private ConstraintsBag markGenealogyAsRedundant(
            Constraint lastSon,
            Constraint lastSurvivor,
            TaskChar key,
            ConstraintsBag genealogyTree) {
        Constraint genealogyDestroyer = lastSon;
//      ConstraintImplicationVerse destructionGeneratorsFamily = lastSurvivor.getSubFamily();
        while (genealogyDestroyer != null) {
    		key = genealogyDestroyer.getBase().getFirstTaskChar();
    		this.markAsRedundant(genealogyDestroyer);
            genealogyDestroyer = genealogyDestroyer.getConstraintWhichThisIsBasedUpon();
        }

        return genealogyTree;
    }

	private void markAsRedundant(Constraint constraint) {
    	if (!constraint.isRedundant()) {
			constraint.setRedundant(true);
			this.numberOfMarkedConstraints++;
    	}
	}


	public SubsumptionHierarchyMarkingPolicy getPolicy() {
		return policy;
	}

	public void setPolicy(SubsumptionHierarchyMarkingPolicy policy) {
		this.policy = policy;
	}

	public void printComputationStats(long before, long after) {
		if (this.isChecking()) {
			throw new IllegalStateException("Subsumption-hierarchy-based check in progress");
		}
		
        StringBuffer
    	csvSummaryBuffer = new StringBuffer(),
    	csvSummaryLegendBuffer = new StringBuffer(),
    	csvSummaryComprehensiveBuffer = new StringBuffer();

        csvSummaryBuffer.append(SubsumptionHierarchyMarker.HIERARCHY_CODE);
        csvSummaryLegendBuffer.append("'Operation code'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.constraintsBag.howManyConstraints());
        csvSummaryLegendBuffer.append("'Input constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(this.getNumberOfMarkedConstraints());
        csvSummaryLegendBuffer.append("'Marked constraints'");
        csvSummaryBuffer.append(";");
        csvSummaryLegendBuffer.append(";");
     // --------------------------------
        csvSummaryBuffer.append(after - before);
        csvSummaryLegendBuffer.append("'Time'");
//        csvSummaryBuffer.append(";");
//        csvSummaryLegendBuffer.append(";");

        csvSummaryComprehensiveBuffer.append("\n\nSubsumption-hierarchy-based pruning: \n");
        csvSummaryComprehensiveBuffer.append(csvSummaryLegendBuffer.toString());
        csvSummaryComprehensiveBuffer.append("\n");
        csvSummaryComprehensiveBuffer.append(csvSummaryBuffer.toString());

        logger.info(csvSummaryComprehensiveBuffer.toString());
	}
}