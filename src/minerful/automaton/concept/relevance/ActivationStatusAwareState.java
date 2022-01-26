package minerful.automaton.concept.relevance;

import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import minerful.utils.RandomCharGenerator;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

@XmlAccessorType(XmlAccessType.NONE)
public class ActivationStatusAwareState extends State {
	private static final long serialVersionUID = 169203888647636487L;

	protected StateActivationStatus status = StateActivationStatus.SAT_TEMP;

	protected String stateUUID = "s" + RandomCharGenerator.generateChar(6);
	
	protected NavigableMap<Character, Transition> transitionMap = new TreeMap<Character, Transition>();
	
	@Override
	@XmlElementWrapper(name="transitions")
	@XmlElement(name="transition")
	public Set<Transition> getTransitions() {
		return super.getTransitions();
	}
	

	@XmlAttribute(name="id")
	public String getStateUUID() {
		return stateUUID;
	}

	public void setStateUUID(String stateUUID) {
		this.stateUUID = stateUUID;
	}
	
	public Set<Character> getAllowedTransitionChars() {
		return this.transitionMap.keySet();
	}
	
	@Override
	public void setAccept(boolean accept) {
		super.setAccept(accept);
	}
	
	@Override
	@XmlAttribute
	public boolean isAccept() {
		return super.isAccept();
	}
	
	@Override
	public void addTransition(Transition transition) {
		for (char fire = transition.getMin(); fire <= transition.getMax(); fire++)
			this.transitionMap.put(fire, transition);

		super.addTransition(transition);
	}
	
	public boolean allowsTheSameTransitionsAs(ActivationStatusAwareState state) {
		return this.transitionMap.keySet().equals(state.transitionMap.keySet());
	}

	public StateActivationStatus getStatus() {
		return status;
	}
	public void setStatus(StateActivationStatus status) {
		this.status = status;
	}

	public RelevanceAwareTransition getTransition(Character arg0) {
		if (this.transitionMap.containsKey(arg0))
			return (RelevanceAwareTransition) this.transitionMap.get(arg0);
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append("State");
		sBuil.append(" ID: ");
		sBuil.append(this.getStateUUID());
		sBuil.append(" Status: ");
		sBuil.append(this.getStatus());
		sBuil.append(" Accept: ");
		sBuil.append(this.isAccept());
		sBuil.append(" ");
		sBuil.append("\n  Transitions:\n");
		for (Transition trans : this.getTransitions()) {
			sBuil.append("    Transition ");
			sBuil.append(trans.getMin() + " - " + trans.getMax());
			sBuil.append(" to ");
			sBuil.append(((ActivationStatusAwareState)trans.getDest()).getStateUUID());
			sBuil.append(" ");
			sBuil.append(" Relevant: ");
			sBuil.append(((RelevanceAwareTransition) trans).getRelevance());
			sBuil.append("\n");
		}
		return sBuil.toString();
	}
}