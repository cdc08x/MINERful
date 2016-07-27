package minerful.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
import minerful.concept.constraint.ConstraintsBag;
import minerful.concept.constraint.relation.RelationConstraint;
import minerful.index.comparator.allinone.HierarchyBasedComparator;
import minerful.index.comparator.allinone.InterestConfidenceBasedComparator;
import minerful.index.comparator.allinone.SupportBasedComparator;
import minerful.index.comparator.allinone.SupportConfidenceInterestFactorBasedComparator;
import minerful.index.comparator.allinone.SupportFamilyConfidenceInterestFactorHierarchyLevelBasedComparator;

public class LinearConstraintsIndexFactory {
	public static ConstraintsBag createConstraintsBagCloneIndexedByTaskCharAndSupport(ConstraintsBag bag) {
		ConstraintsBag bagCopy = (ConstraintsBag) bag.clone();
		TreeSet<Constraint> reindexed = null;
        for (TaskChar key : bagCopy.getTaskChars()) {
        	reindexed = new TreeSet<Constraint>(new SupportBasedComparator());
        	reindexed.addAll(bagCopy.getConstraintsOf(key));
        	bagCopy.eraseConstraintsOf(key);
        }
		return bagCopy;
	}
	
	public static ConstraintsBag indexByImpliedTaskChar(ConstraintsBag bag) {
		ConstraintsBag bagCopy = new ConstraintsBag(bag.getTaskChars());
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
	
	public static ConstraintsBag createConstraintsBagCloneIndexedByTaskCharAndInterest(ConstraintsBag bag) {
		ConstraintsBag bagCopy = (ConstraintsBag) bag.clone();
		TreeSet<Constraint> reindexed = null;
		for (TaskChar key : bagCopy.getTaskChars()) {
			reindexed = new TreeSet<Constraint>(new InterestConfidenceBasedComparator());
			reindexed.addAll(bagCopy.getConstraintsOf(key));
			bagCopy.eraseConstraintsOf(key);
		}
		return bagCopy;
	}
	
	/**
	 * The second coolest method I coded, ever.
	 * @param bag
	 * @return
	 */
	public static Map<TaskChar, Map<Class<? extends Constraint>, SortedSet<Constraint>>> indexByTaskCharConstraintTypeAndSupport(ConstraintsBag bag) {
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
		Collection<Class<? extends Constraint>> possibleConstraints = MetaConstraintUtils.ALL_DISCOVERABLE_CONSTRAINT_TEMPLATES;
		Map<Class<? extends Constraint>, SortedSet<Constraint>> localIndex = new HashMap<Class<? extends Constraint>, SortedSet<Constraint>>(possibleConstraints.size());
		
		for (Class<? extends Constraint> possibleConstraint : possibleConstraints) {
			localIndex.put(possibleConstraint, new TreeSet<Constraint>(new SupportBasedComparator()));
		}
		
		for (Constraint constraint : discoveredConstraints) {
			localIndex.get(constraint.getClass()).add(constraint);
		}
		
		return localIndex;
	}
	
	public static Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> indexByImplyingAndImplied(ConstraintsBag bag) {
		return indexByImplyingAndImplied(bag, false);
	}
	
	public static Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> indexByImplyingAndImplied(ConstraintsBag bag, boolean onlyUnmarked) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map = new TreeMap<TaskChar, Map<TaskChar,NavigableSet<Constraint>>>();
		Map<TaskChar, NavigableSet<Constraint>> subMap = null;
		TaskCharSet impliedSet = null;

		for (TaskChar tCh : bag.getTaskChars()) {
			subMap = new TreeMap<TaskChar, NavigableSet<Constraint>>();
			for (Constraint con : bag.getConstraintsOf(tCh)) {
				if (!onlyUnmarked || !con.isMarkedForExclusion()) {
					impliedSet = (
						(con.getImplied() == null)
						?	new TaskCharSet(tCh)
						:	con.getImplied()
					);
					for (TaskChar implied : impliedSet.getTaskCharsArray()) {
						if (!subMap.containsKey(implied)) {
							subMap.put(implied, new TreeSet<Constraint>());
						}
						subMap.get(implied).add(con);
					}
				}
			}
			map.put(tCh, subMap);
		}
		
		return map;
	}

	public static Collection<Constraint> getAllUnmarkedConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(
			ConstraintsBag bag) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> mapOfConstraintsIndexedByImplyingAndImplied =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(bag, true);
		
		return getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(mapOfConstraintsIndexedByImplyingAndImplied);
	}
	
	public static Collection<Constraint> getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(ConstraintsBag bag) {
		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> mapOfConstraintsIndexedByImplyingAndImplied =
				LinearConstraintsIndexFactory.indexByImplyingAndImplied(bag, false);
		return getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(mapOfConstraintsIndexedByImplyingAndImplied);
	}

	private static Collection<Constraint> getAllConstraintsSortedByBoundsSupportFamilyConfidenceInterestFactorHierarchyLevel(Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> mapOfConstraintsIndexedByImplyingAndImplied) {
		List<TaskChar> taskCharsSortedByNumberOfConnections =
				getTaskCharsSortedByNumberOfConnections(createMapOfConnections(mapOfConstraintsIndexedByImplyingAndImplied));
		Collection<Constraint> constraints = new ArrayList<Constraint>();
		Map<TaskChar, NavigableSet<Constraint>>
			subMap = null,
			subMapReverse = null;
		
		Set<TaskChar>
			taskCharsReverse = new TreeSet<TaskChar>(mapOfConstraintsIndexedByImplyingAndImplied.keySet());
		SortedSet<Constraint> tmpReorderingSet = null;
		
		// Starting from the activity having the highest number of constraints-based connections with other activities...
		for (TaskChar tCh : taskCharsSortedByNumberOfConnections) {
			// Get all constraints pertaining to tCh, indexed by the implied (target) activity
			subMap = mapOfConstraintsIndexedByImplyingAndImplied.get(tCh);
			// For every target activity
			for (TaskChar tChRev : taskCharsReverse) {
				if (subMap.containsKey(tChRev) && subMap.get(tChRev) != null && subMap.get(tChRev).size() > 0) {
					tmpReorderingSet = new TreeSet<Constraint>(new SupportFamilyConfidenceInterestFactorHierarchyLevelBasedComparator());
					tmpReorderingSet.addAll(subMap.get(tChRev));
					constraints.addAll(tmpReorderingSet);
					subMap.put(tChRev, null);
				}
				if (mapOfConstraintsIndexedByImplyingAndImplied.containsKey(tChRev)) {
					subMapReverse = mapOfConstraintsIndexedByImplyingAndImplied.get(tChRev);
					if (subMapReverse.containsKey(tCh) && subMapReverse.get(tCh) != null && subMapReverse.get(tCh).size() > 0) {
						tmpReorderingSet = new TreeSet<Constraint>(new SupportFamilyConfidenceInterestFactorHierarchyLevelBasedComparator());
						tmpReorderingSet.addAll(subMapReverse.get(tCh));
						constraints.addAll(tmpReorderingSet);
						subMapReverse.put(tCh, null);
					}
				}
			}
		}
		return constraints;
	}
	
//	public static Map<TaskChar, Set<TaskChar>> createMapOfConnections(ConstraintsBag bag) {
//		Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map =
//				LinearConstraintsIndexFactory.indexByImplyingAndImplied(bag);
//		
//		return createMapOfConnections(map);
//	}
	
	public static List<TaskChar> getTaskCharsSortedByNumberOfConnections(Map<TaskChar, Set<TaskChar>> map) {
		TreeMap<Integer, Set<TaskChar>> orderingMap = new TreeMap<Integer, Set<TaskChar>>();
		ArrayList<TaskChar> orderedTaskChars = new ArrayList<TaskChar>(map.keySet().size());
		
		Integer howManyCorrelatedTasks = 0;
		for (TaskChar tChr : map.keySet()) {
			howManyCorrelatedTasks = map.get(tChr).size();
			if (!orderingMap.containsKey(howManyCorrelatedTasks)) {
				orderingMap.put(howManyCorrelatedTasks, new TreeSet<TaskChar>());
			}
			orderingMap.get(howManyCorrelatedTasks).add(tChr);
		}
		for (Integer key : orderingMap.descendingKeySet()) {
			orderedTaskChars.addAll(orderingMap.get(key));
		}
		
		return orderedTaskChars;
	}

	public static Map<TaskChar, Set<TaskChar>> createMapOfConnections(
			Map<TaskChar, Map<TaskChar, NavigableSet<Constraint>>> map) {
		Map<TaskChar, Set<TaskChar>> mapOfConnections =
				new TreeMap<TaskChar, Set<TaskChar>>();
		for (TaskChar tChr : map.keySet()) {
			mapOfConnections.put(tChr, map.get(tChr).keySet());
		}
		return mapOfConnections;
	}
	
	public static SortedSet<Constraint> getAllConstraints(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>();
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllUnmarkedConstraints(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>();
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				if (!con.isMarkedForExclusion()) {
					allConstraints.add(con);
				}
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedBySupport(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new SupportBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedBySupportConfidenceInterestFactor(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new SupportConfidenceInterestFactorBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedBySupportFamilyConfidenceInterestFactorHierarchyLevel(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new SupportFamilyConfidenceInterestFactorHierarchyLevelBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedByInterest(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new InterestConfidenceBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
	
	public static SortedSet<Constraint> getAllConstraintsSortedByStrictness(ConstraintsBag bag) {
		SortedSet<Constraint> allConstraints = new TreeSet<Constraint>(new HierarchyBasedComparator());
		for (TaskChar tChr : bag.getTaskChars()) {
			for (Constraint con : bag.getConstraintsOf(tChr)) {
				allConstraints.add(con);
			}
		}
		return allConstraints;
	}
}