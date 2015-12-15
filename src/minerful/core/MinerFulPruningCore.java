package minerful.core;

import java.util.Collection;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.ConstraintsBag;
import minerful.postprocessing.params.PostProcessingCmdParams;
import minerful.postprocessing.pruning.ConflictAndRedundancyResolver;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarkingPolicy;
import minerful.postprocessing.pruning.ThresholdsMarker;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarker;

import org.apache.log4j.Logger;

public class MinerFulPruningCore {
	protected static Logger logger;
	protected ProcessModel processModel;
	protected Collection<TaskChar> tasksToQueryFor; 
	protected PostProcessingCmdParams postProcParams;
	protected SubsumptionHierarchyMarker subMarker;
	protected ThresholdsMarker threshMarker;

	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
        }
	}
	
	public MinerFulPruningCore(ProcessModel processModel,
			PostProcessingCmdParams postProcParams) {
		this(processModel, processModel.getProcessAlphabet(), postProcParams);
	}
	
	public MinerFulPruningCore(ProcessModel processModel,
			Collection<TaskChar> tasksToQueryFor,
			PostProcessingCmdParams postProcParams) {
		this.processModel = processModel;
		this.tasksToQueryFor = tasksToQueryFor;
		this.postProcParams = postProcParams;
		this.subMarker = new SubsumptionHierarchyMarker(processModel.bag);
		// FIXME Make it parametric
		this.subMarker.setPolicy(SubsumptionHierarchyMarkingPolicy.EAGER_ON_SUPPORT_OVER_HIERARCHY);
		this.threshMarker = new ThresholdsMarker(processModel.bag);
	}

	public ConstraintsBag massageConstraints() {
		logger.info("Post-processing the discovered model...");
		
		pruneConstraintsBelowThresholds();
		
		if (this.postProcParams.analysisType.isPostProcessingRequested()) {
			if (this.postProcParams.analysisType.isRedundancyCheckRequested()) {
				this.detectConflictsOrRedundancies();
			} else {
				this.pruneRedundancyBySubsumptionHierarchy();
			}
		}
		return this.processModel.bag;
	}

	private ConstraintsBag pruneConstraintsBelowThresholds() {
		logger.info("Pruning constraints below thresholds...");
		
		long beforeThresholdsPruning = System.currentTimeMillis();
		
		this.processModel.bag = this.threshMarker.markConstraintsBelowThresholds(
				this.postProcParams.supportThreshold,
				this.postProcParams.confidenceThreshold,
				this.postProcParams.interestFactorThreshold);

		long afterThresholdsPruning = System.currentTimeMillis();
    	
		this.threshMarker.printComputationStats(beforeThresholdsPruning, afterThresholdsPruning);
		
		this.processModel.bag.removeMarkedConstraints();

		// Let us try to free memory!
        System.gc();
        
        return this.processModel.bag;
	}

	private ConstraintsBag detectConflictsOrRedundancies() {

    	long beforeConflictResolution = System.currentTimeMillis();
    	
    	ConflictAndRedundancyResolver confliReso = new ConflictAndRedundancyResolver(processModel, postProcParams);
    	this.processModel = confliReso.resolveConflictsOrRedundancies();

    	long afterConflictResolution = System.currentTimeMillis();
        
        confliReso.printComputationStats(beforeConflictResolution, afterConflictResolution);

		this.processModel.bag.removeMarkedConstraints();
		
		// Let us try to free memory!
        System.gc();
		
        return this.processModel.bag;
	}

	public ConstraintsBag pruneRedundancyBySubsumptionHierarchy() {
		long
       	beforeSubCheck = 0L,
       	afterSubCheck = 0L;

        logger.info("Pruning redundancy, on the basis of hierarchy subsumption...");

        beforeSubCheck = System.currentTimeMillis();

        this.subMarker.markSubsumptionRedundantConstraints(this.tasksToQueryFor);

        afterSubCheck = System.currentTimeMillis();
		this.subMarker.printComputationStats(beforeSubCheck, afterSubCheck);
    	
		this.processModel.bag.removeMarkedConstraints();
    	
        // Let us try to free memory!
        System.gc();
	    
	    return this.processModel.bag;
	}

	public ProcessModel getProcessModel() {
		return this.processModel;
	}
}