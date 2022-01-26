package minerful.automaton.concept.weight;

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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.AbstractTaskClass;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
//@XmlJavaTypeAdapter(WeightedAutomatonXmlAdapter.class)
public class WeightedAutomaton extends Automaton {

	private static final long serialVersionUID = 7002546525205169853L;
	private NavigableMap<Character, AbstractTaskClass> translationMap;

	@SuppressWarnings("unused")
	private WeightedAutomaton() {
		super();
	}

	public WeightedAutomaton(Automaton automaton) {
		this(automaton, null);
	}

	public WeightedAutomaton(Automaton automaton,
			NavigableMap<Character, AbstractTaskClass> translationMap) {
		this.translationMap = translationMap;
		
		NavigableMap<State, WeightedState> statesTranslationMap = new TreeMap<State, WeightedState>();
		NavigableSet<State> visitedStates = new TreeSet<State>();

		WeightedState initWState = new WeightedState();
		State initState = automaton.getInitialState();
		this.setInitialState(initWState);

		statesTranslationMap.put(initState, initWState);

		unfoldTransitions(statesTranslationMap, visitedStates, initState);
	}

	@Override
	@XmlElementWrapper(name = "states")
	@XmlElement(name = "state")
	@XmlJavaTypeAdapter(WeightedStateXmlAdapter.class)
	public Set<State> getStates() {
		return super.getStates();
	}

	public void unfoldTransitions(
			NavigableMap<State, WeightedState> statesTranslationMap, NavigableSet<State> visitedStates, State currentState) {
		if (visitedStates.contains(currentState))
			return;

		WeightedState currentWState = statesTranslationMap.get(currentState), destinationWState = null;
		State destinationState = null;

		if (currentState.isAccept())
			currentWState.setAccept(true);

		for (Transition trans : currentState.getTransitions()) {
			destinationState = trans.getDest();
			if (!statesTranslationMap.containsKey(destinationState)) {
				destinationWState = new WeightedState();
				statesTranslationMap.put(destinationState, destinationWState);
			} else {
				destinationWState = statesTranslationMap.get(destinationState);
			}
			for (char evt = trans.getMin(); evt <= trans.getMax(); evt++) {
				currentWState.addTransition(new WeightedTransition(evt,
						destinationWState, translationMap.get(evt).toString()));
			}

			visitedStates.add(currentState);
			unfoldTransitions(statesTranslationMap, visitedStates,
					destinationState);
		}
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
		// TODO Auto-generated method stub
		return super.isDeterministic();
	}

	@Override
	public void setDeterministic(boolean deterministic) {
		// TODO Auto-generated method stub
		super.setDeterministic(deterministic);
	}
}