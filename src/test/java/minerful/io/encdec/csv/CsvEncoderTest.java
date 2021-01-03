package minerful.io.encdec.csv;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;

/**
 * Tests for class CsvEncoder
 */
class CsvEncoderTest {
	
	ProcessModel defaultProcessModel;
	
	@BeforeEach
	void before() {
		defaultProcessModel = createProcessModel();
	}
	
	@Test
	void testPrintBag() {
		CsvEncoder csvEncoder = new CsvEncoder();
		Assert.assertEquals("'Constraint';'Template';'Activation';'Target';'Support';'Confidence level';'Interest factor'\n"
				+ "'AtMostOne(a)';'AtMostOne';'a';;1.000;0.000;0.000\n"
				+ "'End(a)';'End';'a';;1.000;0.000;0.000\n", csvEncoder.printAsCsv(Arrays.asList(CsvEncoder.PRINT_OUT_ELEMENT.values()), defaultProcessModel));
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
