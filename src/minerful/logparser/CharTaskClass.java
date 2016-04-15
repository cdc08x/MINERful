package minerful.logparser;

import javax.xml.bind.annotation.XmlType;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

@XmlType
public class CharTaskClass extends AbstractTaskClass implements TaskClass {
	public final Character charClass;

	public CharTaskClass(Character charClass) {
		this.charClass = charClass;
	}

	@Override
	public int compareTo(TaskClass o) {
		if (o instanceof CharTaskClass)
			return this.charClass.compareTo(((CharTaskClass) o).charClass);
		else
			return super.compareTo(o);
	}

	@Override
	public String getName() {
		return charClass.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((charClass == null) ? 0 : charClass.hashCode());
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
		CharTaskClass other = (CharTaskClass) obj;
		if (charClass == null) {
			if (other.charClass != null)
				return false;
		} else if (!charClass.equals(other.charClass))
			return false;
		return true;
	}
}