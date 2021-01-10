package minerful.automaton.utils;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/**
 * Tests for class AutomatonUtils
 */
 class AutomatonUtilsTest {
	
	@Test
	 void testAccepts_sequenceNotAccepted() {
		Automaton automaton = createTestAutomaton();
		Assertions.assertFalse(AutomatonUtils.accepts(automaton, "c"));
	}
	
	@Test
	 void testAccepts_sequenceAccepted() {
		Automaton automaton = createTestAutomaton();
		Assertions.assertTrue(AutomatonUtils.accepts(automaton, "a"));
	}
	
	@Test
	 void testCreateRegExpLimitingRunLength_minimumAndMaximumSet() {
		Assertions.assertEquals(".{2,10}",AutomatonUtils.createRegExpLimitingRunLength(2, 10));
	}
	
	@Test
	 void testCreateRegExpLimitingRunLength_onlyMaximumSet() {
		Assertions.assertEquals(".{0,2}",AutomatonUtils.createRegExpLimitingRunLength(0, 2));
	}
	
	@Test
	 void testCreateRegExpLimitingRunLength_throwAssertionsionError() {
		Assertions.assertThrows(AssertionError.class, () -> AutomatonUtils.createRegExpLimitingRunLength(2, 0));
	}
	
	@Test
	 void testCreateRegExpLimitingTheAlphabet_withoutWildcard() {
		Assertions.assertEquals("[abcd]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c','d'), false));
	}
	
	@Test
	 void testCreateRegExpLimitingTheAlphabet_withWildcard() {
		Assertions.assertEquals("[abcd_]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c','d'), true));
	}
	
	@Test
	 void testCreateRegExpLimitingTheAlphabet_emptyAlphabet() {
		Assertions.assertEquals("", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList(), true));
	}
	
	@Test
	 void testCreateRegExpLimitingTheAlphabet_onlyAlphabet() {
		Assertions.assertEquals("[abc]*", AutomatonUtils.createRegExpLimitingTheAlphabet(Arrays.asList('a', 'b', 'c')));
	}
	
	@Test
	 void testGetAllPossibleSteps() {
		Automaton automaton = createTestAutomaton();
		State initState = automaton.getInitialState();
		
		Assertions.assertEquals(Arrays.asList('a'), AutomatonUtils.getAllPossibleSteps(initState));
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
