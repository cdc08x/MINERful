package minerful.examples.api.fitness;

import java.io.File;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.declareminer.visualizing.DeclareMap;

import minerful.MinerFulMinerLauncher;
import minerful.checking.integration.prom.ModelFitnessEvaluatorOpenXesInterface;
import minerful.checking.relevance.dao.ModelFitnessEvaluation;
import minerful.concept.ProcessModel;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.InputLogCmdParameters.EventClassification;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

public class FitnessCheckOfDiscoveredProcessModelWithOpenXes {
	private static final String EXAMPLE_LOG_FILE_1 = "/home/claudio/Code/MINERful/logs/BPIC2017/BPI-Challenge-2017-sample1.xes";
	private static final String EXAMPLE_LOG_FILE_2 = "/home/claudio/Code/MINERful/logs/BPIC2017/BPI-Challenge-2017-sample2.xes";
	private static final String EXAMPLE_LOG_FILE_3 = "/home/claudio/Code/MINERful/logs/BPIC2017/BPI-Challenge-2017-sample3.xes";
	private static final int EXAMPLE_XTRACE_PICK_2 = 64;
	private static final int EXAMPLE_XTRACE_PICK_3 = 128;

	public static void main(String[] args) {
//////////////////////////////////////////////////////////////////
//Discovery phase
//////////////////////////////////////////////////////////////////
		// Initialising parameters. Read their documentation to know more about their customisations
		InputLogCmdParameters inputLogParams = new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams = new MinerFulCmdParameters();
		SystemCmdParameters systemParams = new SystemCmdParameters();
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
		
		// Loading log
		XLog myXLog = null;
		try {
			myXLog = new XesXmlParser().parse(new File(EXAMPLE_LOG_FILE_1)).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		inputLogParams.eventClassification = EventClassification.name;
		// // Use the one below if you want to classify events not just by their task name!
		// inputLogParams.eventClassification = EventClassification.logspec;
		postParams.supportThreshold = 0.95; // For a sure total fit with the event log, this parameter should be set to 1.0
		postParams.confidenceThreshold = 0.66; // The higher this is, the higher the frequency of occurrence of tasks triggering the returned constraints
		postParams.interestFactorThreshold = 0.5; // The higher this is, the higher the frequency of occurrence of tasks involved in the returned constraints
		
		// Remove redundant constraints. WARNING: this may take some time.
		// The language of the model remains completely unchanged. What changes is the number of constraints in it.
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		// To leave the default post-processing, comment the line above. To completely remove any form of post-processing, comment the line above and uncomment the following one
		// postParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		
		// Run the discovery algorithm
		System.out.println("Running the discovery algorithm...");
		
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputLogParams, minerFulParams, postParams, systemParams);
		ProcessModel processModel = miFuMiLa.mine(myXLog);
		
		System.out.println("...Done");
		
		// In case we want a reference to an equivalent DeclareMap, use the converter as in the line below.
		// Notice that the graphical position of elements in the Declare Map is not rendered here.
		// Watch out though: it might take time! In case you do not need this instruction, just comment the following two lines.
		DeclareMap declareMap = new DeclareMapEncoderDecoder(processModel).createDeclareMap();
		System.out.println("The constraint definitions in the Declare Map amount to: " + declareMap.getModel().constraintDefinitionsCount());
		
		// Notice that there is also a method that returns directly a DeclareMap upon mining the event log:
		// DeclareMap declareMap = miFuMiLa.mineDeclareMap(myXLog);

//////////////////////////////////////////////////////////////////
//Evaluation phase on an entire log (an instance of OpenXES XLog)
//////////////////////////////////////////////////////////////////
		try {
			myXLog = new XesXmlParser().parse(new File(EXAMPLE_LOG_FILE_2)).get(0);
			// // If you want a perfect fit, comment the line above and uncomment the one below
			// myXLog3 = new XesXmlParser().parse(new File(EXAMPLE_LOG_FILE_1)).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		ModelFitnessEvaluatorOpenXesInterface xEvalor = new ModelFitnessEvaluatorOpenXesInterface(
				myXLog,
				ClassificationType.NAME,
				// // In case you opted for EventClassification.logspec in the discovery phase above, you might want to replace the previous line with the following
				// ClassificationType.LOG_SPECIFIED,
				processModel);
		
		ModelFitnessEvaluation xEvalon = xEvalor.evaluateOnLog();
		
		if (xEvalon.isFullyFitting()) {
			System.out.println("\nWhooppee! The event log in " + EXAMPLE_LOG_FILE_2 + " is perfectly fitting!\n"); // unlikely, if it is not the same log!
		} else {
			System.out.println(
					"\nThe event log in " + EXAMPLE_LOG_FILE_2 + " did not comply with all constraints."
							+ " The average fitness is " + xEvalon.avgFitness()
							+ ". Details follow.\n\n"
					+ xEvalon.printCSV());
		}

//////////////////////////////////////////////////////////////////
//Evaluation phase specifically on a single trace of that XLog
//////////////////////////////////////////////////////////////////
		XTrace myXTrace = myXLog.get(EXAMPLE_XTRACE_PICK_2);
		
		xEvalon = xEvalor.evaluateOnTrace(myXTrace);
		
		// Same code as above
		if (xEvalon.isFullyFitting()) {
			System.out.println("\nWhooppee! Trace " + EXAMPLE_XTRACE_PICK_2 + " of " + EXAMPLE_LOG_FILE_2 + " is perfectly fitting!\n");
		} else {
			System.out.println(
					"\nTrace " + EXAMPLE_XTRACE_PICK_2 + " of " + EXAMPLE_LOG_FILE_2 + " did not comply with all constraints."
							+ " The average fitness is " + xEvalon.avgFitness()
							+ ". Details follow.\n\n"
					+ xEvalon.printCSV());
		}

//////////////////////////////////////////////////////////////////
//Evaluation phase specifically on a trace picked out of another XLog.
//////////////////////////////////////////////////////////////////

		// Loading log
		XLog myXLog3 = null;
		try {
			myXLog3 = new XesXmlParser().parse(new File(EXAMPLE_LOG_FILE_3)).get(0);
			// // If you want a perfect fit, comment the line above and uncomment the one below
			// myXLog3 = new XesXmlParser().parse(new File(EXAMPLE_LOG_FILE_1)).get(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		XTrace myXTrace3 = myXLog3.get(EXAMPLE_XTRACE_PICK_3);
		// This time the event log has changed, so we have to instantiate a new ModelFitnessEvaluatorOpenXesInterface
		ModelFitnessEvaluatorOpenXesInterface xEvalor3 = new ModelFitnessEvaluatorOpenXesInterface(
				myXLog3,
				ClassificationType.NAME,
				// // In case you opted for EventClassification.logspec in the discovery phase above, you might want to replace the previous line with the following
				// ClassificationType.LOG_SPECIFIED,
				processModel);
		
		// Check the process model extracted from EXAMPLE_LOG_FILE_1 against the first trace of EXAMPLE_LOG_FILE_3
		ModelFitnessEvaluation xEvalon3 = xEvalor3.evaluateOnTrace(myXTrace3);
		
		// Same code as above
		if (xEvalon3.isFullyFitting()) {
			System.out.println("\nWhooppee! Trace " + EXAMPLE_XTRACE_PICK_3 + " of " + EXAMPLE_LOG_FILE_3 + " is perfectly fitting!\n");
		} else {
			System.out.println(
					"\nTrace " + EXAMPLE_XTRACE_PICK_3 + " of " + EXAMPLE_LOG_FILE_3 + " did not comply with all constraints."
							+ " The average fitness is " + xEvalon.avgFitness()
							+ ". Details follow.\n\n"
					+ xEvalon.printCSV());
		}
		
		System.exit(0);
	}
}