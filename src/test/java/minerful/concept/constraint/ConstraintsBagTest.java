package minerful.concept.constraint;

import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.existence.AtMostOne;
import minerful.concept.constraint.existence.End;
import minerful.concept.constraint.existence.ExactlyOne;

/**
 * Tests for class ConstraintsBag
 */
class ConstraintsBagTest {

	@Test
	void testAdd_addTaskCharactersWithConstraints() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		TaskChar tCh2 = new TaskChar('b');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.add(tCh1, c1);
		constraintsBag.add(tCh1, c2);
		Assert.assertEquals(2, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(2, constraintsBag.getConstraintsOf(tCh1).size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh2).size());
	}

	@Test
	void testAdd_addTaskCharacterSet() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		TaskChar tCh2 = new TaskChar('b');
		TaskCharSet tChSet = new TaskCharSet(Arrays.asList(tCh1, tCh2));
		Constraint c1 = new AtMostOne(tCh1);
		constraintsBag.add(tChSet, c1);
		Assert.assertEquals(1, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(1, constraintsBag.getConstraintsOf(tCh1).size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh2).size());
	}

	@Test
	void testAdd_addTaskCharacter() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		constraintsBag.add(tCh1);
		Assert.assertEquals(0, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh1).size());
		Assert.assertEquals(1, constraintsBag.getTaskChars().size());
	}

	@Test
	void testAddAll() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(2, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(2, constraintsBag.getConstraintsOf(tCh1).size());
	}

	@Test
	void testRemove_constraint_from_taskChar() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		constraintsBag.remove(tCh1, c1);
		Assert.assertEquals(1, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(1, constraintsBag.getConstraintsOf(tCh1).size());
	}

	@Test
	void testRemove_constraint() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		constraintsBag.remove(c1);
		Assert.assertEquals(1, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(1, constraintsBag.getConstraintsOf(tCh1).size());
	}

	@Test
	void testEraseConstraintsOf() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		constraintsBag.eraseConstraintsOf(tCh1);
		Assert.assertEquals(0, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh1).size());
	}

	@Test
	void testWipeOutConstraints() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(2, constraintsBag.wipeOutConstraints());
		Assert.assertEquals(0, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh1).size());
	}

	@Test
	void testGet() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		Constraint c3 = new ExactlyOne(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));

		Assert.assertEquals(c1, constraintsBag.get(tCh1, c1));
		Assert.assertEquals(c2, constraintsBag.get(tCh1, c2));
		Assert.assertNull(constraintsBag.get(tCh1, c3));
	}

	@Test
	void testGetOrAdd() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1));

		Assert.assertEquals(c1, constraintsBag.getOrAdd(tCh1, c1));
		Assert.assertEquals(c2, constraintsBag.getOrAdd(tCh1, c2));
	}

	@Test
	void testToString() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1));
		Assert.assertEquals("ConstraintsBag [bag={a=[AtMostOne(a)]}, taskChars=[a]]", constraintsBag.toString());
	}

	@Test
	void testClone() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1));
		ConstraintsBag clonedConstraintsBag = (ConstraintsBag) constraintsBag.clone();
		Assert.assertEquals(c1, clonedConstraintsBag.get(tCh1, c1));
	}

	@Test
	void testCreateEmptyIndexedCopy() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1));
		ConstraintsBag clonedConstraintsBag = (ConstraintsBag) constraintsBag.createEmptyIndexedCopy();
		Assert.assertEquals(1, clonedConstraintsBag.getTaskChars().size());
		Assert.assertEquals(0, clonedConstraintsBag.getAllConstraints().size());
	}

	@Test
	void testCreateComplementOfCopyPrunedByThreshold() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1, 0.5);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		ConstraintsBag clonedConstraintsBag = (ConstraintsBag) constraintsBag
				.createComplementOfCopyPrunedByThreshold(1.0);
		Assert.assertEquals(1, clonedConstraintsBag.getTaskChars().size());
		Assert.assertEquals(1, clonedConstraintsBag.getAllConstraints().size());
	}

	@Test
	void testHowMany() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1, 0.5);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(2, constraintsBag.howManyConstraints());
		Assert.assertEquals(2, constraintsBag.howManyUnmarkedConstraints());
		Assert.assertEquals(Long.valueOf(2), constraintsBag.howManyExistenceConstraints());
	}

	@Test
	void testSetAlphabet() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		constraintsBag.setAlphabet(Arrays.asList(tCh1));
		Assert.assertEquals(1, constraintsBag.getTaskChars().size());
	}

	@Test
	void testGetOnlyFullySupportedConstraints() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1, 0.5);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(1, constraintsBag.getOnlyFullySupportedConstraints().size());
	}

	@Test
	void testGetOnlyFullySupportedConstraintsInNewBag() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1, 0.5);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(1, constraintsBag.getOnlyFullySupportedConstraintsInNewBag().getAllConstraints().size());
	}

	@Test
	void testRemoveMarkedConstraints() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		Constraint c1 = new AtMostOne(tCh1, 0.5);
		c1.setRedundant(true);
		Constraint c2 = new End(tCh1);
		constraintsBag.addAll(tCh1, Arrays.asList(c1, c2));
		Assert.assertEquals(1, constraintsBag.removeMarkedConstraints());
	}
	
	@Test
	void testSlice() {
		ConstraintsBag constraintsBag = new ConstraintsBag();
		TaskChar tCh1 = new TaskChar('a');
		TaskChar tCh2 = new TaskChar('b');
		Constraint c1 = new AtMostOne(tCh1);
		Constraint c2 = new End(tCh1);
		constraintsBag.add(tCh1, c1);
		constraintsBag.add(tCh1, c2);
		Assert.assertEquals(2, constraintsBag.getAllConstraints().size());
		Assert.assertEquals(2, constraintsBag.getConstraintsOf(tCh1).size());
		Assert.assertEquals(0, constraintsBag.getConstraintsOf(tCh2).size());
		
		ConstraintsBag slicedConstraintBag = constraintsBag.slice(new TreeSet<TaskChar> (Arrays.asList(tCh1)));
		Assert.assertEquals(1, slicedConstraintBag.getTaskChars().size());
		Assert.assertEquals(2, slicedConstraintBag.getConstraintsOf(tCh1).size());

	}

}
