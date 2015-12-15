package minerful.logparser;

import javax.xml.bind.annotation.XmlTransient;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

import org.deckfour.xes.classification.XEventClass;

public class XesTaskClass extends AbstractTaskClass implements TaskClass {
	@XmlTransient
	public XEventClass xEventClass;

	protected XesTaskClass() {
		super();
	}

	public XesTaskClass(XEventClass xEventClass) {
		this.xEventClass = xEventClass;
		super.setName(xEventClass.getId());
	}
	
	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof XesTaskClass) {
			return this.xEventClass.compareTo(((XesTaskClass) o).xEventClass);
		}
		else {
			return super.compareTo(o);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((xEventClass == null) ? 0 : xEventClass.hashCode());
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
		XesTaskClass other = (XesTaskClass) obj;
		if (xEventClass == null) {
			if (other.xEventClass != null)
				return false;
		} else if (!xEventClass.equals(other.xEventClass))
			return false;
		return true;
	}
}