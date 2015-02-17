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

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskChar implements Comparable<TaskChar> {
	@XmlAttribute
	@XmlJavaTypeAdapter(value=CharAdapter.class)
    public final Character identifier;
	@XmlAttribute
    public String name;
    
	protected TaskChar() {
		this.identifier = null;
	}
	
    public TaskChar(Character identifier) {
        this.identifier = identifier;
        this.name = this.identifier.toString();
    }

    public TaskChar(Character identifier, String name) {
    	this.identifier = identifier;
		this.name = name;
	}

	@Override
    public String toString() {
        return this.name.toString();
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
    public int compareTo(TaskChar t) {
        return this.identifier.compareTo(t.identifier);
    }

    
}