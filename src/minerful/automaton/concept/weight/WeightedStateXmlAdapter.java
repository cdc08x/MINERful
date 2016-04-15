package minerful.automaton.concept.weight;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import dk.brics.automaton.State;

public class WeightedStateXmlAdapter extends XmlAdapter<WeightedState, State> {

	@Override
	public State unmarshal(WeightedState v) throws Exception {
		return v;
	}

	@Override
	public WeightedState marshal(State v) throws Exception {
		return (WeightedState) v;
	}

}
