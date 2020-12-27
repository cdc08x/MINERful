package minerful.automaton.utils;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * Tests for class AutomatonUtils
 */
public class AutomatonUtilsTest {
	
	@Test
	public void testAccepts_sequenceNotAccepted() {
		Automaton automaton = createTestAutomaton();
		Assert.assertFalse(AutomatonUtils.accepts(automaton, "c"));
	}
	
	@Test
	public void testAccepts_sequenceAccepted() {
		Automaton automaton = createTestAutomaton();
		Assert.assertTrue(AutomatonUtils.accepts(automaton, "a"));
	}
	
	@Test
	public void testCreateRegExpLimitingRunLength_minimumAndMaximumSet() {
		Assert.assertEquals(".{2,10}",AutomatonUtils.createRegExpLimitingRunLength(2, 10));
	}
	
	@Test
	public void testCreateRegExpLimitingRunLength_onlyMaximumSet() {
		Assert.assertEquals(".{0,2}",AutomatonUtils.createRegExpLimitingRunLength(0, 2));
	}
	
	@Test
	public void testCreateRegExpLimitingRunLength_throwAssertionError() {
		Assert.assertThrows(AssertionError.class, () -> AutomatonUtils.createRegExpLimitingRunLength(2, 0));
	}
	
	@Test
	public void testCreateRegExpLimitingTheAlphabet_withoutWildcard() {
		Assert.assertEquals("[abcd]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c','d'), false));
	}
	
	@Test
	public void testCreateRegExpLimitingTheAlphabet_withWildcard() {
		Assert.assertEquals("[abcd_]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c','d'), true));
	}
	
	@Test
	public void testCreateRegExpLimitingTheAlphabet_emptyAlphabet() {
		Assert.assertEquals("", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList(), true));
	}
	
	@Test
	public void testCreateRegExpLimitingTheAlphabet_onlyAlphabet() {
		Assert.assertEquals("[abc]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c')));
	}
	
	@Test
	public void testGetAllPossibleSteps() {
		Automaton automaton = createTestAutomaton();
		State initState = automaton.getInitialState();
		
		Assert.assertEquals(Arrays.asList('a'), AutomatonUtils.getAllPossibleSteps(initState));
	}
	
	/**
	 * Creates an example automaton which is used for testing
	 * 
	 * @return example automaton
	 */
	private Automaton createTestAutomaton() {
		Automaton automaton = new Automaton();
		State initState = new State();
		State secondState = new State();
		State thirdState = new State();
		secondState.setAccept(true);
		thirdState.setAccept(true);
		initState.addTransition(new Transition('a', secondState));
		secondState.addTransition(new Transition('b', thirdState));
		automaton.setInitialState(initState);
		return automaton;
	}
	

}
