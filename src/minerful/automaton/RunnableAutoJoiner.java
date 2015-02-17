package minerful.automaton;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

public class RunnableAutoJoiner implements Runnable {
	private static Logger logger = Logger.getLogger(AutomatonFactory.class.getCanonicalName());

	public Automaton automaton;
	public Automaton secondAutomaton;

	public RunnableAutoJoiner(Automaton firstAutomaton, Automaton secondAutomaton) {
		this.automaton = firstAutomaton;
		this.secondAutomaton = secondAutomaton;
	}

	@Override
	public void run() {
		this.automaton = this.automaton.intersection(secondAutomaton);
		logger.trace("Automaton states: " + this.automaton.getNumberOfStates() + "; automaton transitions: " + this.automaton.getNumberOfTransitions());
	}
}