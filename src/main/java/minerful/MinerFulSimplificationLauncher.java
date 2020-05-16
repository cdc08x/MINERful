package minerful;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessModel;
import minerful.io.ProcessModelLoader;
import minerful.io.encdec.ProcessModelEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputModelParameters;
import minerful.miner.core.MinerFulPruningCore;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulSimplificationLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulSimplificationLauncher.class);
			
	private ProcessModel inputProcess;
	private PostProcessingCmdParameters postParams;
	
	private MinerFulSimplificationLauncher(PostProcessingCmdParameters postParams) {
		this.postParams = postParams;
	}
	
	public MinerFulSimplificationLauncher(AssignmentModel declareMapModel, PostProcessingCmdParameters postParams) {
		this(postParams);

		this.inputProcess = new ProcessModelLoader().loadProcessModel(declareMapModel);
	}

	public MinerFulSimplificationLauncher(ProcessModel minerFulProcessModel, PostProcessingCmdParameters postParams) {
		this(postParams);

		this.inputProcess = minerFulProcessModel;
	}

	public MinerFulSimplificationLauncher(InputModelParameters inputParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams) {
		this(postParams);

		this.inputProcess = new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile);
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


}