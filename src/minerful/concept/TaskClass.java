package minerful.concept;

import javax.xml.bind.annotation.XmlElement;

public interface TaskClass extends Comparable<TaskClass> {
	@XmlElement
	public String getName();
}