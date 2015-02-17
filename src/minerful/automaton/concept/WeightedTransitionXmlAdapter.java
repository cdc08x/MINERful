package minerful.automaton.concept;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import dk.brics.automaton.Transition;

public class WeightedTransitionXmlAdapter extends XmlAdapter<WeightedTransition, Transition> {

	@Override
	public Transition unmarshal(WeightedTransition v) throws Exception {
		return v;
	}

	@Override
	public WeightedTransition marshal(Transition v) throws Exception {
		return (WeightedTransition) v;
	}

}
