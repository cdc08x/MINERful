package minerful.postprocessing.pruning;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters.PostProcessingAnalysisType;

/**
 * Tests for class ConflictAndRedundancyResolver
 */
class ConflictAndRedundancyResolverTest {

	ProcessModel defaultProcessModel;

	@BeforeEach
	void before() {
		defaultProcessModel = createProcessModel();
	}

	@Test
	void testResolveConflictsOrRedundancies() {
		
		PostProcessingCmdParameters postProcessingCmdParameters = new PostProcessingCmdParameters();
		postProcessingCmdParameters.postProcessingAnalysisType = PostProcessingAnalysisType.HIERARCHYCONFLICTREDUNDANCYDOUBLE;
		
		ConflictAndRedundancyResolver conflictAndRedundancyResolver = new ConflictAndRedundancyResolver(
				defaultProcessModel, postProcessingCmdParameters);
		conflictAndRedundancyResolver.init();
		ProcessModel adjustedProcessModel = conflictAndRedundancyResolver.resolveConflictsOrRedundancies();
	}

	/**
	 * Creates an example processModel which is used for testing
	 * 
	 * @return example process model
	 */
	private ProcessModel createProcessModel() {
		// Create constraints bag
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.add(tCh1, c1);
		constraintsBag.add(tCh1, c2);

		ProcessModel processModel = new ProcessModel(constraintsBag);

		return processModel;
	}

}
