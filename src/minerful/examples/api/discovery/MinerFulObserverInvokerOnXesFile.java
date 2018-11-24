package minerful.examples.api.discovery;

import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintChange;
import minerful.io.params.OutputModelParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to invoke the MINERful miner as an API, and subsequently observe the
 * changes that are applied to the process model from the MinerFulSimplificationLauncher.
 * Lastly, we save the model as a Declare Map file.
 * 
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 * 
 */
public class MinerFulObserverInvokerOnXesFile implements Observer {

	public static void main(String[] args) {
//////////////////////////////////////////////////////////////////
//		Discovery phase
//////////////////////////////////////////////////////////////////

		InputLogCmdParameters inputParams =
				new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters();
		ViewCmdParameters viewParams =
				new ViewCmdParameters();
		OutputModelParameters outParams =
				new OutputModelParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters();
		
		inputParams.inputLogFile = new File("/home/claudio/Code/MINERful/logs/BPIC2012/financial_log.xes.gz");
		postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.25;
		postParams.interestFactorThreshold = 0.125;
		
		// Optionally, exclude some tasks from the analysis.
		minerFulParams.activitiesToExcludeFromResult = new ArrayList<String>();
		minerFulParams.activitiesToExcludeFromResult.add("W_Wijzigen contractgegevens");
		minerFulParams.activitiesToExcludeFromResult.add("W_Valideren aanvraag");
		minerFulParams.activitiesToExcludeFromResult.add("W_Completeren aanvraag");
		
		// With the following option set up to "false", redundant/inconsistent/below-thresholds constraints are retained in the model, although marked as redundant/inconsistent/below-thresholds
		postParams.cropRedundantAndInconsistentConstraints = false;
		
		// To completely remove any form of post-processing, uncomment the following line:
		// postParams.analysisType = PostProcessingAnalysisType.NONE;
		
		// Run the discovery algorithm
		System.out.println("Running the discovery algorithm...");
		
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		ProcessModel processModel = miFuMiLa.mine();
		
		System.out.println("...Done");

//////////////////////////////////////////////////////////////////
//		Observing the changes in the model with the "observer" pattern implementation
//////////////////////////////////////////////////////////////////

		// Start observing changes in the model
		System.out.println("Starting to observe the changes in the process model...");
		
		processModel.addObserver(new MinerFulObserverInvokerOnXesFile());
		
//////////////////////////////////////////////////////////////////
//		Simplification phase
//////////////////////////////////////////////////////////////////

		// Set up the new options for the simplification tool. Beware that untouched options stay the same, of course.
		postParams.supportThreshold = 0.9;
		postParams.confidenceThreshold = 0.5;
		postParams.interestFactorThreshold = 0.25;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		
		// Run the simplification algorithm
		System.out.println("Running the simplification algorithm...");
		
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processModel, postParams);
		miFuSiLa.simplify();

		System.out.println("...Done");
	
//////////////////////////////////////////////////////////////////
//		Saving
//////////////////////////////////////////////////////////////////

		// Specify the output files locations
		// Please notice that only the XML-saved model may contain also the redundant/conflicting/below-the-thresholds constraints.
		// To do so, the
		//		postParams.cropRedundantAndInconsistentConstraints = false;
		// directive was given. By leaving the default value (true), the model does NOT contain the redundant/conflicting/below-the-thresholds constraints.
		outParams.fileToSaveAsXML = new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco-minerful.xml");
		// Please notice that NONE of the Declare-map XML-, JSON-, or CSV-formatted copies contain the redundant/conflicting/below-the-thresholds constraints.
		outParams.fileToSaveAsConDec = new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco-declaremap.xml");
		outParams.fileToSaveAsJSON = new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco.json");
		outParams.fileToSaveConstraintsAsCSV = new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco.csv");
		
		System.out.println("Saving...");
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);
	
		System.out.println("...Done");		
		
//////////////////////////////////////////////////////////////////
//		Cropping the identified redundant/inconsistent constraints
//////////////////////////////////////////////////////////////////

		// Let us minimise the model now, by removing the redundant/conflicting/below-the-thresholds constraints.
		postParams.cropRedundantAndInconsistentConstraints = true;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		// It is not necessary to go again through all checks again, if we do not want to change the thresholds or the conflict/redundancy-check policies: it is just enough to set the previous option to "true"
		
		System.out.println("Removing the already detected inconsistencies/redundancies...");
		
		miFuSiLa.simplify();
		
		System.out.println("...Done");
		
//////////////////////////////////////////////////////////////////
//		Saving again...
//////////////////////////////////////////////////////////////////

		outParams.fileToSaveAsXML = new File("/home/claudio/Code/MINERful/temp/BPIC2012-disco-minerful-min.xml");

		System.out.println("Saving...");
		
		outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);
	
		System.out.println("...Done");		
		
		// That's all for now
		System.exit(0);
	}

	/**
	 * Just a simple implementation of the method to implement for observers on the process model.
	 * It prints what happened.
	 */
	@Override
	public void update(Observable o, Object arg) {
		// Just to check whether "o", namely the notifier, is a process model.
		// Until a new Observer-observable framework is not provided for other objects of MINERful, this check is basically useless.
		if (ProcessModel.class.isAssignableFrom(o.getClass())) {
			ConstraintChange change = (ConstraintChange) arg;
			System.out.println("Change detected! "
					+ "Constraint "
					+ change.constraint
					+ " has updated its " 
					+ change.property
					+ " to the new value of "
					+ change.value);
			// The following line is used to show that one can access all properties of the modified constraint.
			System.out.println("\tIs it suitable for elimination? "
					+ change.constraint.isMarkedForExclusion());
		}
	}
}