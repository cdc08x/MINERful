package minerful;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessModel;
import minerful.core.MinerFulPruningCore;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulSimplificationLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulSimplificationLauncher.class);
			
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

		MessagePrinter.configureLogging(systemParams.debugLevel);
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
	    	switch (inputParams.inputLanguage) {
	    	case MINERFUL:
	    		inputProcess = new ProcessModelEncoderDecoder().unmarshalProcessModel(inputParams.inputFile);
	    		break;
	    	case JSON:
	    		inputProcess = new ProcessModelEncoderDecoder().readFromJsonFile(inputParams.inputFile);
	    		break;
	    	case DECLARE_MAP:
	    		inputProcess = new DeclareMapEncoderDecoder(inputParams.inputFile.getAbsolutePath()).createMinerFulProcessModel();
	    		break;
    		default:
    			break;
	    	}
//	        inputProcess =
//	        		(	inputParams.inputLanguage.equals(InputModelParameters.InputEncoding.MINERFUL)
//	        			?	new ProcessModelEncoderDecoder().unmarshalProcessModel(inputParams.inputFile)
//	        			:	new DeclareMapEncoderDecoder(inputParams.inputFile.getAbsolutePath()).createMinerFulProcessModel());
	    } catch (Exception e) {
	    	MessagePrinter.getInstance(this).error("Unreadable process model from file: " + inputParams.inputFile.getAbsolutePath() + ". Check the file path or the specified encoding.", e);
	    	System.exit(1);
	    }
		return inputProcess;
	}

	private ProcessModel extractProcessModel(AssignmentModel declareMapModel) {
		return new DeclareMapEncoderDecoder(declareMapModel).createMinerFulProcessModel();
	}
}