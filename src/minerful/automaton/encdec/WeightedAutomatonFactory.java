package minerful.automaton.encdec;

import java.util.NavigableMap;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.concept.WeightedAutomaton;
import minerful.automaton.concept.WeightedAutomatonStats;
import minerful.automaton.concept.WeightedState;
import minerful.automaton.concept.WeightedTransition;
import minerful.automaton.utils.AutomatonUtils;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

public class WeightedAutomatonFactory {
	private static Logger logger = Logger.getLogger(AutomatonFactory.class
			.getCanonicalName());
	private NavigableMap<Character, String> translationMap;

	public static class IllegalTransitionException extends IllegalStateException {
		private static final long serialVersionUID = -562295596335012451L;
		//
		public static final String MESSAGE_TEMPLATE = new String("%1$s is not allowed after %2$s");
		public final String history;
		public final Character illegalEvent;
		
		public IllegalTransitionException(String history, Character illegalEvent) {
			this.history = history;
			this.illegalEvent = illegalEvent;
		}
	}
	
	public WeightedAutomatonFactory(NavigableMap<Character, String> translationMap) {
		this.translationMap = translationMap;
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, String[] testBedArray) {
		return this.augmentByReplay(automaton, testBedArray, false);
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, String[] testBedArray, boolean ignoreIfNotCompliant) {
		if (automaton == null || automaton.isEmpty())
			return null;
		WeightedAutomaton weightedAutomaton = new WeightedAutomaton(automaton, translationMap);
		WeightedState
			initState = (WeightedState) weightedAutomaton.getInitialState(),
			currentState = null,
			nextState = null;

		StringBuilder soFar = new StringBuilder();
		String trace = null;
		
		boolean illegalEventReached = false;
		char[] charArrayTrace = null;
		Character event = null;

		for (int i = 0; i < testBedArray.length; i++) {
			trace = testBedArray[i];
			
			if (AutomatonUtils.accepts(automaton, trace)) {
				nextState = initState;
				nextState.increaseWeight(); // This is the initial state: at every start of a trace, a +1 is added to the visit counter

				logger.trace("Replaying legal trace #" + (i+1) + "/" + testBedArray.length);
				
				charArrayTrace = trace.toCharArray();
				
				for (int j = 0; j < charArrayTrace.length; j++) {
					event = charArrayTrace[j];
					currentState = nextState;
					
					nextState = currentState.stepAndIncreaseTransitionWeight(event);
					nextState.increaseWeight();
				}
			} else if (!ignoreIfNotCompliant) {
				soFar = new StringBuilder();
				illegalEventReached = false;
				nextState = initState;
				nextState.increaseNonConformityWeight(); // This is the initial state: at every start of a trace, a +1 is added to the visit counter
				
				logger.trace("Replaying trace #" + (i+1) + "/" + testBedArray.length);
				
				charArrayTrace = trace.toCharArray();
				
				for (int j = 0; j < charArrayTrace.length && !illegalEventReached; j++) {
					event = charArrayTrace[j];
					currentState = nextState;
					nextState = currentState.stepAndIncreaseTransitionsNonConformityWeight(event);
					if (nextState != null) {
						nextState.increaseNonConformityWeight();
					} else {
						illegalEventReached = true;
						// Create a new illegal transition to a new illegal state
						// New illegal state
						WeightedState illegalState = new WeightedState();
						illegalState.setIllegal(true);
						// New illegal transition
						WeightedTransition illegalTransition = new WeightedTransition(event, illegalState, this.translationMap.get(event));
						illegalTransition.setIllegal(true);
						// Connect the transition to the current state
						currentState.addTransition(illegalTransition);
						// Increase the non-conformity weight
						illegalState.increaseNonConformityWeight();
						illegalTransition.increaseNonConformityWeight();
					}
					// TODO Record somewhere and somewhat the illegal trace + the state where it took place the last legal action!
					soFar.append(this.translationMap.get(event));
					soFar.append(", ");
				}
				logger.trace("Legal trunk of replayed trace: " + soFar.substring(0, soFar.length() - 2));
			}
				
		}
		
		WeightedAutomatonStats wAutSta = new WeightedAutomatonStats(weightedAutomaton);
		wAutSta.augmentWeightedAutomatonWithQuantiles();
		if (!ignoreIfNotCompliant) {
			wAutSta.augmentWeightedAutomatonWithIllegalityQuantiles();
		}

		return weightedAutomaton;
	}
}