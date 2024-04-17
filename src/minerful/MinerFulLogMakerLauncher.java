package minerful;

import java.io.IOException;
import java.io.FileNotFoundException;


import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.concept.ProcessSpecification;
import minerful.concept.constraint.Constraint;
import minerful.io.ProcessSpecificationLoader;
import minerful.io.params.InputSpecificationParameters;
import minerful.logmaker.MinerFulLogMaker;
import minerful.logmaker.params.LogMakerParameters;
import minerful.params.SystemCmdParameters;
import minerful.utils.MessagePrinter;

/**
 * Launches the generation of event logs from declarative process specifications.
 */
public class MinerFulLogMakerLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulLogMakerLauncher.class);
			
	private ProcessSpecification inputProcess;
	private LogMakerParameters logMakParams;
	private ProcessSpecification negProcessSpecification;
	
	private MinerFulLogMakerLauncher(LogMakerParameters logMakParams) {
		this.logMakParams = logMakParams;
	}
	
	public MinerFulLogMakerLauncher(AssignmentModel declareMapModel, LogMakerParameters logMakParams) {
		this(logMakParams);

		this.inputProcess = new ProcessSpecificationLoader().loadProcessSpecification(declareMapModel);
	}

	public MinerFulLogMakerLauncher(ProcessSpecification minerFulProcessSpecification, LogMakerParameters logMakParams) {
		this(logMakParams);

		this.inputProcess = minerFulProcessSpecification;
	}

	public MinerFulLogMakerLauncher(InputSpecificationParameters inputParams, 
			LogMakerParameters logMakParams, SystemCmdParameters systemParams) {
		this(logMakParams);

		this.inputProcess = new ProcessSpecificationLoader().loadProcessSpecification(inputParams.inputLanguage, inputParams.inputFile);
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process specification file missing!");
			System.exit(1);
		}
		
		if (logMakParams.negativeConstraintsFile != null){
			this.negProcessSpecification = new ProcessSpecificationLoader().loadNegatedProcessSpecification(logMakParams.negativeConstraintsFile);		
		}else{
			this.negProcessSpecification = null;
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
		logMak.createLog(this.inputProcess, this.negProcessSpecification);
		
		try {
			logMak.storeLog();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}