package minerful.miner.core;

import java.util.Collection;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.ConstraintsBag;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.simplification.ConflictAndRedundancyResolver;

import org.apache.log4j.Logger;

public class MinerFulPruningCore {
	protected static Logger logger;
	protected ProcessModel processModel;
	protected Collection<TaskChar> tasksToQueryFor; 
	protected MinerFulCmdParameters minerFulParams;
	protected ViewCmdParameters viewParams;

	{
        if (logger == null) {
    		logger = Logger.getLogger(MinerFulQueryingCore.class.getCanonicalName());
        }
	}
	
	public MinerFulPruningCore(ProcessModel processModel,
			Collection<TaskChar> tasksToQueryFor,
			MinerFulCmdParameters minerFulParams, ViewCmdParameters viewParams) {
		this.processModel = processModel;
		this.tasksToQueryFor = tasksToQueryFor;
		this.minerFulParams = minerFulParams;
		this.viewParams = viewParams;
	}

	public ConstraintsBag massageConstraints() {
		if (this.minerFulParams.avoidRedundancy) {
			this.pruneRedundancyBySubsumptionHierarchy();
		}
		this.processModel.bag = this.processModel.bag.markConstraintsBelowThresholds(
				this.viewParams.supportThreshold,
				this.viewParams.confidenceThreshold,
				this.viewParams.interestThreshold);
		if (minerFulParams.avoidConflicts || minerFulParams.deepAvoidRedundancy) {
			this.detectConflictsOrRedundancies();
		}
		this.processModel.bag.removeMarkedConstraints();
		return this.processModel.bag;
	}

	private ConstraintsBag detectConflictsOrRedundancies() {

    	long beforeConflictResolution = System.currentTimeMillis();
    	
    	ConflictAndRedundancyResolver confliReso = new ConflictAndRedundancyResolver(processModel, minerFulParams.deepAvoidRedundancy);
    	confliReso.resolveConflictsOrRedundancies();
    	
    	long afterConflictResolution = System.currentTimeMillis();
        
        confliReso.printComputationStats(beforeConflictResolution, afterConflictResolution);
		
        return this.processModel.bag;
	}

	public ConstraintsBag pruneRedundancyBySubsumptionHierarchy() {
		long
       	before = 0L,
       	after = 0L,
        pruniTime = 0L;

		if (minerFulParams.avoidRedundancy) {
	        logger.info("Pruning redundancy, on the basis of hierarchy subsumption");
	
	        before = System.currentTimeMillis();
	
	        this.processModel.bag.markSubsumptionRedundantConstraints(this.tasksToQueryFor);
	    	
	    	after = System.currentTimeMillis();
	    	pruniTime = after - before;
	    	
	        // Let us try to free memory!
	        System.gc();
	    }
	
	    System.gc();
	    
	    return this.processModel.bag;
	}
}