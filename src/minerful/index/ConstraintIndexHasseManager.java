package minerful.index;

import java.util.TreeMap;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;

public abstract class ConstraintIndexHasseManager {
	public static enum NavDirection {
		DOWN,
		UP
	}

	public ConstraintIndexHasseDiagram hasseDiagram;
	protected ConstraintIndexHasseNode currentNode;
	protected TaskCharSet currentTaskCharSet;

	public ConstraintIndexHasseManager() {
		super();
	}

	public ConstraintIndexHasseManager(ConstraintIndexHasseDiagram hasseDiagram) {
		this.hasseDiagram = hasseDiagram;
	}

	@Override
	public String toString() {
		return "ConstraintIndexHasseManager [hasseDiagram=" + hasseDiagram
				+ "]";
	}

	public String printInBreadthFirstVisit() {
		TreeMap<ConstraintIndexHasseNode, String>
			nuGenerationNodes = new TreeMap<ConstraintIndexHasseNode, String>();
		StringBuilder sBuil = new StringBuilder();
		int level = 1;
		
		for (TaskChar childTCh : hasseDiagram.root.children.keySet()) {
			nuGenerationNodes.put(hasseDiagram.root.children.get(childTCh), childTCh.toString());
			sBuil.append(childTCh);
			sBuil.append("\n");
		}
		sBuil.append("--------========\n        End of level ");
		sBuil.append(level);
		sBuil.append("\n========--------\n");
		
		sBuil.append(printInBreadthFirstVisit(nuGenerationNodes, ++level));
		
		return sBuil.toString();
	}

	public String printInBreadthFirstVisit(TreeMap<ConstraintIndexHasseNode, String> currentGenerationNodes, int level) {
		StringBuilder sBuil = new StringBuilder();
		TreeMap<ConstraintIndexHasseNode, String>
			nuGenerationNodes = new TreeMap<ConstraintIndexHasseNode, String>();
		String nuHistory = null;
		ConstraintIndexHasseNode nuChild = null;
		
		for (ConstraintIndexHasseNode currentGenerationNode : currentGenerationNodes.keySet()) {
			for (TaskChar childTCh : currentGenerationNode.children.keySet()) {
				nuHistory = currentGenerationNodes.get(currentGenerationNode) + " " + childTCh;
				nuChild = currentGenerationNode.children.get(childTCh);
				nuGenerationNodes.put(nuChild, nuHistory);
				sBuil.append("  " + nuHistory);
				sBuil.append("\n    child of \n");
				sBuil.append("        " + currentGenerationNodes.get(currentGenerationNode));
				if (nuChild.uncles.size() > 0) {
					sBuil.append("\n"
							+"    and nephew of \n");
					sBuil.append("        ");
					for (ConstraintIndexHasseNode uncle : nuChild.uncles) {
						sBuil.append(currentGenerationNodes.get(uncle));
						sBuil.append(" , ");
					}
				}
				sBuil.append('\n');
			}
		}
		
		if (sBuil.length() > 0)
			sBuil.append(printInBreadthFirstVisit(nuGenerationNodes, ++level));
		
		return sBuil.toString();
	}

}