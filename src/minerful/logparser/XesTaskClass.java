package minerful.logparser;

import org.deckfour.xes.classification.XEventClass;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

public class XesTaskClass extends AbstractTaskClass implements TaskClass {
	public final XEventClass xEventClass;

	public XesTaskClass(XEventClass xEventClass) {
		this.xEventClass = xEventClass;
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
	public String getName() {
		return xEventClass.getId();
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