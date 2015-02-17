package minerful.automaton;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import dk.brics.automaton.Automaton;

public class DimensionalityHeuristicBasedAutomataIntersector {
	private static Logger logger = Logger.getLogger(DimensionalityHeuristicBasedAutomataIntersector.class.getCanonicalName());

	public static class AutomataAscendingDimensionComparator implements Comparator<Automaton> {
		@Override
		public int compare(Automaton o1, Automaton o2) {
			return	Integer.valueOf(o1.getNumberOfTransitions())
					.compareTo(
							Integer.valueOf(o2.getNumberOfTransitions())
					);
		}
	}

	public static class AutomataDescendingDimensionComparator implements Comparator<Automaton> {
		@Override
		public int compare(Automaton o1, Automaton o2) {
			return	Integer.valueOf(o1.getNumberOfTransitions())
					.compareTo(
							Integer.valueOf(o2.getNumberOfTransitions())
							)
					*
					(-1);
		}
	}

	public Automaton intersect(Collection<Automaton> automata) {
		TreeSet<Automaton>
			orderedAutomata = new TreeSet<Automaton>(new AutomataAscendingDimensionComparator());
		
		orderedAutomata.addAll(automata);
				
		return intersectInRecursion(orderedAutomata).pollFirst();
	}
	
	private NavigableSet<Automaton> intersectInRecursion(NavigableSet<Automaton> orderedAutomata) {
		if (orderedAutomata.size() < 2) {
			return orderedAutomata;
		}
		
		TreeSet<Automaton>
			automataForNextRound = new TreeSet<Automaton>(new AutomataAscendingDimensionComparator());
		Automaton
			intersectedAutomaton = null;
		
		int sizeOfAutomataSet = orderedAutomata.size();
		
		for (int i = 0; i < sizeOfAutomataSet / 2; i++) {
			logger.trace("Intersecting automaton...");
			intersectedAutomaton = orderedAutomata.pollFirst().intersection(orderedAutomata.pollLast());
			// Only for the last loop, and only if the number of automata were odd
			if (orderedAutomata.size() == 1) {
				// Intersect with the spurious automaton
				automataForNextRound.add(intersectedAutomaton.intersection(orderedAutomata.pollFirst()));
			}
			else {
				automataForNextRound.add(intersectedAutomaton);
			}
    		logger.trace("Automaton states: " + intersectedAutomaton.getNumberOfStates() + "; automaton transitions: " + intersectedAutomaton.getNumberOfTransitions());
		}
		
		return intersectInRecursion(automataForNextRound);
	}
}