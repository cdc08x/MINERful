package minerful.logparser;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

public class StringTaskClass extends AbstractTaskClass implements TaskClass {
	public final String classString;

	public StringTaskClass(String classString) {
		this.classString = classString;
	}

	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof StringTaskClass)
			return this.classString.compareTo(((StringTaskClass) o).classString);
		else
			return super.compareTo(o);
	}

	@Override
	public String getName() {
		return classString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((classString == null) ? 0 : classString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		StringTaskClass other = (StringTaskClass) obj;
		if (classString == null) {
			if (other.classString != null)
				return false;
		} else if (!classString.equals(other.classString))
			return false;
		return true;
	}
}