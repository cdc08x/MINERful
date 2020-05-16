package minerful.index;

import java.util.TreeMap;
import java.util.TreeSet;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharSet;
import minerful.concept.TaskCharSetFactory;
import minerful.concept.constraint.Constraint;

import org.apache.commons.lang3.StringUtils;

public class ConstraintIndexHasseMaker extends ConstraintIndexHasseManager {
	private TaskCharArchive taskCharArchive;
	private TaskCharSetFactory taskCharSetFactory;
	
	public ConstraintIndexHasseMaker(TaskCharArchive taskCharArchive,
			int maxSizeOfCombos, TaskChar excludedTaskChar) {
		this.hasseDiagram = new ConstraintIndexHasseDiagram();
		this.currentNode = hasseDiagram.root;
		this.taskCharArchive = taskCharArchive;
		this.taskCharSetFactory = new TaskCharSetFactory(this.taskCharArchive);
		this.currentTaskCharSet = TaskCharSet.VOID_TASK_CHAR_SET;
		this.populateHasseDiagram(maxSizeOfCombos, excludedTaskChar);
	}
	
	public ConstraintIndexHasseMaker(TaskCharArchive taskCharArchive, int maxSizeOfCombos) {
		this(taskCharArchive, maxSizeOfCombos, null);
	}

	private void populateHasseDiagram(int maxSizeOfCombos, TaskChar excludedTaskChar) {
		this.currentNode = hasseDiagram.root;
		this.currentTaskCharSet = TaskCharSet.VOID_TASK_CHAR_SET;
		
		if (maxSizeOfCombos > this.taskCharArchive.size() - (excludedTaskChar == null ? 0 : 1))
			maxSizeOfCombos = this.taskCharArchive.size() - (excludedTaskChar == null ? 0 : 1);
		
		TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>
			currentDepthNodes = new TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>(),
			newRootNodes = null,
			newGenerationNodes = new TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>();
		
		TreeSet<TaskChar> taskChars = this.taskCharArchive.getCopyOfTaskChars();
		if (excludedTaskChar != null)
			taskChars.remove(excludedTaskChar);
		
		TreeMap<String, ConstraintIndexHasseNode> historyForNodesGeneratedByTheSameTaskCharBranch = new TreeMap<String, ConstraintIndexHasseNode>();
		ConstraintIndexHasseNode nuHasseNode = null;
		// First level: single task characters
		for (TaskChar tCh : taskChars) {
			nuHasseNode = new ConstraintIndexHasseNode(this.currentNode, new TaskCharSet(tCh));
			historyForNodesGeneratedByTheSameTaskCharBranch = new TreeMap<String, ConstraintIndexHasseNode>();
			this.currentNode.children.put(tCh, nuHasseNode);
			historyForNodesGeneratedByTheSameTaskCharBranch.put(String.valueOf(tCh.identifier), nuHasseNode);
			currentDepthNodes.put(tCh, historyForNodesGeneratedByTheSameTaskCharBranch);
		}

		boolean newLevelIsNeeded = (--maxSizeOfCombos) > 0;
		TreeSet<TaskChar> remainingTaskChars = taskChars;
		
		// Second to N-th level, with N equal to the size of the alphabet.
		TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>> temporaryNewGenerationNodes = null;
		TreeMap<String, ConstraintIndexHasseNode> temporaryNewGenerationNodeHistories = null;
		
		while(newLevelIsNeeded) {
			newRootNodes = (TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>)currentDepthNodes.clone();
			newGenerationNodes = new TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>();
			for (TaskChar keyTaskChar : currentDepthNodes.keySet()) {
				historyForNodesGeneratedByTheSameTaskCharBranch = currentDepthNodes.get(keyTaskChar);
				remainingTaskChars = (TreeSet<TaskChar>)taskChars.tailSet(keyTaskChar, false);
				for (String history : historyForNodesGeneratedByTheSameTaskCharBranch.keySet()) {
					if (remainingTaskChars != null) {
						temporaryNewGenerationNodes = 
							populateHasseOneLevelDeeper(
								historyForNodesGeneratedByTheSameTaskCharBranch.get(history),
								keyTaskChar,
								history,
								newRootNodes,
								remainingTaskChars
							);
						for (TaskChar tempNuGenNodesKey : temporaryNewGenerationNodes.keySet()) {
							temporaryNewGenerationNodeHistories = temporaryNewGenerationNodes.get(tempNuGenNodesKey);
							if (newGenerationNodes.containsKey(tempNuGenNodesKey)) {
								for (String historyForTempNuGenNodes : temporaryNewGenerationNodeHistories.keySet()) {
									newGenerationNodes.get(tempNuGenNodesKey).put(historyForTempNuGenNodes, temporaryNewGenerationNodeHistories.get(historyForTempNuGenNodes));
								}
							} else {
								newGenerationNodes.put(tempNuGenNodesKey, temporaryNewGenerationNodes.get(tempNuGenNodesKey));
							}
						}
					}
				}
			}
			newLevelIsNeeded = (--maxSizeOfCombos) > 0;
			if (newLevelIsNeeded) {
				currentDepthNodes = (TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>)newGenerationNodes.clone();
			}
		}
//		// REMOVED: newGenerationNodes.size() > 1 when the branching factor is lower than the number of process activities
//		if (newGenerationNodes.size() > 1) {
//			throw new IllegalStateException("Multiple sink nodes in Hasse diagram");
//		} else {
		for (TaskChar sinkTaskChar : newGenerationNodes.keySet()) {
			for (String sinkNodeHistory : newGenerationNodes.get(sinkTaskChar).keySet()) { // expected to be 1
				this.hasseDiagram.addSink(newGenerationNodes.get(sinkTaskChar).get(sinkNodeHistory));
			}
		}
//		}
	}
	
	private TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>> populateHasseOneLevelDeeper(
			ConstraintIndexHasseNode root,
			TaskChar rootTaskChar,
			String historyForRoot,
			TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>> olderGeneration,
			TreeSet<TaskChar> taskChars) {
		TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>> treep =
				new TreeMap<TaskChar, TreeMap<String, ConstraintIndexHasseNode>>();
		
		String nuNodeHistory = null;
		TreeMap<String, ConstraintIndexHasseNode>
			uncles = null,
			nuSiblings = null;
		ConstraintIndexHasseNode nuNode = null;

		for (TaskChar tCh : taskChars) {
			nuSiblings = new TreeMap<String, ConstraintIndexHasseNode>();
			
			nuNode = new ConstraintIndexHasseNode(root, this.taskCharSetFactory.createSet(root.indexedTaskCharSet, tCh));
			root.children.put(tCh, nuNode);
			nuNodeHistory = historyForRoot + tCh.identifier;
			uncles = olderGeneration.get(tCh);
			for (String uncleHistory : uncles.keySet()) {
				if (StringUtils.containsOnly(uncleHistory, nuNodeHistory)) {
					nuNode.uncles.add(uncles.get(uncleHistory));
				}
			}
			
			nuSiblings.put(nuNodeHistory, nuNode);
			treep.put(tCh, nuSiblings);
		}
		return treep;
	}
	

	public ConstraintIndexHasseNode addConstraint(TaskCharSet referenceTaskChSet, Constraint c) {
		currentNode = this.searchNodeForConstraint(referenceTaskChSet, c);
		currentNode.addConstraint(c);
		return currentNode;
	}

	/**
	 * Optimised for depth-first search!
	 * @param refTaChSetId
	 * @param c
	 * @return
	 */
	private ConstraintIndexHasseNode searchNodeForConstraint(TaskCharSet referenceTaskChSet, Constraint c) {
		// Is the current node OK for inserting the constraint?
		if (currentTaskCharSet.equals(referenceTaskChSet)) {
			return this.currentNode;
		} else {
			// Is the current node a sibling or a deeper descendant of an ancestor?
			if (currentTaskCharSet.size() >= referenceTaskChSet.size()) {
				this.currentNode = this.currentNode.parent;
				this.currentTaskCharSet = this.currentNode.indexedTaskCharSet;
				return this.searchNodeForConstraint(referenceTaskChSet, c);
			} else {
				// The following is implicit: is the current node a parent or a less deep descendant of an ancestor?
				// if (currentStringOfIdentiers.length() < refTaChSetId.length()) {
				// Is this a direct ancestor?
				if (this.currentTaskCharSet.isPrefixOf(referenceTaskChSet)) {
					TaskChar parentToFindId = referenceTaskChSet.getTaskChar(this.currentTaskCharSet.size());
					this.currentNode = this.currentNode.children.get(parentToFindId);
					this.currentTaskCharSet = this.currentNode.indexedTaskCharSet;
					return this.searchNodeForConstraint(referenceTaskChSet, c);
				// ... or not? In case, you have to rise along the hierarchy and search for a common ancestor!
				} else {
					this.currentNode = this.currentNode.parent;
					this.currentTaskCharSet = this.currentNode.indexedTaskCharSet;
					return this.searchNodeForConstraint(referenceTaskChSet, c);
				}
			}
		}
	}
}