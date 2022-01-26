package minerful.concept;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractTaskClass implements TaskClass {
	public String className;

	protected AbstractTaskClass() {}
	
	@Override
	public String getName() {
		return this.className;
	}

	@Override
	public void setName(String className) {
		this.className = className;
	}
	
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