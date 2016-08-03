package minerful.concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class TaskCharSetFactory {
	private final TaskCharArchive taskCharArchive;

	public TaskCharSetFactory(TaskCharArchive taskCharArchive) {
		this.taskCharArchive = taskCharArchive;
	}

	public TaskCharSet createSetFromRawCharacters(Collection<Character> characters) {
		return new TaskCharSet(this.taskCharArchive.getTaskCharsIdentifiedByCharacters(characters));
	}

	public TaskCharSet createSetFromTaskClasses(Collection<AbstractTaskClass> taskClasses) {
		return new TaskCharSet(this.taskCharArchive.getTaskCharsIdentifiedByTaskClasses(taskClasses));
	}

	public TaskCharSet createSetFromTaskStrings(Collection<String> taskNames) {
		return new TaskCharSet(this.taskCharArchive.getTaskCharsIdentifiedByStrings(taskNames));
	}

	public TaskCharSet[] createSetsFromTaskStringsCollection(
			List<Set<String>> parameters) {
		TaskCharSet[] sets = new TaskCharSet[parameters.size()];
		int i = 0;
		for (Set<String> paramSet: parameters) {
			sets[i++] = this.createSetFromTaskStrings(paramSet);
		}
		return sets;
	}
	
	public TaskCharSet createSet(TaskCharSet existing, Character plus) {
		return existing.pushAtLast(this.taskCharArchive.getTaskChar(plus));
	}
	
	public TaskCharSet createSet(TaskCharSet existing, TaskChar plus) {
		return existing.pushAtLast(plus);
	}

	public SortedSet<TaskCharSet> createAllMultiCharCombosExcludingOneTaskChar(TaskChar excluded, int maxSizeOfCombos) {
		Collection<TaskChar> alphabet = taskCharArchive.getTaskChars();
		Collection<TaskChar> otherChrs = new ArrayList<TaskChar>(alphabet);
		if(excluded != null)
			otherChrs.remove(excluded);

		SortedSet<TaskCharSet> combos = new TreeSet<TaskCharSet>();
		
		if (otherChrs.size() < 1) {
			return combos;
		}
		// Create the initial vector
		ICombinatoricsVector<TaskChar> initialVector = Factory.createVector(otherChrs);
		Generator<TaskChar> gen = null;
		Iterator<ICombinatoricsVector<TaskChar>> combosIterator = null;
		
		if (maxSizeOfCombos < otherChrs.size()) {
			for (int k=1; k <= maxSizeOfCombos; k++) {
				// Create a simple combination generator to generate k-combinations of the initial vector
				gen = Factory.createSimpleCombinationGenerator(initialVector, k);
				combosIterator = gen.iterator();
				while (combosIterator.hasNext()) {
					combos.add(new TaskCharSet(combosIterator.next().getVector()));
				}
			}
		} else {
			Collection<TaskChar> auxComboVector = null;
			// Create an instance of the subset generator
			gen = Factory.createSubSetGenerator(initialVector);
			combosIterator = gen.iterator();
			while (combosIterator.hasNext()) {
				auxComboVector = combosIterator.next().getVector();
				if (	auxComboVector.size() > 0
					&&	auxComboVector.size() <= otherChrs.size()) {
					combos.add(new TaskCharSet(auxComboVector));
				}
			}
		}
		return combos;
	}

	public SortedSet<TaskCharSet> createAllMultiCharCombos(int maxSizeOfCombos) {
		return this.createAllMultiCharCombosExcludingOneTaskChar(null, maxSizeOfCombos);
	}
}