package minerful.examples.api.simplification;

import java.io.File;

import minerful.MinerFulOutputManagementLauncher;
import minerful.MinerFulSimplificationLauncher;
import minerful.concept.ProcessSpecification;
import minerful.index.comparator.modular.ConstraintSortingPolicy;
import minerful.io.params.InputSpecificationParameters;
import minerful.io.params.InputSpecificationParameters.InputEncoding;
import minerful.io.params.OutputSpecificationParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * This example class demonstrates how to load a Declare Map file as a process specification, then run the simplification engine of MINERful to remove the redundant constraints.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class MinerFulSimplificationInvokerOnDeclareMapFile {

	private static final String EXAMPLE_INPUT_PROCESS_SPECIFICATION_FILE = "/home/claudio/Code/MINERful/models/mined/bpi_challenge_2013_closed_problems-model-s075.xml";

	public static void main(String[] args) {
		InputSpecificationParameters inputParams = new InputSpecificationParameters();
		PostProcessingCmdParameters postParams = new PostProcessingCmdParameters();
		ViewCmdParameters viewParams = new ViewCmdParameters();
		OutputSpecificationParameters outParams = new OutputSpecificationParameters();
		SystemCmdParameters systemParams = new SystemCmdParameters();

		// Specifies the type of post-processing analysis, through which getting rid of redundancies or conflicts in the process specification
		postParams.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		// Policies according to which constraints are ranked in terms of significance. The position in the array reflects the order with which the policies are used. When a criterion does not establish which constraint in a pair should be put ahead in the ranking, the following one in the array is utilised. 
		postParams.sortingPolicies = new ConstraintSortingPolicy[]{
			ConstraintSortingPolicy.ACTIVATIONTARGETBONDS,
			ConstraintSortingPolicy.FAMILYHIERARCHY,
			ConstraintSortingPolicy.SUPPORTCONFIDENCECOVERAGE
		};

		// Specifies the input file where the specification is stored
		inputParams.inputFile=new File(EXAMPLE_INPUT_PROCESS_SPECIFICATION_FILE);
		// Specifies that the input specification is stored in the input file as a Declare Map. It can also be a MINERful output specification.
		inputParams.inputLanguage=InputEncoding.DECLARE_MAP;
		// If you want to try with a MINERful-XML specification, comment the line above and uncomment the one below.
		// inputParams.inputLanguage=InputEncoding.MINERFUL;
		
		MinerFulSimplificationLauncher miFuSimpLa = new MinerFulSimplificationLauncher(inputParams, postParams, systemParams);
		
		/*
		 * Should the process specification be already in memory, it does not make much sense to write it into a file and then read that file. In such a case, please refer to the following constructors for MinerFulSimplificationLauncher:
		 * MinerFulSimplificationLauncher(AssignmentModel declareMapModel, PostProcessingCmdParameters postParams)
		 * MinerFulSimplificationLauncher(ProcessSpecification minerFulProcessSpecification, PostProcessingCmdParameters postParams)
		 */
		
		ProcessSpecification processSpecification = miFuSimpLa.simplify();
		
		// To store the simplified process specification file somewhere. Please mind that the process specification can also be stored as a Declare map. See the specification of minerful.io.params.OutputSpecificationParameters
		//outParams.fileToSaveAsXML = new File(EXAMPLE_OUTPUT_PROCESS_SPECIFICATION_FILE);
		
		MinerFulOutputManagementLauncher outputMgt = new MinerFulOutputManagementLauncher();
		outputMgt.manageOutput(processSpecification, viewParams, outParams, systemParams);
		
		System.out.println("Simplified specification: " + processSpecification);
		
		System.exit(0);
	}
}
