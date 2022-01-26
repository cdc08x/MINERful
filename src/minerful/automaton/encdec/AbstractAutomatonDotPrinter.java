package minerful.automaton.encdec;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.utils.ResourceReader;
import dk.brics.automaton.State;

public abstract class AbstractAutomatonDotPrinter {

	protected static class EmphasizableLabelPojo {
		public final String label;
		public final boolean emphasized;
		public EmphasizableLabelPojo(String label, boolean emphasized) {
			this.label = label;
			this.emphasized = emphasized;
		}
	}
	
	protected static final String DOT_INIT = ResourceReader.readResource("minerful/automaton/encdec/init_for_dot.txt");
	protected static final String DOT_END = ResourceReader.readResource("minerful/automaton/encdec/end_for_dot.txt");
	protected static Properties DOT_TEMPLATES = null;

	protected NavigableMap<Character, String> transMap;
	
	public AbstractAutomatonDotPrinter(NavigableMap<Character, String> translationMap) {
		this.transMap = translationMap;
		if (DOT_TEMPLATES == null) {
			DOT_TEMPLATES = new Properties();
			try {
				DOT_TEMPLATES.load(ResourceReader.loadResource("minerful/automaton/encdec/dot_templates.properties"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected int getAlphabetSize() {
		return this.transMap.keySet().size();
	}
	
	protected Map<State, String> defineStateNodesIds(Set<State> states) {
		Map<State, String> statesIdMap = new TreeMap<State, String>();
		int i = 0;
		for (State state : states) {
			// Let us map states with unique identifiers
			statesIdMap.put(state, String.format(DOT_TEMPLATES.getProperty("stateNodeNameTemplate"), i++));
		}
		return statesIdMap;
	}

	protected Collection<Character> makeItHowNotToGetThere(Collection<Character> list) {
		Collection<Character> notThere = new TreeSet<Character>(this.transMap.keySet());
		notThere.removeAll(list);
		return notThere;
	}

}
