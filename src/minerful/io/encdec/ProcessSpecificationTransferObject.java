package minerful.io.encdec;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.pojo.ConstraintPojo;
import minerful.io.encdec.pojo.ProcessSpecificationPojo;

public class ProcessSpecificationTransferObject {
	public final String name;
	public final Set<String> tasks;
	public final Set<DeclareConstraintTransferObject> constraints;
	
	public ProcessSpecificationTransferObject(ProcessSpecificationPojo specificationPojo) {
		this.name = specificationPojo.name;
		this.constraints = new TreeSet<DeclareConstraintTransferObject>();
		this.tasks = new TreeSet<String>();
		if (specificationPojo.tasks == null || specificationPojo.tasks.size() == 0) {
			if (specificationPojo.constraints.size() > 0) {
				for (ConstraintPojo pojo : specificationPojo.constraints) {
					for (Set<String> paramSet : pojo.parameters) {
						for (String param : paramSet) {
							this.tasks.add(param);
						}
					}
				}
			}
		} else {
			this.tasks.addAll(specificationPojo.tasks);
		}
		
		if (specificationPojo.constraints != null) {
			for (ConstraintPojo conPojo : specificationPojo.constraints) {
				if (conPojo != null) { // Seems trivial but it may happen if the list of constraints end with a comma in the JSON file
					this.constraints.add(new DeclareConstraintTransferObject(conPojo));
				}
			}
		}
	}
	
	public ProcessSpecificationTransferObject(ProcessSpecification proSpec) {
		this.name = proSpec.getName();
		this.tasks = new HashSet<String>(proSpec.howManyTasks(), (float)1.0);
		for (TaskChar taskChar: proSpec.getTasks()) {
			this.tasks.add(taskChar.getName());
		}
		this.constraints = new HashSet<DeclareConstraintTransferObject>(proSpec.howManyUnmarkedConstraints(), (float)1.0);
		for (Constraint con: proSpec.getAllUnmarkedConstraints()) {
			this.constraints.add(new DeclareConstraintTransferObject(con));
		}
	}
	
	public ProcessSpecificationPojo toPojo() {
		ProcessSpecificationPojo pojo = new ProcessSpecificationPojo();
		
		pojo.name = this.name;
		pojo.tasks = this.tasks;
		pojo.constraints = new HashSet<ConstraintPojo>(this.constraints.size(), (float)1.0);

		for (DeclareConstraintTransferObject conTO : this.constraints) {
			pojo.constraints.add(conTO.toPojo());
		}
		
		return pojo;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProcessModelTransferObject [name=");
		builder.append(name);
		builder.append(", tasks=");
		builder.append(tasks);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append("]");
		return builder.toString();
	}
}