package minerful;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.checking.ProcessSpecificationFitnessEvaluator;
import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.io.ProcessModelLoader;
import minerful.io.params.InputModelParameters;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulFitnessCheckLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulFitnessCheckLauncher.class);
			
	private ProcessModel inputProcess;
	private LogParser inputLog;
	private CheckingCmdParameters chkParams;
	
	private MinerFulFitnessCheckLauncher(CheckingCmdParameters chkParams) {
		this.chkParams = chkParams;
	}
	
	public MinerFulFitnessCheckLauncher(AssignmentModel declareMapModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.inputProcess = new ProcessModelLoader().loadProcessModel(declareMapModel);
		this.inputLog = inputLog;
	}

	public MinerFulFitnessCheckLauncher(ProcessModel minerFulProcessModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.inputProcess = minerFulProcessModel;
		this.inputLog = inputLog;
	}

	public MinerFulFitnessCheckLauncher(InputModelParameters inputParams, 
			InputLogCmdParameters inputLogParams, CheckingCmdParameters chkParams, SystemCmdParameters systemParams) {
		this(chkParams);

		this.inputProcess = new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile);
		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}
		this.inputLog = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);

		MessagePrinter.configureLogging(systemParams.debugLevel);
	}
	
	public ProcessModel check() {
		ProcessSpecificationFitnessEvaluator evalor = new ProcessSpecificationFitnessEvaluator(
				this.inputLog.getEventEncoderDecoder(), this.inputProcess);

		ModelFitnessEvaluation evalon = evalor.evaluateOnLog(this.inputLog);
		
		reportOnEvaluation(evalon);
		
	    return inputProcess;
	}

	public ProcessModel check(LogTraceParser trace) {
		ProcessSpecificationFitnessEvaluator evalor = new ProcessSpecificationFitnessEvaluator(
				this.inputLog.getEventEncoderDecoder(), this.inputProcess);
		
		ModelFitnessEvaluation evalon = evalor.evaluateOnTrace(trace);
		
		reportOnEvaluation(evalon);
		
		return inputProcess;
	}

	private void reportOnEvaluation(ModelFitnessEvaluation evalon) {
		if (evalon.isFullyFitting()) {
			logger.info("Yay! The passed declarative process specification is fully fitting with the input traces!");
		} else {
			logger.warn(
					"The passed declarative process specification is not fully fitting with the input traces"
					+ ((chkParams.fileToSaveResultsAsCSV == null) ?
							". See below for further details." :
							". See " + chkParams.fileToSaveResultsAsCSV.getAbsolutePath() + " for further details.")
					);
		}
		
		if (chkParams.fileToSaveResultsAsCSV != null) {
			logger.info("Saving results in CSV format as " + chkParams.fileToSaveResultsAsCSV + "...");
			PrintWriter outWriter = null;
        	try {
    				outWriter = new PrintWriter(chkParams.fileToSaveResultsAsCSV);
    	        	outWriter.print(evalon.printCSV());
    	        	outWriter.flush();
    	        	outWriter.close();
    			} catch (FileNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
		}
	}

}