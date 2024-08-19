package minerful.miner.core;

import java.util.Collection;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.constraint.ConstraintsBag;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.pruning.ConflictAndRedundancyResolver;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarkingPolicy;
import minerful.postprocessing.pruning.ThresholdsMarker;
import minerful.postprocessing.pruning.SubsumptionHierarchyMarker;

import org.apache.log4j.Logger;

public class MinerFulPruningCore {
	protected static Logger logger;
	protected ProcessSpecification processSpecification;
	protected Collection<TaskChar> tasksToQueryFor; 
	protected PostProcessingCmdParameters postProcParams;
	protected SubsumptionHierarchyMarker subMarker;
	protected ThresholdsMarker threshMarker;

	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
        }
	}
	
	public MinerFulPruningCore(ProcessSpecification processSpecification,
			PostProcessingCmdParameters postProcParams) {
		this(	processSpecification,
				processSpecification.getProcessAlphabet(),
				postProcParams);
	}
	
	public MinerFulPruningCore(ProcessSpecification processSpecification,
			Collection<TaskChar> tasksToQueryFor,
			PostProcessingCmdParameters postProcParams) {
		this.processSpecification = processSpecification;
		this.tasksToQueryFor = tasksToQueryFor;
		this.postProcParams = postProcParams;
		this.subMarker = new SubsumptionHierarchyMarker(processSpecification.bag);
		// fixed to be it parametric
		this.subMarker.setPolicy(this.postProcParams.hierarchyPolicy.translate());
		this.threshMarker = new ThresholdsMarker(processSpecification.bag);
	}

	public ConstraintsBag massageConstraints() {
		logger.info("Post-processing the discovered specification...");
		if (this.postProcParams.postProcessingAnalysisType.isPostProcessingRequested()) {
			this.markConstraintsBelowThresholds();
			if (this.postProcParams.postProcessingAnalysisType.isHierarchySubsumptionResolutionRequested()) {
				this.markRedundancyBySubsumptionHierarchy();
			}
			if (this.postProcParams.postProcessingAnalysisType.isRedundancyResolutionRequested()) {
				this.detectConflictsOrRedundancies();
			}
		}
		

		if (this.postProcParams.cropRedundantAndInconsistentConstraints) {
			this.processSpecification.bag.removeMarkedConstraints();
		}

		return this.processSpecification.bag;
	}

	private ConstraintsBag markConstraintsBelowThresholds() {
		logger.info("Pruning constraints below thresholds...");
		
		long beforeThresholdsPruning = System.currentTimeMillis();
		
		this.processSpecification.bag = this.threshMarker.markConstraintsBelowThresholds(
				this.postProcParams.evtSupportThreshold,
				this.postProcParams.evtConfidenceThreshold,
				this.postProcParams.evtCoverageThreshold,
				this.postProcParams.trcSupportThreshold,
				this.postProcParams.trcConfidenceThreshold,
				this.postProcParams.trcCoverageThreshold);

		long afterThresholdsPruning = System.currentTimeMillis();
    	
		this.threshMarker.printComputationStats(beforeThresholdsPruning, afterThresholdsPruning);
		
		if (this.postProcParams.cropRedundantAndInconsistentConstraints) {
			this.processSpecification.bag.removeMarkedConstraints();
		}

		// Let us try to free memory!
        System.gc();
        
        return this.processSpecification.bag;
	}

	private ConstraintsBag detectConflictsOrRedundancies() {

    	long beforeConflictResolution = System.currentTimeMillis();
    	
    	ConflictAndRedundancyResolver confliReso = new ConflictAndRedundancyResolver(processSpecification, postProcParams);
//    	this.processSpecification = confliReso.resolveConflictsOrRedundancies();
    	confliReso.resolveConflictsOrRedundancies();

    	long afterConflictResolution = System.currentTimeMillis();
        
        confliReso.printComputationStats(beforeConflictResolution, afterConflictResolution);

        if (this.postProcParams.cropRedundantAndInconsistentConstraints) {
			this.processSpecification.bag.removeMarkedConstraints();
		}
		
		// Let us try to free memory!
        System.gc();
		
        return this.processSpecification.bag;
	}

	public ConstraintsBag markRedundancyBySubsumptionHierarchy() {
		long
       	beforeSubCheck = 0L,
       	afterSubCheck = 0L;
		
		// if (!this.postProcParams.cropRedundantAndInconsistentConstraints) {
		// 	this.processSpecification.resetMarks();
		// }

        logger.info("Pruning redundancy, on the basis of hierarchy subsumption...");

        beforeSubCheck = System.currentTimeMillis();

        this.subMarker.markSubsumptionRedundantConstraints(this.tasksToQueryFor);

        afterSubCheck = System.currentTimeMillis();
		this.subMarker.printComputationStats(beforeSubCheck, afterSubCheck);
    	
		if (this.postProcParams.cropRedundantAndInconsistentConstraints) {
			this.processSpecification.bag.removeMarkedConstraints();
		}
    	
        // Let us try to free memory!
        System.gc();
	    
	    return this.processSpecification.bag;
	}

	public ProcessSpecification getProcessSpecification() {
		return this.processSpecification;
	}
}