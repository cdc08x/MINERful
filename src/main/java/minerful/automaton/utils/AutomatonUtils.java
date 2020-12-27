package minerful.automaton.utils;

import java.util.ArrayList;
import java.util.Collection;

import minerful.io.encdec.TaskCharEncoderDecoder;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class AutomatonUtils {

	/**
	 * Checks if the provided sequence of characters/tasks is accepted by the
	 * automaton.
	 * 
	 * @param automaton    that the provided sequence is checked against
	 * @param taskSequence that is checked for acceptance
	 * @return true when accepted, otherwise false
	 */
	public static final boolean accepts(Automaton automaton, String taskSequence) {
		State state = automaton.getInitialState();
		for (char step : taskSequence.toCharArray()) {
			state = state.step(step);
			// state is null if transition does not exist
			if (state == null)
				return false;
		}
		return state.isAccept();
	}

	/**
	 * Creates regular expression that ensures that the length is between minLength
	 * and maxLength.
	 * 
	 * @param minLength minimum length of characters, default set to 0
	 * @param maxLength maximum length of characters, default is no limitation
	 * @return regular expression as {@ String}
	 */
	public static String createRegExpLimitingRunLength(int minLength, int maxLength) {
		assert minLength >= 0;
		assert maxLength >= minLength;
		StringBuilder sBuil = new StringBuilder();

		if (minLength > 0 || maxLength > 0) {
			sBuil.append(".{");
			sBuil.append(minLength > 0 ? minLength : 0);
			sBuil.append(",");
			sBuil.append(maxLength > 0 ? maxLength : "");
			sBuil.append("}");
		}

		return sBuil.toString();
	}

	/**
	 * 
	 * @param alphabet
	 * @return
	 */
	public static String createRegExpLimitingTheAlphabet(Collection<Character> alphabet) {
		return createRegExpLimitingTheAlphabet(alphabet, false);
	}

	/**
	 * Creates a regular expression that limits the allowed alphabet. If set to
	 * true, the flag withWildcard adds an underscore to the regular expression.
	 * 
	 * @param alphabet as a collection of characters
	 * @param withWildcard flag which adds underscore if set to true
	 * @return regular expression with allowed alphabet
	 */
	public static String createRegExpLimitingTheAlphabet(Collection<Character> alphabet, boolean withWildcard) {
		if (alphabet.size() < 1)
			return "";
		// limiting the alphabet
		String regexpLimitingTheAlphabet = "";
		for (Character c : alphabet) {
			regexpLimitingTheAlphabet += c;
		}
		if (withWildcard)
			regexpLimitingTheAlphabet += TaskCharEncoderDecoder.WILDCARD_CHAR;
		return "[" + regexpLimitingTheAlphabet + "]*";
	}

	/**
	 * Adjust provided automaton by limiting its run length.
	 * 
	 * @param automaton which run length should be limited
	 * @param minLength minimum run length
	 * @param maxLength maximum run length
	 * @return adjusted automaton
	 */
	public static Automaton limitRunLength(Automaton automaton, int minLength, int maxLength) {
		RegExp regExpLimitingRunLength = new RegExp(createRegExpLimitingRunLength(minLength, maxLength));
		return automaton.intersection(regExpLimitingRunLength.toAutomaton());
	}

	/**
	 * Extracts a list of possible steps
	 * 
	 * @param state that is checked for possible steps
	 * @return List of possible steps
	 */
	public static ArrayList<Character> getAllPossibleSteps(State state) {
		Collection<Transition> transitions = state.getTransitions();
		ArrayList<Character> enabledTransitions = new ArrayList<Character>();
		for (Transition transition : transitions) {
			for (char c = transition.getMin(); c <= transition.getMax(); c++) {
				enabledTransitions.add(c);
			}
		}
		return enabledTransitions;
	}

}