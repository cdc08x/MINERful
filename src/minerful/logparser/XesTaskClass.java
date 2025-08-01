package minerful.logparser;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.deckfour.xes.classification.XEventClass;

public class XesTaskClass extends AbstractTaskClass implements TaskClass, Serializable {
    private static final long serialVersionUID = 1L;

    // Transient - not serialized
    private transient XEventClass xEventClass;

    // Serializable fields to recreate xEventClass
    private String xEventClassId;
    private int xEventClassIndex;

    protected XesTaskClass() {
        super();
    }

    public XesTaskClass(XEventClass xEventClass) {
        this.xEventClass = xEventClass;
        this.xEventClassId = xEventClass.getId();
        this.xEventClassIndex = xEventClass.getIndex();
        super.setName(xEventClassId);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();  
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); 
        this.xEventClass = recreateXEventClassById(xEventClassId, xEventClassIndex);
        super.setName(xEventClassId);
    }

    private XEventClass recreateXEventClassById(String id, int index) {
        return new XEventClass(id, index);
    }

    @Override
    public int compareTo(TaskClass o) {
        if (o instanceof XesTaskClass) {
            return this.xEventClass.compareTo(((XesTaskClass) o).xEventClass);
        } else {
            return super.compareTo(o);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((xEventClass == null) ? 0 : xEventClass.hashCode());
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

    public XEventClass getXEventClass() {
        return xEventClass;
    }
}
