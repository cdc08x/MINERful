package minerful.index;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.concept.constraint.relation.RelationConstraint;

public class LinearConstraintsIndexFactory {
	public static class HierarchyBasedComparator implements Comparator<Constraint> {
		@Override
		public int compare(Constraint o1, Constraint o2) {
			int result = Integer.valueOf(o1.getHierarchyLevel()).compareTo(Integer.valueOf(o1.getHierarchyLevel()));
			return (
					(result == 0)
					?	o1.compareTo(o2)
							:	result * (-1)
					);
		}
	}
	public static class SupportBasedComparator implements Comparator<Constraint> {
		@Override
		public int compare(Constraint o1, Constraint o2) {
			int result = Double.valueOf(o1.support).compareTo(Double.valueOf(o2.support));
			return (
				(result == 0)
				?	o1.compareTo(o2)
				:	result * (-1)
			);
		}
	}
	public static class InterestBasedComparator implements Comparator<Constraint> {
		@Override
		public int compare(Constraint o1, Constraint o2) {
			Double 	interestOfO1 = null,
					interestOfO2 = null;
			int		result = 0;
			
			if (o1 instanceof RelationConstraint) {
				interestOfO1 = ((RelationConstraint)o1).interestFactor;
			} else {
				interestOfO1 = o1.confidence;
			}
			if (o2 instanceof RelationConstraint) {
				interestOfO2 = ((RelationConstraint)o2).interestFactor;
			} else {
				interestOfO2 = o2.confidence;
			}
			
			result = interestOfO1.compareTo(interestOfO2);
			
			if (result == 0 && (o1 instanceof RelationConstraint || o2 instanceof RelationConstraint)) {
				interestOfO1 = o1.confidence;
				interestOfO2 = o2.confidence;
				result = interestOfO1.compareTo(interestOfO2);
			}
			
			return (
				(result == 0)
				?	o1.compareTo(o2)
				:	result * (-1)
			);
		}
	}
	
	public static TaskCharRelatedConstraintsBag indexByTaskCharAndSupport(TaskCharRelatedConstraintsBag bag) {
		TaskCharRelatedConstraintsBag bagCopy = (TaskCharRelatedConstraintsBag) bag.clone();
		TreeSet<Constraint> reindexed = null;
        for (TaskChar key : bagCopy.getTaskChars()) {
        	reindexed = new TreeSet<Constraint>(new SupportBasedComparator());
        	reindexed.addAll(bagCopy.getConstraintsOf(key));
        	bagCopy.replaceConstraints(key, reindexed);
        }
		return bagCopy;
	}
	
	public static TaskCharRelatedConstraintsBag indexByImpliedTaskChar(TaskCharRelatedConstraintsBag bag) {
		TaskCharRelatedConstraintsBag bagCopy = new TaskCharRelatedConstraintsBag(bag.getTaskChars());
        for (TaskChar key : bag.getTaskChars()) {
        	for (Constraint c : bag.getConstraintsOf(key)) {
        		if (c instanceof RelationConstraint) {
        			bagCopy.add(((RelationConstraint)c).getImplied(), c);
        		} else {
        		}
        	}
        }
        return bagCopy;
	}
	
	public static TaskCharRelatedConstraintsBag indexByTaskCharAndInterest(TaskCharRelatedConstraintsBag bag) {
		TaskCharRelatedConstraintsBag bagCopy = (TaskCharRelatedConstraintsBag) bag.clone();
		TreeSet<Constraint> reindexed = null;
		for (TaskChar key : bagCopy.getTaskChars()) {
			reindexed = new TreeSet<Constraint>(new InterestBasedComparator());
			reindexed.addAll(bagCopy.getConstraintsOf(key));
			bagCopy.replaceConstraints(key, reindexed);
		}
		return bagCopy;
	}
	
	/**
	 * The second coolest method I coded, ever.
	 * @param bag
	 * @return
	 */
	public static Map<TaskChar, Map<Class<? extends Constraint>, SortedSet<Constraint>>> indexByTaskCharConstraintTypeAndSupport(TaskCharRelatedConstraintsBag bag) {
		Map<TaskChar, Map<Class<? extends Constraint>, SortedSet<Constraint>>> index =
				new HashMap<TaskChar,
				Map<Class<? extends Constraint>, SortedSet<Constraint>>>(bag.getTaskChars().size());
		
		for (TaskChar taskChar : bag.getTaskChars()) {
			index.put(taskChar,
					indexByConstraintTypeAndSupport(
							bag.getConstraintsOf(taskChar),
							taskChar
					)
			);
		}
		
		return index;
	}
	
	public static Map<Class<? extends Constraint>, SortedSet<Constraint>> indexByConstraintTypeAndSupport(Set<? extends Constraint> discoveredConstraints, TaskChar taskChar) {
		Collection<Class<? extends Constraint>> possibleConstraints = MetaConstraintUtils.ALL_POSSIBLE_CONSTRAINT_TEMPLATES;
		Map<Class<? extends Constraint>, SortedSet<Constraint>> localIndex = new HashMap<Class<? extends Constraint>, SortedSet<Constraint>>(possibleConstraints.size());
		
		for (Class<? extends Constraint> possibleConstraint : possibleConstraints) {
			localIndex.put(possibleConstraint, new TreeSet<Constraint>(new SupportBasedComparator()));
		}
		
		for (Constraint constraint : discoveredConstraints) {
			localIndex.get(constraint.getClass()).add(constraint);
		}
		
		return localIndex;
	}
	
	public static Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> indexByImplyingAndImplied(TaskCharRelatedConstraintsBag bag) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map = new TreeMap<TaskChar, Map<TaskChar,NavigableSet<Constraint>>>();
		Map<TaskChar, NavigableSet<Constraint>> subMap = null;
		TaskCharSet impliedSet = null;

		for (TaskChar tCh : bag.getTaskChars()) {
			subMap = new TreeMap<TaskChar, NavigableSet<Constraint>>();
			for (Constraint con : bag.getConstraintsOf(tCh)) {
				impliedSet = (
					(con.getImplied() == null)
					?	new TaskCharSet(tCh)
					:	con.getImplied()
				);
				for (TaskChar implied : impliedSet.getTaskChars()) {
					if (!subMap.containsKey(implied)) {
						subMap.put(implied, new TreeSet<Constraint>());
					}
					subMap.get(implied).add(con);
				}
			}
			map.put(tCh, subMap);
		}
		
		return map;
	}
	
	public static SortedSet<Constraint> getAllConstraints(TaskCharRelatedConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>();
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedBySupport(TaskCharRelatedConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new SupportBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedByInterest(TaskCharRelatedConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new InterestBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedByStrictness(TaskCharRelatedConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new HierarchyBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
}