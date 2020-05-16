package minerful.automaton;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;

public class DimensionalityHeuristicBasedCallableSubAutomataMaker implements Callable<SubAutomaton> {
	private static Logger logger = Logger.getLogger(DimensionalityHeuristicBasedCallableSubAutomataMaker.class.getCanonicalName());
	
	public final Character basingCharacter;
	public final Collection<String> regularExpressions;

	public DimensionalityHeuristicBasedCallableSubAutomataMaker(Character basingCharacter, Collection<String> regularExpressions) {
		this.regularExpressions = regularExpressions;
		this.basingCharacter = basingCharacter;
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
        	nuRegExp = null;

        logger.trace("Preparing the automaton...");
        
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
        		logger.trace("Automaton states: " + automaton.getNumberOfStates() + "; automaton transitions: " + automaton.getNumberOfTransitions());
        	}
// DEBUGGING!
//        	processAutomaton = processAutomaton.intersection(new RegExp("[^a]?[^a]a[^a][^a]?").toAutomaton());
// E.O. DEBUGGING
        }
        automaton.minimize();
        return new SubAutomaton(basingCharacter, automaton);
	}
}