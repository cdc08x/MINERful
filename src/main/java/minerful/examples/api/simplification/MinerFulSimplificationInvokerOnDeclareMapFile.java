package minerful.examples.api.simplification;

import java.io.File;

import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessModel;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.io.params.InputModelParameters;
import minerful.io.params.InputModelParameters.InputEncoding;
import minerful.io.params.OutputModelParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to load a Declare Map file as a process model, then run the simplification engine of MINERful to remove the redundant constraints.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class MinerFulSimplificationInvokerOnDeclareMapFile {

	private static final String EXAMPLE_OUTPUT_PROCESS_MODEL_FILE = "/home/claudio/Desktop/example-model.xml";
	private static final String EXAMPLE_INPUT_PROCESS_MODEL_FILE = "/home/claudio/Code/MINERful/models/mined/bpi_challenge_2013_closed_problems-model-s075.xml";

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

		// Specifies the input file where the model is stored
		inputParams.inputFile=new File(EXAMPLE_INPUT_PROCESS_MODEL_FILE);
		// Specifies that the input model is stored in the input file as a Declare Map. It can also be a MINERful output model.
		inputParams.inputLanguage=InputEncoding.MINERFUL;
		
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