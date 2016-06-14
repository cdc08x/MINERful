package minerful;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessModel;
import minerful.core.MinerFulPruningCore;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;

public class MinerFulSimplificationLauncher {
	private ProcessModel inputProcess;
	private PostProcessingCmdParameters postParams;
	
	public MinerFulSimplificationLauncher(AssignmentModel declareMapModel, PostProcessingCmdParameters postParams) {
		this.postParams = postParams;

		this.inputProcess = extractProcessModel(declareMapModel);
	}

	public MinerFulSimplificationLauncher(ProcessModel minerFulProcessModel, PostProcessingCmdParameters postParams) {
		this.postParams = postParams;

		this.inputProcess = minerFulProcessModel;
	}

	public MinerFulSimplificationLauncher(InputModelParameters inputParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams) {
		this.postParams = postParams;

		this.inputProcess = extractProcessModel(inputParams, systemParams);
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}

		MinerFulMinerStarter.configureLogging(systemParams.debugLevel);
	}
	
	public ProcessModel simplify() {
	    MinerFulPruningCore miFuPruNi = new MinerFulPruningCore(inputProcess, postParams);
	    miFuPruNi.massageConstraints();
	    
	    ProcessModel outputProcess = miFuPruNi.getProcessModel();

	    return outputProcess;
	}

	private ProcessModel extractProcessModel(InputModelParameters inputParams, SystemCmdParameters systemParams) {
		ProcessModel inputProcess = null;
			
	    try {
	        inputProcess =
	        		(	inputParams.inputLanguage.equals(InputModelParameters.InputEncoding.MINERFUL)
	        			?	new ProcessModelEncoderDecoder().unmarshalProcessModel(inputParams.inputFile)
	        			:	DeclareMapEncoderDecoder.fromDeclareMapToMinerfulProcessModel(inputParams.inputFile.getAbsolutePath()));
	    } catch (Exception e) {
	    	System.err.println("Unreadable process model from file: " + inputParams.inputFile.getAbsolutePath() + ". Check the file path or the specified encoding.");
	    	e.printStackTrace(System.err);
	    	System.exit(1);
	    }
		return inputProcess;
	}

	private ProcessModel extractProcessModel(AssignmentModel declareMapModel) {
		return DeclareMapEncoderDecoder.fromDeclareMapToMinerfulProcessModel(declareMapModel);
	}
}