package minerful.io;

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
 * Tests for class ConstraintsPrinter
 */
class ConstraintsPrinterTest {

	ProcessModel defaultProcessModel;

	@BeforeEach
	void before() {
		defaultProcessModel = createProcessModel();
	}

	@Test
	void testPrintBag() {
		ConstraintsPrinter constPrinter = new ConstraintsPrinter(defaultProcessModel);
		Assert.assertTrue(constPrinter.printBag().contains("100.000% AtMostOne(a)    conf.:   0.000;  int'f:   0.000;"));
		Assert.assertTrue(constPrinter.printBag().contains("100.000% End(a)          conf.:   0.000;  int'f:   0.000;"));
	}
	
	@Test
	void testPrintBagAsMachineReadable() {
		ConstraintsPrinter constPrinter = new ConstraintsPrinter(defaultProcessModel);
		Assert.assertEquals("Machine-readable results: \r\n"
				+ "Legend: 1;2;3;4\r\n"
				+ "'AtMostOne(a)';;;'End(a)';;;'Init(a)';;;'Participation(a)';;\r\n"
				+ "'Support';'Confidence';'InterestF';'Support';'Confidence';'InterestF';'Support';'Confidence';'InterestF';'Support';'Confidence';'InterestF'\r\n"
				+ "Measures: 100.000000000;0.000000000;0.000000000;100.000000000;0.000000000;0.000000000;0.000000000;0.000000000;0.000000000;0.000000000;0.000000000;0.000000000",constPrinter.printBagAsMachineReadable());
	}
	
	@Test
	void testPrintConstraintsCollection() {
		ConstraintsPrinter constPrinter = new ConstraintsPrinter(defaultProcessModel);
		Assert.assertTrue(constPrinter.printUnfoldedBag().contains("100.000% AtMostOne(a)    conf.:   0.000;  int'f:   0.000;"));
		Assert.assertTrue(constPrinter.printUnfoldedBag().contains("100.000% End(a)          conf.:   0.000;  int'f:   0.000;"));
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
