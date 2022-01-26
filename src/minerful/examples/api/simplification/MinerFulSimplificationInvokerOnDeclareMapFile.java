package minerful.examples.api.simplification;

import java.io.File;

import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.io.params.InputModelParameters;
import minerful.io.params.InputModelParameters.InputEncoding;
import minerful.io.params.OutputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to load a Declare Map file as a process model,
 * then run the simplification engine of MINERful to remove the redundant constraints.
 * Here we ignore the thresholds for constraint measures (i.e., support and the others).
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class MinerFulSimplificationInvokerOnDeclareMapFile {

	private static final String EXAMPLE_OUTPUT_PROCESS_MODEL_FILE = "/home/cdc08x/Desktop/example-model.xml";
	private static final String EXAMPLE_INPUT_PROCESS_MODEL_FILE = 
			"/home/cdc08x/Code/MINERful-dev/models/examples-FMM/DeclareMap3.xml";
	// Comment the line above and uncomment the line below if you want to try with a MINERful-XML specification
	//		"/home/cdc08x/Code/MINERful-dev/models/mined/bpi_challenge_2013_closed_problems-model-s05-c02-i00.xml";

	public static void main(String[] args) {
		InputModelParameters inputParams = new InputModelParameters();
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
		ViewCmdParameters viewParams = new ViewCmdParameters();
		OutputModelParameters outParams = new OutputModelParameters();
		SystemCmdParameters systemParams = new SystemCmdParameters();

		// Specifies the type of post-processing analysis, through which getting rid of redundancies or conflicts in the process model
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		// Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following one in the array is utilised. 
		postParams.sortingPolicies = new ConstraintSortingPolicy[]{
			ConstraintSortingPolicy.ACTIVATIONTARGETBONDS,
			ConstraintSortingPolicy.FAMILYHIERARCHY,
			ConstraintSortingPolicy.SUPPORTCONFIDENCEINTERESTFACTOR
		};
		// Set the measures' thresholds to the minimum
		postParams.supportThreshold = Constraint.MIN_SUPPORT;
		postParams.confidenceThreshold = Constraint.MIN_CONFIDENCE;
		postParams.interestFactorThreshold = Constraint.MIN_INTEREST_FACTOR;

		// Specifies the input file where the model is stored
		inputParams.inputFile=new File(EXAMPLE_INPUT_PROCESS_MODEL_FILE);
		// Specifies that the input model is stored in the input file as a Declare Map. It can also be a MINERful output model.
		inputParams.inputLanguage=InputEncoding.DECLARE_MAP;
		// If you want to try with a MINERful-XML specification, comment the line above and uncomment the one below.
		// inputParams.inputLanguage=InputEncoding.MINERFUL;
		
		MinerFulSimplificationLauncher miFuSimpLa = new MinerFulSimplificationLauncher(inputParams, postParams, systemParams);
		
		/*
		 * Should the process model be already in memory, it does not make much sense to write it into a file and then read that file. In such a case, please refer to the following constructors for MinerFulSimplificationLauncher:
		 * MinerFulSimplificationLauncher(AssignmentModel declareMapModel, PostProcessingCmdParameters postParams)
		 * MinerFulSimplificationLauncher(ProcessModel minerFulProcessModel, PostProcessingCmdParameters postParams)
		 */
		
		ProcessModel processModel = miFuSimpLa.simplify();
		
		// To store the simplified process model file somewhere. Please mind that the process model can also be stored as a Declare map. See the specification of minerful.io.params.OutputModelParameters
		outParams.fileToSaveAsXML = new File(EXAMPLE_OUTPUT_PROCESS_MODEL_FILE);
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processModel, viewParams, outParams, systemParams);
		
		System.out.println("Simplified model: " + processModel);
		
		System.exit(0);
	}
}