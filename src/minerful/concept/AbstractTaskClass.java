package minerful.concept;

public abstract class AbstractTaskClass implements TaskClass {

	@Override
	public int compareTo(TaskClass o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TaskClass) {
			return this.getName().equals(((TaskClass)o).getName());
		}
		else {
			return false;
		}
	}
}