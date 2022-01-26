package minerful.automaton;

import java.util.ArrayList;
import java.util.Collection;

import minerful.automaton.utils.AutomatonUtils;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

public class AutomatonRandomWalker {
	private Automaton automaton;
	private State currentState;
	private ArrayList<Character> enabledTransitions = new ArrayList<Character>();

	public AutomatonRandomWalker(Automaton automaton) {
		this.automaton = automaton;
		this.goToStart();
	}
	
	public void goToStart() {
		this.goToState(this.automaton.getInitialState());
	}
	
	private void goToState(State state) {
		this.currentState = state;
		this.enabledTransitions = AutomatonUtils.getAllPossibleSteps(this.currentState);
		
	}
	
	public Character walkOn() {
		Character pickedTransitionChar = null;
		int pickedTransitionNumber = -1;
		if (!this.currentState.isAccept() || decideToContinueTheWalk()) {
			if (this.enabledTransitions.size() > 0) {
				pickedTransitionNumber = pickTransitionToWalkThrough(this.enabledTransitions);
				pickedTransitionChar = this.enabledTransitions.get(pickedTransitionNumber);
				this.goToState(this.currentState.step(pickedTransitionChar));
			}
		}
		return pickedTransitionChar;
	}

	private int pickTransitionToWalkThrough(Collection<Character> enabledTransitions) {
		return (int) (Math.floor(Math.random() * enabledTransitions.size()));
	}

	private boolean decideToContinueTheWalk() {
		return Math.random() > 0.5;
	}

}