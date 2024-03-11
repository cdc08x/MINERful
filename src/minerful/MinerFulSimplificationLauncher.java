package minerful;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessSpecification;
import minerful.io.ProcessSpecificationLoader;
import minerful.io.encdec.ProcessSpecificationEncoderDecoder;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.io.params.InputSpecificationParameters;
import minerful.miner.core.MinerFulPruningCore;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulSimplificationLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulSimplificationLauncher.class);
			
	private ProcessSpecification inputProcess;
	private PostProcessingCmdParameters postParams;
	
	private MinerFulSimplificationLauncher(PostProcessingCmdParameters postParams) {
		this.postParams = postParams;
	}
	
	public MinerFulSimplificationLauncher(AssignmentModel declareMapModel, PostProcessingCmdParameters postParams) {
		this(postParams);

		this.inputProcess = new ProcessSpecificationLoader().loadProcessSpecification(declareMapModel);
	}

	public MinerFulSimplificationLauncher(ProcessSpecification minerFulProcessSpecification, PostProcessingCmdParameters postParams) {
		this(postParams);

		this.inputProcess = minerFulProcessSpecification;
	}

	public MinerFulSimplificationLauncher(InputSpecificationParameters inputParams, 
			PostProcessingCmdParameters postParams, SystemCmdParameters systemParams) {
		this(postParams);

		this.inputProcess = new ProcessSpecificationLoader().loadProcessSpecification(inputParams.inputLanguage, inputParams.inputFile);
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process specification file missing!");
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);
	}
	
	public ProcessSpecification simplify() {
	    MinerFulPruningCore miFuPruNi = new MinerFulPruningCore(inputProcess, postParams);
	    miFuPruNi.massageConstraints();

	    ProcessSpecification outputProcess = miFuPruNi.getProcessSpecification();

	    return outputProcess;
	}


}