package minerful.automaton.utils;

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
}