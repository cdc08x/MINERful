package minerful.concept.constraint;

import java.util.HashMap;
import java.util.Map;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.index.ConstraintIndexHasseDiagram;

public class BranchedConstraintsStructure {
	public Map<TaskChar, ConstraintIndexHasseDiagram> inBranchedStructuresMap;
	public Map<TaskChar, ConstraintIndexHasseDiagram> outBranchedStructuresMap;
	
	
	public BranchedConstraintsStructure() {
	}
	public BranchedConstraintsStructure(TaskCharArchive taskCharArchive) {
		this.inBranchedStructuresMap = new HashMap<TaskChar, ConstraintIndexHasseDiagram>(taskCharArchive.size(), (float) 1.0);
		this.outBranchedStructuresMap = new HashMap<TaskChar, ConstraintIndexHasseDiagram>(taskCharArchive.size(), (float) 1.0);
	}
}