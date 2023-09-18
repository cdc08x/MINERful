package minerful.automaton;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class DimensionalityHeuristicBasedCallableBriefSubAutomataMaker extends DimensionalityHeuristicBasedCallableSubAutomataMaker {
	private static Logger logger = Logger.getLogger(DimensionalityHeuristicBasedCallableBriefSubAutomataMaker.class.getCanonicalName());
	public static final String LIMITING_ACTIONS_REG_EXP_TEMPLATE =  "[^%1$s]{0,%2$d}%1$s[^%1$s]{0,%2$d}";
	
	public final int maxActions;
	
	public DimensionalityHeuristicBasedCallableBriefSubAutomataMaker(Character basingCharacter, Collection<String> regularExpressions, int maxActions) {
		super(basingCharacter, regularExpressions);
		this.maxActions = maxActions;
	}

	@Override
	public SubAutomaton call() throws Exception {
        // Turn strings into Regular Expressions and attach them
        Automaton
        	automaton = null;
        
        SortedSet<Automaton> regExpAutomata =
        		new TreeSet<Automaton>(
        				new DimensionalityHeuristicBasedAutomataIntersector.AutomataAscendingDimensionComparator());
        
        String
        	nuRegExp = null,
        	limitingRegExp = buildActivitiesLimitingRegExp();

        logger.trace("Preparing the automaton...");
        regExpAutomata.add(new RegExp(limitingRegExp).toAutomaton());
        
        if (regularExpressions.size() > 0) {

        	Iterator<String> regExpsIterator = regularExpressions.iterator();
        	if (regExpsIterator.hasNext()) {
// DEBUGGING!
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1," + alphabet.size() * 2 + "}").toAutomaton());
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1," + ((int)(alphabet.size() * 1.25)) + "}").toAutomaton());
//        	processAutomaton = processAutomaton.intersection(new RegExp(".{1,24}").toAutomaton());
// E.O. DEBUGGING
        		
        		while (regExpsIterator.hasNext()) {
        			nuRegExp = regExpsIterator.next();
//        			logger.trace("Intersecting the automaton with the accepting for: " + nuRegExp);
        			regExpAutomata.add(new RegExp(nuRegExp).toAutomaton());
        		}
        		
        		automaton = new DimensionalityHeuristicBasedAutomataIntersector().intersect(regExpAutomata);
        		automaton.minimize();
        		logger.trace("Automaton states: " + automaton.getNumberOfStates() + "; automaton transitions: " + automaton.getNumberOfTransitions());
        		automaton = this.refineAutomaton(automaton);
        	}
        }
        return new SubAutomaton(basingCharacter, automaton);
	}
	
	private Automaton refineAutomaton(Automaton automaton) {
		State initialState = automaton.getInitialState();
		this.pruneOutRedundantTransitions(initialState);
		
		automaton.minimize();
		return automaton;
	}
	
	private void pruneOutRedundantTransitions(State initialState) {
		boolean redundantTransitions = false;
		State nextState = initialState.step(basingCharacter.charValue());
		
		// Heuristic 1: if the automaton contains an action with the basingCharacter, the other are optional: we can exclude them!
		if (nextState != null)
			redundantTransitions = true;
		if (redundantTransitions) {
			Iterator<Transition> transIterator = initialState.getTransitions().iterator();
			while (transIterator.hasNext()) {
				transIterator.next();
				transIterator.remove();
			}
			initialState.addTransition(new Transition(basingCharacter, nextState));
		}
		else {
			Set<Transition> transitions = initialState.getTransitions();
			if (transitions.size() == 0) {
				return;
			} else {
				for (Transition transition : transitions) {
					this.pruneOutRedundantTransitions(transition.getDest());
				}
			}
		}
	}

	protected String buildActivitiesLimitingRegExp() {
		return String.format(LIMITING_ACTIONS_REG_EXP_TEMPLATE, this.basingCharacter, this.maxActions);
	}
}