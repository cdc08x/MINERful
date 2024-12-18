package minerful.examples.api.discovery;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

import minerful.MinerFulMinerLauncher;
import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessSpecification;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintChange;
import minerful.io.params.OutputSpecificationParameters;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to invoke the MINERful miner as an API, and subsequently observe the
 * changes that are applied to the process specification from the MinerFulSimplificationLauncher.
 * Lastly, we save the specification as a Declare Map file.
 * 
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 * 
 */
public class MinerFulObserverInvokerOnXesFile implements PropertyChangeListener {

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
		OutputSpecificationParameters outParams =
				new OutputSpecificationParameters();
		SystemCmdParameters systemParams =
				new SystemCmdParameters();
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters();
		
		inputParams.inputLogFile = new File(MinerFulObserverInvokerOnXesFile.class.getClassLoader().getResource("examples/running-example.xes").getFile());
		postParams.evtSupportThreshold = 0.9;
		postParams.evtConfidenceThreshold = 0.25;
		postParams.evtCoverageThreshold = 0.125;
		
		// Optionally, exclude some tasks from the analysis.
		minerFulParams.tasksToBeExcludedFromResult = new ArrayList<String>();
		minerFulParams.tasksToBeExcludedFromResult.add("W_Wijzigen contractgegevens");
		minerFulParams.tasksToBeExcludedFromResult.add("W_Valideren aanvraag");
		minerFulParams.tasksToBeExcludedFromResult.add("W_Completeren aanvraag");
		
		// With the following option set up to "false", redundant/inconsistent/below-thresholds constraints are retained in the specification, although marked as redundant/inconsistent/below-thresholds
		postParams.cropRedundantAndInconsistentConstraints = false;
		
		// To completely remove any form of post-processing, uncomment the following line:
		// postParams.analysisType = PostProcessingAnalysisType.NONE;
		
		// Run the discovery algorithm
		System.out.println("Running the discovery algorithm...");
		
		MinerFulMinerLauncher miFuMiLa = new MinerFulMinerLauncher(inputParams, minerFulParams, postParams, systemParams);
		ProcessSpecification processSpecification = miFuMiLa.mine();
		
		System.out.println("...Done");

//////////////////////////////////////////////////////////////////
//		Observing the changes in the specification with the "observer" pattern implementation
//////////////////////////////////////////////////////////////////

		// Start observing changes in the specification
		System.out.println("Starting to observe the changes in the process specification...");
		
		processSpecification.addPropertyChangeListener(new MinerFulObserverInvokerOnXesFile());
		
//////////////////////////////////////////////////////////////////
//		Simplification phase
//////////////////////////////////////////////////////////////////

		// Set up the new options for the simplification tool. Beware that untouched options stay the same, of course.
		postParams.evtSupportThreshold = 0.9;
		postParams.evtConfidenceThreshold = 0.5;
		postParams.evtCoverageThreshold = 0.25;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		
		// Run the simplification algorithm
		System.out.println("Running the simplification algorithm...");
		
		MinerFulSimplificationLauncher miFuSiLa = new MinerFulSimplificationLauncher(processSpecification, postParams);
		miFuSiLa.simplify();

		System.out.println("...Done");
	
//////////////////////////////////////////////////////////////////
//		Saving
//////////////////////////////////////////////////////////////////

		// Specify the output files locations
		// Please notice that only the XML-saved specification may contain also the redundant/conflicting/below-the-thresholds constraints.
		// To do so, the
		//		postParams.cropRedundantAndInconsistentConstraints = false;
		// directive was given. By leaving the default value (true), the specification does NOT contain the redundant/conflicting/below-the-thresholds constraints.
		String pathToExampleOutput = Paths.get("").toAbsolutePath().getParent().toString() + "/example-output";
		new File(Paths.get("").toAbsolutePath().getParent().toString() + "/example-output").mkdirs();

		//outParams.fileToSaveAsXML = new File(pathToExampleOutput +"/BPIC2012-disco-minerful.xml");
		// Please notice that NONE of the Declare-map XML-, JSON-, or CSV-formatted copies contain the redundant/conflicting/below-the-thresholds constraints.
		outParams.fileToSaveAsConDec = new File(pathToExampleOutput + "/BPIC2012-disco-declaremap.xml");
		outParams.fileToSaveAsJSON = new File(pathToExampleOutput + "/BPIC2012-disco.json");
		outParams.fileToSaveConstraintsAsCSV = new File(pathToExampleOutput +"/BPIC2012-disco.csv");

		System.out.println("Saving...");
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processSpecification, viewParams, outParams, systemParams);
	
		System.out.println("...Done");		
		
//////////////////////////////////////////////////////////////////
//		Cropping the identified redundant/inconsistent constraints
//////////////////////////////////////////////////////////////////

		// Let us minimise the specification now, by removing the redundant/conflicting/below-the-thresholds constraints.
		postParams.cropRedundantAndInconsistentConstraints = true;
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.NONE;
		// It is not necessary to go again through all checks again, if we do not want to change the thresholds or the conflict/redundancy-check policies: it is just enough to set the previous option to "true"
		
		System.out.println("Removing the already detected inconsistencies/redundancies...");
		
		miFuSiLa.simplify();
		
		System.out.println("...Done");
		
//////////////////////////////////////////////////////////////////
//		Saving again...
//////////////////////////////////////////////////////////////////

		//outParams.fileToSaveAsXML = new File(pathToExampleOutput + "/running-examples2.xml");

		System.out.println("Saving...");
		
		outputMgt.manageOutput(processSpecification, viewParams, outParams, systemParams);
	
		System.out.println("...Done");		
		
		// That's all for now
		System.exit(0);
	}

	/**
	 * Just a simple implementation of the method to implement for PropertyListener on the process specification.
	 * It prints what happened.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Just to check whether "o", namely the notifier, is a process specification.
		// Until a new Observer-observable framework is not provided for other objects of MINERful, this check is basically useless.
		Constraint constraint = (Constraint) evt.getSource();

		System.out.println("Change detected! "
				+ "Constraint "
				+ constraint.toString()
				+ " has updated its " 
				+ evt.getPropertyName()
				+ " to the new value of "
				+ evt.getNewValue());
		// The following line is used to show that one can access all properties of the modified constraint.
		System.out.println("\tIs it suitable for elimination? "
				+ constraint.isMarkedForExclusionOrForbidden());

	}

}
