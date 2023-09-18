package minerful.io.encdec.pojo;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class ProcessModelPojo {
	public String name;
	public Set<String> tasks;
	public Set<ConstraintPojo> constraints;
	
	public ProcessModelPojo() {
		this.tasks = new TreeSet<String>();
		this.constraints = new TreeSet<ConstraintPojo>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constraints == null) ? 0 : constraints.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessModelPojo other = (ProcessModelPojo) obj;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else {
			if (constraints.size() != other.constraints.size()) {
				return false;
			}
			Iterator<ConstraintPojo>
				thisCnsIt = constraints.iterator(),
				otherCnsIt = other.constraints.iterator();
			while (thisCnsIt.hasNext() && otherCnsIt.hasNext()) {
				if (!thisCnsIt.next().equals(otherCnsIt.next())) {
					return false;
				}
			}
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else {
			if (tasks.size() != other.tasks.size()) {
				return false;
			}
			Iterator<String>
				thisCnsIt = tasks.iterator(),
				otherCnsIt = other.tasks.iterator();
			while (thisCnsIt.hasNext() && otherCnsIt.hasNext()) {
				if (!thisCnsIt.next().equals(otherCnsIt.next())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProcessModelPojo [name=");
		builder.append(name);
		builder.append(", tasks=");
		builder.append(tasks);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append("]");
		return builder.toString();
	}
}