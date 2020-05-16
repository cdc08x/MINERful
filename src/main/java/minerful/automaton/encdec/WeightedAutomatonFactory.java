package minerful.automaton.encdec;

import java.util.Iterator;
import java.util.NavigableMap;

import dk.brics.automaton.Automaton;
import minerful.automaton.concept.weight.WeightedAutomaton;
import minerful.automaton.concept.weight.WeightedAutomatonStats;
import minerful.automaton.concept.weight.WeightedState;
import minerful.automaton.concept.weight.WeightedTransition;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.AbstractTaskClass;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

public class WeightedAutomatonFactory {
	private static MessagePrinter logger = MessagePrinter.getInstance(WeightedAutomatonFactory.class);
	private NavigableMap<Character, AbstractTaskClass> translationMap;

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
	
	public WeightedAutomatonFactory(NavigableMap<Character, AbstractTaskClass> navigableMap) {
		this.translationMap = navigableMap;
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, LogParser logParser, boolean skimIt) {
		return this.augmentByReplay(automaton, logParser, skimIt, true);
	}

	public WeightedAutomaton augmentByReplay(Automaton automaton, LogParser logParser, boolean skimIt, boolean ignoreIfNotCompliant) {
		if (automaton == null || automaton.isEmpty())
			return null;
		WeightedAutomaton weightedAutomaton = new WeightedAutomaton(automaton, translationMap);

		WeightedState
			initState = (WeightedState) weightedAutomaton.getInitialState(),
			currentState = null,
			nextState = null;
		
		WeightedState
			faultPitState = new WeightedState();
		faultPitState.setIllegal(true);

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
			trace = auXTraPar.encodeTrace();
			
			if (AutomatonUtils.accepts(automaton, trace)) {
				nextState = initState;
				nextState.increaseWeight(); // This is the initial state: at every start of a trace, a +1 is added to the visit counter

				logger.trace("Replaying legal trace #{0}/{1}: {2}", i++, logParser.length(), trace);
				
				boolean illegalTransitionRequested = false;
				
				while(!auXTraPar.isParsingOver() && !illegalTransitionRequested) {
					auxEvtIdentifier = auXTraPar.parseSubsequentAndEncode();
					currentState = nextState;
					
					nextState = currentState.stepAndIncreaseTransitionWeight(auxEvtIdentifier);
					if (nextState == null) {
						illegalTransitionRequested = true;
					} else {
						nextState.increaseWeight();
					}
				}
				if (illegalTransitionRequested) {
					logger.error("Last read: " + auxEvtIdentifier + " (" + translationMap.get(auxEvtIdentifier) + ")");
					logger.error("Full trace: " + trace);
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
						// New illegal transition
						WeightedTransition illegalTransition = new WeightedTransition(auxEvtIdentifier, faultPitState, this.translationMap.get(auxEvtIdentifier).getName());
						illegalTransition.setIllegal(true);
						// Connect the transition to the current state
						currentState.addTransition(illegalTransition);
						// Increase the non-conformity weight
						faultPitState.increaseNonConformityWeight();
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
		wAutSta.augmentWeightedAutomatonWithQuantiles(skimIt);
		if (!ignoreIfNotCompliant) {
			wAutSta.augmentWeightedAutomatonWithIllegalityQuantiles();
		}

		return weightedAutomaton;
	}
}