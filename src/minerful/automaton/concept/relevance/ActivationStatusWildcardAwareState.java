package minerful.automaton.concept.relevance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import dk.brics.automaton.State;

@XmlAccessorType(XmlAccessType.NONE)
public class ActivationStatusWildcardAwareState extends ActivationStatusAwareState {
	private static final long serialVersionUID = 7185379079900528036L;

	public State stepWild() {
		return super.step(VacuityAwareWildcardAutomaton.getWildCardChar());
	}

	public RelevanceAwareTransition getWildTransition() {
		if (this.transitionMap.containsKey(VacuityAwareWildcardAutomaton.getWildCardChar()))
			return (RelevanceAwareTransition) this.transitionMap.get(VacuityAwareWildcardAutomaton.getWildCardChar());
		return null;
	}
}