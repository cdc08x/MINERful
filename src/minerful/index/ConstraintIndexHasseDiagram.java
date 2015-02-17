package minerful.index;

import java.util.ArrayList;
import java.util.Collection;

import minerful.concept.TaskCharSet;


public class ConstraintIndexHasseDiagram {
	final ConstraintIndexHasseNode root;
	private Collection<ConstraintIndexHasseNode> sinkNodes;

	public ConstraintIndexHasseDiagram() {
		this.root = new ConstraintIndexHasseNode(null, TaskCharSet.VOID_TASK_CHAR_SET);
		this.sinkNodes = new ArrayList<ConstraintIndexHasseNode>();
	}
	
	public void addSink(ConstraintIndexHasseNode sink) {
		this.sinkNodes.add(sink);
	}
	public Collection<ConstraintIndexHasseNode> getSinkNodes() {
		return this.sinkNodes;
	}

	@Override
	public String toString() {
		StringBuilder sBuil = new StringBuilder();
		sBuil.append("BranchedConstraintIndexTree\n");
		
		// sBuil.append(this.root.toString());
		sBuil.append(this.root.toPrefixedPathString());
		
		return sBuil.toString();
	}
}