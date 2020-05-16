package minerful.automaton.concept.relevance;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import minerful.concept.AbstractTaskClass;
import minerful.utils.MessagePrinter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class VacuityAwareAutomaton extends Automaton {
	public static MessagePrinter logger = MessagePrinter.getInstance(VacuityAwareAutomaton.class);

	private static final long serialVersionUID = 7002546525205169853L;
	private String name;
	protected Set<Character> alphabet;
	protected Map<Character, AbstractTaskClass> translationMap;

	protected VacuityAwareAutomaton() {
		super();
		this.translationMap = new TreeMap<Character, AbstractTaskClass>();
	}

	protected VacuityAwareAutomaton(Map<Character, AbstractTaskClass> translationMap) {
		super();
		this.translationMap = translationMap;
	}

	public VacuityAwareAutomaton(String name, Automaton automaton, Map<Character, AbstractTaskClass> translationMap) {
		this(translationMap);
		this.name = name;
		this.postConstructionInit(automaton);
	}
	
	protected void postConstructionInit(Automaton automaton) {
//		logger.debug(String.format("Building %s", this.name));

		this.alphabet = new TreeSet<Character>();
		
		automaton.minimize();

		NavigableMap<State, ActivationStatusAwareState> statesTranslationMap = new TreeMap<State, ActivationStatusAwareState>();
		NavigableSet<State> visitedStates = new TreeSet<State>();

		ActivationStatusAwareState initWState = makeNewState();
		State initState = automaton.getInitialState();
		this.setInitialState(initWState);

		statesTranslationMap.put(initState, initWState);

		visitTransitions(statesTranslationMap, visitedStates, initState);
	}

	@Override
	@XmlElementWrapper(name = "states")
	@XmlElement(name = "state")
	public Set<State> getStates() {
		return super.getStates();
	}

	public void visitTransitions(NavigableMap<State, ActivationStatusAwareState> statesTranslationMap, NavigableSet<State> visitedStates, State currentState) {
		if (visitedStates.contains(currentState))
			return;

		ActivationStatusAwareState
			currentStAwaState = statesTranslationMap.get(currentState),
			destStAwaState = null;
		State destinationState = null;

		this.decideActivationStatus(currentState, currentStAwaState);

		for (Transition trans : currentState.getTransitions()) {
//			logger.debug(String.format("Visiting %s", trans.toString()));
			destinationState = trans.getDest();
			
			if (!statesTranslationMap.containsKey(destinationState)) {
				destStAwaState = makeNewState();
				statesTranslationMap.put(destinationState, destStAwaState);
			} else {
				destStAwaState = statesTranslationMap.get(destinationState);
			}
			for (char evt = trans.getMin(); evt <= trans.getMax(); evt++) {
				currentStAwaState.addTransition(new RelevanceAwareTransition(evt, destStAwaState,
						translationMap.get(evt).toString()));
				this.alphabet.add(evt);
			}

			visitedStates.add(currentState);

			// Recursive call: after this call, the reachable states have all been visited and assigned an activation status  
			visitTransitions(statesTranslationMap, visitedStates, destinationState);
		}

		this.decideRelevanceOfTransitions(currentStAwaState);

		return;
	}

	protected ActivationStatusAwareState makeNewState() {
		return new ActivationStatusAwareState();
	}

	private void decideRelevanceOfTransitions(
			ActivationStatusAwareState currentStAwaState) {
		ActivationStatusAwareState destStAwaState;
		RelevanceAwareTransition relAwaTrans;
		for (Transition trans : currentStAwaState.getTransitions()) {
			relAwaTrans = (RelevanceAwareTransition) trans;
			destStAwaState = (ActivationStatusAwareState) relAwaTrans.getDest();
			// A transition is of relevance iff...
			if (
					// 1. the activation status of the destination state changes (no matter how), or
					!currentStAwaState.getStatus().equals(destStAwaState.getStatus())
					// 2. the possible actions that are allowed from this state are different than the ones in the destination state
				||	!currentStAwaState.allowsTheSameTransitionsAs(destStAwaState)
			) {
				relAwaTrans.setRelevance(TransitionRelevance.RELEVANT);
			} else {
				relAwaTrans.setRelevance(TransitionRelevance.IRRELEVANT);
			}
		}
	}

	private void decideActivationStatus(State currentState, ActivationStatusAwareState currentStAwaState) {
		boolean loop = true;
		int outgoingAllowedTransitions = 0;

		// A permanent satisfaction is possible in trimmed MINIMISED automata iff
		// 1. the state is accepting...
		if (currentState.isAccept()) {
			currentStAwaState.setAccept(true);
			
			Iterator<Transition> transIt = currentState.getTransitions().iterator();
			Transition nextTrans = null;

			// 2. the state is looping...
			while (loop & transIt.hasNext()) {
				nextTrans = transIt.next();
				loop = loop && nextTrans.getDest().equals(currentState);
				outgoingAllowedTransitions += nextTrans.getMax() - nextTrans.getMin() + 1;
			}
			// 3. all transitions are loops
			if (loop && outgoingAllowedTransitions == this.translationMap.size()) {
				currentStAwaState.setStatus(StateActivationStatus.SAT_PERM);
			} else {
				// In the worst case, an accepting state is at least a temp-satisfied one
				currentStAwaState.setStatus(StateActivationStatus.SAT_TEMP);
			}
		} else {
			// A state is of permanent violation when it is a not accepting sink
			// Watch out: empty automata are clearly composed only of a permanently violating state
			if (currentState.getTransitions().size() == 0) {
				currentStAwaState.setStatus(StateActivationStatus.VIO_PERM);
			// An intermediate non-accepting state is a temporary violation
			} else {
				currentStAwaState.setStatus(StateActivationStatus.VIO_TEMP);
			}
		}
//		logger.debug(String.format("Current state is %2$s (%1$s)", currentState.toString(), currentStAwaState.getStatus()));
	}
	

	@Override
	public State getInitialState() {
		return super.getInitialState();
	}

	@Override
	public void setInitialState(State s) {
		super.setInitialState(s);
	}
	
	@Override
	@XmlAttribute
	public boolean isDeterministic() {
		return super.isDeterministic();
	}

	@Override
	public void setDeterministic(boolean deterministic) {
		super.setDeterministic(deterministic);
	}

	public Set<Character> getAlphabet() {
		return alphabet;
	}
}