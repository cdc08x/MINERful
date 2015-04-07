package minerful.automaton.encdec;

import java.util.Iterator;
import java.util.NavigableMap;

import minerful.automaton.AutomatonFactory;
import minerful.automaton.concept.WeightedAutomaton;
import minerful.automaton.concept.WeightedAutomatonStats;
import minerful.automaton.concept.WeightedState;
import minerful.automaton.concept.WeightedTransition;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.TaskClass;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

public class WeightedAutomatonFactory {
	private static Logger logger = Logger.getLogger(AutomatonFactory.class
			.getCanonicalName());
	private NavigableMap<Character, TaskClass> translationMap;

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
	
	public WeightedAutomatonFactory(NavigableMap<Character, TaskClass> navigableMap) {
		this.translationMap = navigableMap;
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, LogParser logParser) {
		return this.augmentByReplay(automaton, logParser, false);
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, LogParser logParser, boolean ignoreIfNotCompliant) {
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
		int i = 0;
		Character auxEvtIdentifier = null;
		LogTraceParser auXTraPar = null;
		Iterator<LogTraceParser> traceParsersIterator = logParser.traceIterator();
		
		while (traceParsersIterator.hasNext()) {
			auXTraPar = traceParsersIterator.next();
			auXTraPar.init();
			
			if (AutomatonUtils.accepts(automaton, trace)) {
				nextState = initState;
				nextState.increaseWeight(); // This is the initial state: at every start of a trace, a +1 is added to the visit counter

				logger.trace("Replaying legal trace #" + (i++) + "/" + logParser.length());
				
				while(!auXTraPar.isParsingOver()) {
					auxEvtIdentifier = auXTraPar.parseSubsequentAndEncode();
					currentState = nextState;
					
					nextState = currentState.stepAndIncreaseTransitionWeight(auxEvtIdentifier);
					nextState.increaseWeight();
				}
				
			} else if (!ignoreIfNotCompliant) {
				soFar = new StringBuilder();
				illegalEventReached = false;
				nextState = initState;
				nextState.increaseNonConformityWeight(); // This is the initial state: at every start of a trace, a +1 is added to the visit counter
				
				logger.trace("Replaying trace #" + (i++) + "/" + logParser.length());
				
				while(!auXTraPar.isParsingOver() && !illegalEventReached) {
					auxEvtIdentifier = auXTraPar.parseSubsequentAndEncode();
					currentState = nextState;
					nextState = currentState.stepAndIncreaseTransitionsNonConformityWeight(auxEvtIdentifier);
					if (nextState != null) {
						nextState.increaseNonConformityWeight();
					} else {
						illegalEventReached = true;
						// Create a new illegal transition to a new illegal state
						// New illegal state
						WeightedState illegalState = new WeightedState();
						illegalState.setIllegal(true);
						// New illegal transition
						WeightedTransition illegalTransition = new WeightedTransition(auxEvtIdentifier, illegalState, this.translationMap.get(auxEvtIdentifier).getName());
						illegalTransition.setIllegal(true);
						// Connect the transition to the current state
						currentState.addTransition(illegalTransition);
						// Increase the non-conformity weight
						illegalState.increaseNonConformityWeight();
						illegalTransition.increaseNonConformityWeight();
					}
					// TODO Record somewhere and somewhat the illegal trace + the state where it took place the last legal action!
					soFar.append(this.translationMap.get(auxEvtIdentifier));
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