package minerful.automaton.concept.weight;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import dk.brics.automaton.Automaton;

public class WeightedAutomatonXmlAdapter extends
		XmlAdapter<WeightedAutomaton, Automaton> {

	@Override
	public Automaton unmarshal(WeightedAutomaton v) throws Exception {
		return v;
	}

	@Override
	public WeightedAutomaton marshal(Automaton v) throws Exception {
		return (WeightedAutomaton) v;
	}

}
