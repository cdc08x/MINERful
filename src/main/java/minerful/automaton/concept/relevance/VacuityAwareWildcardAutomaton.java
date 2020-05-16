package minerful.automaton.concept.relevance;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.concept.AbstractTaskClass;
import minerful.logparser.StringTaskClass;
import dk.brics.automaton.Automaton;

public class VacuityAwareWildcardAutomaton extends VacuityAwareAutomaton {
	private static final long serialVersionUID = 2481171406279235021L;
	
	protected SortedSet<Character> alphabetWithoutWildcard;
	private String name;
	
	protected VacuityAwareWildcardAutomaton() {
		super();
	}
	
	protected VacuityAwareWildcardAutomaton(
			String regExp,
			Map<Character, AbstractTaskClass> translationMap) {
		super(translationMap);
		this.alphabetWithoutWildcard = new TreeSet<Character>(translationMap.keySet());

		this.translationMap.put(getWildCardChar(), getWildCardClass());
		Automaton automaton = AutomatonFactory.fromRegularExpressions(
				Arrays.asList(regExp),
				translationMap.keySet(),
				true
		);

		super.postConstructionInit(automaton);
	}

	public VacuityAwareWildcardAutomaton(String name, String regularExpression,
			Map<Character, AbstractTaskClass> translationMap) {
		this(regularExpression, translationMap);
		this.name = name;
	}

	public SortedSet<Character> getAlphabetWithoutWildcard() {
		return alphabetWithoutWildcard;
	}

	@Override
	protected ActivationStatusAwareState makeNewState() {
		return new ActivationStatusWildcardAwareState();
	}

	public static final Character getWildCardChar() {
		return AutomatonFactory.WILD_CARD;
	}
	public static final StringTaskClass getWildCardClass() {
		return StringTaskClass.WILD_CARD;
	}

	public ActivationStatusWildcardAwareState getInitialWildState() {
		return (ActivationStatusWildcardAwareState) super.getInitialState();
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VacuityAwareWildcardAutomaton [name=");
		builder.append(name);
		builder.append(", automaton=");
		builder.append(super.toDot().trim() + " // Open this with XDot or similar GraphViz tools");
		builder.append("]");
		return builder.toString();
	}

}