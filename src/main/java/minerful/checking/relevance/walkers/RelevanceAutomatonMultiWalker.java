package minerful.checking.relevance.walkers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.concept.AbstractTaskClass;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;

/**
 * This class serves as a machine to walk on automata that abstract from the single specific event class.
 * The trick is, the automaton is parametric to the specific character that labels the action.
 * Multiple cursors ("walkers") point at the current state as if the underyling automaton was not parametric.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class RelevanceAutomatonMultiWalker {
	private final String name;
	private VacuityAwareWildcardAutomaton vacuAwaWildAuto;
	private Map<Character, AbstractTaskClass> logTranslationMap;
	private List<RelevanceAutomatonWalker> walkers;
	

	private void init(VacuityAwareWildcardAutomaton vAwaWildAuto, Map<Character, AbstractTaskClass> logTranslationMap) {
		this.vacuAwaWildAuto = vAwaWildAuto;
		this.logTranslationMap = logTranslationMap;
	}

	public RelevanceAutomatonMultiWalker(
			String name,
			VacuityAwareWildcardAutomaton vAwaWildAuto,
			Map<Character, AbstractTaskClass> logTranslationMap) {
		this.name = name;
		init(vAwaWildAuto, logTranslationMap);
		this.walkers = setUpAllWalkersByPermutationsOfTasks();
	}

	public RelevanceAutomatonMultiWalker(
			String name,
			VacuityAwareWildcardAutomaton vAwaWildAuto,
			Map<Character, AbstractTaskClass> logTranslationMap,
			List<List<Character>> charParametersList) {
		this.name = name;
		init(vAwaWildAuto, logTranslationMap);
//System.out.println("Lurido merdonazzo charParametersList " + charParametersList);
		this.walkers = setupAllWalkers(charParametersList);
	}
	
	private List<RelevanceAutomatonWalker> setupAllWalkers(List<List<Character>> charParametersList) {
		List<Character> automAlphabetNoWildcard = charParametersList.get(0);
		ArrayList<RelevanceAutomatonWalker> walkers =
				new ArrayList<RelevanceAutomatonWalker>(charParametersList.size());
		for (List<Character> charParameters : charParametersList) {
//System.out.println("Lurido merdo charParameters " + charParameters);
//System.out.println("Lurido merdo alphabetWithoutWildcard " + automAlphabetNoWildcard);
			walkers.add(
					new RelevanceAutomatonWalker(
							this.name + "/" + charParameters,
							charParameters,
							automAlphabetNoWildcard,
							logTranslationMap, 
							vacuAwaWildAuto.getInitialWildState()));

		}
//System.out.println("Lurido merdone logTranslationMap: " + logTranslationMap);
		return walkers;
	}

	private List<RelevanceAutomatonWalker> setUpAllWalkersByPermutationsOfTasks() {
		List<Character> alphabetWithoutWildcard = new ArrayList<Character>(vacuAwaWildAuto.getAlphabetWithoutWildcard());
		int k = alphabetWithoutWildcard.size();

		Set<Character> taskIdentifiersInLog = this.logTranslationMap.keySet();
		
		if (taskIdentifiersInLog.size() < k) {
			throw new IllegalArgumentException("Not enough tasks in the log to instanciate the constraint");
		}

		ICombinatoricsVector<Character> initialVector =
				Factory.createVector(taskIdentifiersInLog.toArray(new Character[k+1]));
		Iterator<ICombinatoricsVector<Character>> combosPermIterator = null;
		Generator<Character>
			comboGen = Factory.createSimpleCombinationGenerator(initialVector, k),
			combosPermGen = null;
		ArrayList<RelevanceAutomatonWalker> walkers = new ArrayList<RelevanceAutomatonWalker>();
		List<Character> vectorOfChars = null;
		int i = 0;
		for (ICombinatoricsVector<Character> simpleCombo : comboGen) {
			combosPermGen = Factory.createPermutationGenerator(simpleCombo);
			combosPermIterator = combosPermGen.iterator();
			while (combosPermIterator.hasNext()) {
				vectorOfChars = combosPermIterator.next().getVector();
				walkers.add(i++, new RelevanceAutomatonWalker(
						this.name + "/" + vectorOfChars,
						vectorOfChars,
						alphabetWithoutWildcard,
						logTranslationMap,
						vacuAwaWildAuto.getInitialWildState()));
			}
		}

		return walkers;
	}
	
	public void run(LogParser log) {
		Iterator<LogTraceParser> logPar = log.traceIterator();
		while (logPar.hasNext()) {
			this.run(logPar.next());
		}
	}

	public void run(LogTraceParser traceParser) {
		this.reset();

		AbstractTaskClass tasCla = null;
		while (!traceParser.isParsingOver()) {
			tasCla = traceParser.parseSubsequent().getEvent().getTaskClass();
			for (RelevanceAutomatonWalker walker : walkers) {
				walker.step(tasCla);
			}
		}
	}

	public List<RelevanceAutomatonWalker> getWalkers() {
		return walkers;
	}

	public void reset() {
		for (RelevanceAutomatonWalker walker : this.walkers) {
			walker.reset();
		}
	}

	public int getNumberOfWalkers() {
		return this.walkers.size();
	}

	public boolean remove(RelevanceAutomatonWalker walker) {
		return this.walkers.remove(walker);
	}
}