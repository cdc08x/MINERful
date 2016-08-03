/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.xmlenc.CharAdapter;
import minerful.logparser.CharTaskClass;

import minerful.concept.xmlenc.TaskClassAdapter;

@XmlRootElement(name="task")
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskChar implements Comparable<TaskChar> {
	@XmlAttribute
	@XmlJavaTypeAdapter(value=CharAdapter.class)
    public Character identifier;
	@XmlAttribute
	@XmlJavaTypeAdapter(value=TaskClassAdapter.class)
    public final AbstractTaskClass taskClass;
    
	protected TaskChar() {
		this.identifier = null;
		this.taskClass = null;
	}
	
    public TaskChar(Character identifier) {
        this(identifier, new CharTaskClass(identifier));
    }

    public TaskChar(Character identifier, AbstractTaskClass taskClass) {
    	this.identifier = identifier;
    	this.taskClass = taskClass;
	}

	/**
	 * Returns {@link #getName() this.getName()}.
	 */
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

	/**
	 * Returns the string version of {@link #taskClass this.taskClass}.
	 */
    public String getName() {
    	return this.taskClass.toString();
    }

}