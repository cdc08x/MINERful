package minerful.automaton.encdec;

import java.util.Map;
import java.util.TreeMap;

import dk.brics.automaton.State;

public class StateTransitionCountersMap {
	private Map<State, StateTransitionCounter> stateTransCountMap = new TreeMap<State, StateTransitionCounter>();
	
	public void incrementStateCrossingsCounter(State state) {
		this.addStateToMapIfNeeded(state);
		stateTransCountMap.get(state).incrementCrossingsCounter();
	}
	public void incrementStateCrossingsCounter(State state, int by) {
		this.addStateToMapIfNeeded(state);
		stateTransCountMap.get(state).incrementCrossingsCounter(by);
	}
	
	public void incrementTransitionsCounter(State tailState, Character to, int by) {
		this.addStateToMapIfNeeded(tailState);
		stateTransCountMap.get(tailState).incrementTransitionsCounter(to, by);
	}
	public void incrementTransitionsCounter(State tailState, Character to) {
		this.addStateToMapIfNeeded(tailState);
		stateTransCountMap.get(tailState).incrementTransitionsCounter(to);
	}

	private void addStateToMapIfNeeded(State tailState) {
		if (!this.stateTransCountMap.containsKey(tailState)) {
			this.stateTransCountMap.put(tailState, new StateTransitionCounter(tailState));
		}
	}

	public Map<State, StateTransitionCounter> getStateTransCountMap() {
		return stateTransCountMap;
	}
}