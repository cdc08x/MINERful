package minerful.checking.relevance.walkers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import minerful.automaton.concept.relevance.VacuityAwareWildcardAutomaton;
import minerful.checking.ConstraintsFitnessEvaluator;
import minerful.concept.AbstractTaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

/**
 * This class serves as a machine to walk on automata that abstract from the single specific event class.
 * The trick is, the automaton is parametric to the specific character that labels the action.
 * Multiple cursors ("walkers") point at the current state as if the underlying automaton was not parametric.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class RelevanceAutomatonMultiWalker {
	private static MessagePrinter logger = MessagePrinter.getInstance(RelevanceAutomatonMultiWalker.class);
	
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
			List<List<Collection<Character>>> charParametersList,
			Character[] formalParameters) {
		this.name = name;
		this.init(vAwaWildAuto, logTranslationMap);
		logger.debug("DEBUG PRINTOUT name " + name);
		this.walkers = setupAllWalkers(charParametersList, formalParameters);
	}
	
	/**
	 * 
	 * @param charParametersListOfLists For every constraint, this data structure considers the list of actual parameters in the form of characters.
		 We have thus a list (one for each constraint)
		 of lists (one for each parameter of that constraint) 
		 of collections (the parameter's characters; can be more than one, due to branching).: 
		 NOTICE that we assume that the number of parameters (so, the size of the first nested list)
		 stays the same for all elements of the first nested list, because we assume the template to be fixed.
	 * @return
	 */
	private List<RelevanceAutomatonWalker> setupAllWalkers(List<List<Collection<Character>>> charParametersListOfLists, Character[] formalParameters) {
		if (charParametersListOfLists.size() == 0 || charParametersListOfLists.get(0).size() == 0) {
			throw new IllegalArgumentException("The passed list cannot be empty, nor can the inner lists or collections!");
		}
		ArrayList<RelevanceAutomatonWalker> walkers =
				new ArrayList<RelevanceAutomatonWalker>(charParametersListOfLists.size());
		// Formal parameters label the automaton's transitions (with the exception of the wild-card character)
		List<Character> automAlphabetNoWildcard = new ArrayList<Character>(Arrays.asList(formalParameters));
		for (List<Collection<Character>> charParametersList : charParametersListOfLists) {
			walkers.add(
					new RelevanceAutomatonWalker(
							this.name,
							charParametersList,
							automAlphabetNoWildcard,
							logTranslationMap, 
							vacuAwaWildAuto.getInitialWildState()));
		}
		return walkers;
	}

	private List<RelevanceAutomatonWalker> setUpAllWalkersByPermutationsOfTasks() {
		List<Character> alphabetWithoutWildcard = new ArrayList<Character>(vacuAwaWildAuto.getAlphabetWithoutWildcard());
		int k = alphabetWithoutWildcard.size();

		Set<Character> taskIdentifiersInLog = this.logTranslationMap.keySet();
		
		if (taskIdentifiersInLog.size() < k) {
			throw new IllegalArgumentException("Not enough tasks in the log to instantiate the constraint");
		}

		ICombinatoricsVector<Character> initialVector =
				Factory.createVector(taskIdentifiersInLog.toArray(new Character[k+1]));
		Iterator<ICombinatoricsVector<Character>> combosPermIterator = null;
		Generator<Character>
			comboGen = Factory.createSimpleCombinationGenerator(initialVector, k),
			combosPermGen = null;
		ArrayList<RelevanceAutomatonWalker> walkers = new ArrayList<RelevanceAutomatonWalker>();
		List<Character> vectorOfChars = null;
		List<Collection<Character>> charParameters = null;
		int i = 0;
		for (ICombinatoricsVector<Character> simpleCombo : comboGen) {
			combosPermGen = Factory.createPermutationGenerator(simpleCombo);
			combosPermIterator = combosPermGen.iterator();
			while (combosPermIterator.hasNext()) {
				vectorOfChars = combosPermIterator.next().getVector();
				charParameters = new ArrayList<Collection<Character>>(vectorOfChars.size());
				for (Character charParam : vectorOfChars) {
					charParameters.add(Arrays.asList(charParam));
				}
				walkers.add(i++, new RelevanceAutomatonWalker(
						this.name + "/" + vectorOfChars,
						charParameters,
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