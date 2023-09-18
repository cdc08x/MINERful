package minerful.automaton.encdec;

import java.util.Map;
import java.util.TreeMap;

import dk.brics.automaton.State;

public class StateTransitionCounter implements Comparable<StateTransitionCounter> {
	public static final int DEFAULT_INCREMENT = 1;
	public final State tailState;
	private Map<Character, Integer> transitionCounterMap;
	private int howManyCrossings = 0;

	public StateTransitionCounter(State tailState) {
		this.tailState = tailState;
		this.transitionCounterMap = new TreeMap<Character, Integer>();
	}
	
	public void incrementTransitionsCounter(Character to) {
		this.incrementTransitionsCounter(to, DEFAULT_INCREMENT);
	}
	
	public void incrementTransitionsCounter(Character to, int by) {
		int howMuch = by;
		if (this.transitionCounterMap.containsKey(to)) {
			howMuch += this.transitionCounterMap.get(to);
		}
		this.transitionCounterMap.put(to, howMuch);
	}
	
	public void incrementCrossingsCounter() {
		this.incrementCrossingsCounter(DEFAULT_INCREMENT);
	}
	public void incrementCrossingsCounter(int by) {
		this.howManyCrossings += by;
	}

	public Map<Character, Integer> getTransitionCounterMap() {
		return transitionCounterMap;
	}
	public int getHowManyCrossings() {
		return howManyCrossings;
	}

	@Override
	public int compareTo(StateTransitionCounter o) {
		return this.tailState.compareTo(o.tailState);
	}
}