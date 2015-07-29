package minerful.automaton.utils;

import java.util.Collection;

import minerful.io.encdec.TaskCharEncoderDecoder;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

public class AutomatonUtils {
	public static final boolean accepts(Automaton automaton, String string) {
		State state = automaton.getInitialState();
		for (char step : string.toCharArray()) {
			state = state.step(step);
			if (state == null)
				return false;
		}
		return state.isAccept();
	}
	
	
    public static String createRegExpLimitingTraceLength (int minLen, int maxLen) {
    	assert minLen >= 0;
    	assert maxLen >= minLen;
    	StringBuilder sBuil = new StringBuilder();

    	if (minLen > 0 || maxLen > 0) {
    		sBuil.append(".{");
    		sBuil.append(minLen > 0 ? minLen : 0);
    		sBuil.append(",");
    		sBuil.append(maxLen > 0 ? maxLen : "");
    		sBuil.append("}");
    	}

    	return sBuil.toString();
    }

	public static String createRegExpLimitingTheAlphabet(
			Collection<Character> alphabet) {
		return createRegExpLimitingTheAlphabet(alphabet, false);
	}

	public static String createRegExpLimitingTheAlphabet(
			Collection<Character> alphabet, boolean withWildcard) {
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

}