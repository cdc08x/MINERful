package minerful.automaton;

import dk.brics.automaton.Automaton;

public class SubAutomaton {
	public final Character basingCharacter;
	public final Automaton automaton;

	public SubAutomaton(Character character, Automaton automaton) {
		this.basingCharacter = character;
		this.automaton = automaton;
	}
}