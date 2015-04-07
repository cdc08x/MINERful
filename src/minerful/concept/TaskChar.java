/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.xmlenc.CharAdapter;
import minerful.logparser.CharTaskClass;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskChar implements Comparable<TaskChar> {
	@XmlAttribute
	@XmlJavaTypeAdapter(value=CharAdapter.class)
    public final Character identifier;
	@XmlTransient
    public final TaskClass taskClass;
    
	protected TaskChar() {
		this.identifier = null;
		this.taskClass = null;
	}
	
    public TaskChar(Character identifier) {
        this(identifier, new CharTaskClass(identifier));
    }

    public TaskChar(Character identifier, TaskClass taskClass) {
    	this.identifier = identifier;
    	this.taskClass = taskClass;
	}

	@Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TaskChar other = (TaskChar) obj;
        if ((this.identifier == null) ? (other.identifier != null) : !this.identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }
    
    @Override
	public int hashCode() {
		return this.taskClass.hashCode();
	}

	@Override
    public int compareTo(TaskChar t) {
        return this.identifier.compareTo(t.identifier);
    }

    @XmlAttribute(name="name")
    public String getName() {
    	return this.taskClass.toString();
    }
}