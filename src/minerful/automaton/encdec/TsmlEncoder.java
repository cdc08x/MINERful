package minerful.automaton.encdec;

import java.util.HashMap;
import java.util.NavigableMap;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class TsmlEncoder {
	public final static String DEFAULT_WEIGHT = "1";
	protected NavigableMap<Character, String> transMap;
	
	public TsmlEncoder(NavigableMap<Character, String> transMap) {
		this.transMap = transMap;
	}

	/**
	 * 
	 * Creates a TSML-document based on an instance of the class {@link Automaton}. 
	 * 
	 * @param a Automaton to be used.
	 * @param automatonSource The Automaton used.
	 * @return TSML document.
	 */
	public String automatonToTSML(Automaton a, String automatonSource) {

		Set<State> stateSet = a.getStates();
		HashMap<State, Set<Transition>> transitionSet = new HashMap<State, Set<Transition>>();
		StringBuilder tsmlBuilder = new StringBuilder();

		tsmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		tsmlBuilder.append('\n');
		tsmlBuilder.append("<tsml label=\"Converted from " + automatonSource + "\" layout=\"false\">");
		tsmlBuilder.append('\n');

		State initialState = a.getInitialState();
		for (State state : stateSet) {
			tsmlBuilder.append("<state weight=\"");
			tsmlBuilder.append(DEFAULT_WEIGHT); // TODO: WEIGHT of a transition!
			tsmlBuilder.append("\" ");
			tsmlBuilder.append("id=\"state" + state.hashCode() + "\" ");
			if (initialState.equals(state))
				tsmlBuilder.append(" start=\"true\"");
			if (state.isAccept())
				tsmlBuilder.append(" accept=\"true\"");
			tsmlBuilder.append(">\n<name><text>" + state.hashCode());
			tsmlBuilder.append("</text></name>");
			tsmlBuilder.append("</state>");
			tsmlBuilder.append('\n');
			transitionSet.put(state, state.getTransitions());
		}

		for (State source : transitionSet.keySet()) {
			for (Transition t : transitionSet.get(source)) {
				for (Character c = t.getMin(); c <= t.getMax(); c++) {
					tsmlBuilder.append("<transition weight=\"");
					tsmlBuilder.append(DEFAULT_WEIGHT); // TODO: WEIGHT of a transition!
					tsmlBuilder.append("\" ");
//					tsmlBuilder.append("id=\"transition_" + source.hashCode() + "_" + c.hashCode() + "_" + t.getDest().hashCode() + "\" ");
					tsmlBuilder.append("id=\"" + this.transMap.get(c) + "\" ");
					tsmlBuilder.append("source=\"state" + source.hashCode() + "\" ");
					tsmlBuilder.append("target=\"state" + t.getDest().hashCode() + "\" >");
					tsmlBuilder.append("<name><text>");
					tsmlBuilder.append(this.transMap.get(c));
					tsmlBuilder.append("</text></name>");
					tsmlBuilder.append("</transition>");
					tsmlBuilder.append('\n');
				}
			}
		}

		tsmlBuilder.append("</tsml>");
		
		return tsmlBuilder.toString();
	}
}