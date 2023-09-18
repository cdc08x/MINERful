package minerful.logparser;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

public class StringTaskClass extends AbstractTaskClass implements TaskClass {
	public static StringTaskClass WILD_CARD = new StringTaskClass(TaskCharEncoderDecoder.WILDCARD_STRING);

	protected StringTaskClass() {
		super();
	}

	public StringTaskClass(String classString) {
		this.className = classString;
	}

	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof StringTaskClass)
			return this.className.compareTo(((StringTaskClass) o).className);
		else
			return super.compareTo(o);
	}

	@Override
	public String getName() {
		return className;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
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
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		return true;
	}
}