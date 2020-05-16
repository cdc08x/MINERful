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
import minerful.miner.core.MinerFulPruningCore;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulFitnessCheckLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulFitnessCheckLauncher.class);
			
	private ProcessModel processSpecification;
	private LogParser eventLogParser;
	private CheckingCmdParameters chkParams;
	
	private MinerFulFitnessCheckLauncher(CheckingCmdParameters chkParams) {
		this.chkParams = chkParams;
	}
	
	public MinerFulFitnessCheckLauncher(AssignmentModel declareMapModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.processSpecification = new ProcessModelLoader().loadProcessModel(declareMapModel);
		this.eventLogParser = inputLog;
	}

	public MinerFulFitnessCheckLauncher(ProcessModel minerFulProcessModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.processSpecification = minerFulProcessModel;
		this.eventLogParser = inputLog;
	}

	public MinerFulFitnessCheckLauncher(InputModelParameters inputParams, PostProcessingCmdParameters preProcParams,
			InputLogCmdParameters inputLogParams, CheckingCmdParameters chkParams, SystemCmdParameters systemParams) {
		this(chkParams);

		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}
		// Load the process specification from the file
		this.processSpecification = 
				new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile);

		// Apply some preliminary pruning
		MinerFulPruningCore pruniCore = new MinerFulPruningCore(this.processSpecification, preProcParams);
		this.processSpecification.bag = pruniCore.massageConstraints();

		this.eventLogParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);

		// Notice that the merging of event log codification of TaskChars with the given model’s one happens only late (at checking time)
		MessagePrinter.configureLogging(systemParams.debugLevel);
	}

	public ProcessModel getProcessSpecification() {
		return processSpecification;
	}

	public LogParser getEventLogParser() {
		return eventLogParser;
	}
	
	public ModelFitnessEvaluation check() {
		ProcessSpecificationFitnessEvaluator evalor = new ProcessSpecificationFitnessEvaluator(
				this.eventLogParser.getEventEncoderDecoder(), this.processSpecification);

		ModelFitnessEvaluation evalon = evalor.evaluateOnLog(this.eventLogParser);
		
		reportOnEvaluation(evalon);
		
	    return evalon;
	}

	public ModelFitnessEvaluation check(LogTraceParser trace) {
		ProcessSpecificationFitnessEvaluator evalor = new ProcessSpecificationFitnessEvaluator(
				this.eventLogParser.getEventEncoderDecoder(), this.processSpecification);
		
		ModelFitnessEvaluation evalon = evalor.evaluateOnTrace(trace);
		
		reportOnEvaluation(evalon);
		
		return evalon;
	}
	
	private static String printFitnessJsonSummary(ModelFitnessEvaluation evalon) {
		return "{\"Avg.fitness\":" 
				+ MessagePrinter.formatFloatNumForCSV(evalon.avgFitness()) + ";" 
				+ "\"Trace-fit-ratio\":" 
				+ MessagePrinter.formatFloatNumForCSV(evalon.traceFitRatio())
				+ "}";
	}

	private void reportOnEvaluation(ModelFitnessEvaluation evalon) {
		if (evalon.isFullyFitting()) {
			logger.info("Yay! The passed declarative process specification is fully fitting with the input traces! Summary:\n"
			+ printFitnessJsonSummary(evalon) + "\n");
		} else {
			logger.warn(
					"The passed declarative process specification is not fully fitting with the input traces. Summary:\n"
					+ printFitnessJsonSummary(evalon) + "\n"
					+ ((chkParams.fileToSaveResultsAsCSV == null) ?
							"See below for further details." :
							"See " + chkParams.fileToSaveResultsAsCSV.getAbsolutePath() + " for further details.")
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