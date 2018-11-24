package minerful;

import java.io.IOException;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessModel;
import minerful.io.ProcessModelLoader;
import minerful.io.params.InputModelParameters;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.params.SystemCmdParameters;
import minerful.utils.MessagePrinter;

/**
 * Launches the generation of event logs from declarative process specifications.
 */
public class MinerFulLogMakerLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulLogMakerLauncher.class);
			
	private ProcessModel inputProcess;
	private LogMakerParameters logMakParams;
	
	private MinerFulLogMakerLauncher(LogMakerParameters logMakParams) {
		this.logMakParams = logMakParams;
	}
	
	public MinerFulLogMakerLauncher(AssignmentModel declareMapModel, LogMakerParameters logMakParams) {
		this(logMakParams);

		this.inputProcess = new ProcessModelLoader().loadProcessModel(declareMapModel);
	}

	public MinerFulLogMakerLauncher(ProcessModel minerFulProcessModel, LogMakerParameters logMakParams) {
		this(logMakParams);

		this.inputProcess = minerFulProcessModel;
	}

	public MinerFulLogMakerLauncher(InputModelParameters inputParams, 
			LogMakerParameters logMakParams, SystemCmdParameters systemParams) {
		this(logMakParams);

		this.inputProcess = new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile);
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);
	}
	
	public void makeLog() {
		if (this.logMakParams.outputLogFile == null) {
			throw new IllegalArgumentException("Output file for log storage not specified!");
		}
		/*
		 * Creates the log.
		 */
		MinerFulLogMaker logMak = new MinerFulLogMaker(logMakParams);
		logMak.createLog(this.inputProcess);
		
		try {
			logMak.storeLog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}