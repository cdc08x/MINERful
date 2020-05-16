package minerful.io.encdec;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.pojo.ConstraintPojo;
import minerful.io.encdec.pojo.ProcessModelPojo;

public class ProcessModelTransferObject {
	public final String name;
	public final Set<String> tasks;
	public final Set<DeclareConstraintTransferObject> constraints;
	
	public ProcessModelTransferObject(ProcessModelPojo modelPojo) {
		this.name = modelPojo.name;
		this.constraints = new TreeSet<DeclareConstraintTransferObject>();
		this.tasks = new TreeSet<String>();
		if (modelPojo.tasks == null || modelPojo.tasks.size() == 0) {
			if (modelPojo.constraints.size() > 0) {
				for (ConstraintPojo pojo : modelPojo.constraints) {
					for (Set<String> paramSet : pojo.parameters) {
						for (String param : paramSet) {
							this.tasks.add(param);
						}
					}
				}
			}
		} else {
			this.tasks.addAll(modelPojo.tasks);
		}
		
		if (modelPojo.constraints != null) {
			for (ConstraintPojo conPojo : modelPojo.constraints) {
				this.constraints.add(new DeclareConstraintTransferObject(conPojo));
			}
		}
	}
	
	public ProcessModelTransferObject(ProcessModel proMod) {
		this.name = proMod.getName();
		this.tasks = new HashSet<String>(proMod.howManyTasks(), (float)1.0);
		for (TaskChar taskChar: proMod.getTasks()) {
			this.tasks.add(taskChar.getName());
		}
		this.constraints = new HashSet<DeclareConstraintTransferObject>(proMod.howManyUnmarkedConstraints(), (float)1.0);
		for (Constraint con: proMod.getAllUnmarkedConstraints()) {
			this.constraints.add(new DeclareConstraintTransferObject(con));
		}
	}
	
	public ProcessModelPojo toPojo() {
		ProcessModelPojo pojo = new ProcessModelPojo();
		
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