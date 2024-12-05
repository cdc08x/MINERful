/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept;



import minerful.logparser.CharTaskClass;

/**
 * Class associating tasks to single-character identifiers and event classes as per the event log.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 *
 */

public class TaskChar implements Comparable<TaskChar> {
	/* Generic symbolic TaskChar's. */
	public static final TaskChar[] SYMBOLIC_TASKCHARS = new TaskChar[]{ 
			new TaskChar('0'), 
			new TaskChar('1'), 
			new TaskChar('2'), 
			new TaskChar('3'), 
			new TaskChar('4')
	};
	/**
	 * Used in {{@link #getNumericId getNumericId()}.
	 */
	public static final String TASK_NUM_PREFIX = "t";
	
	/**
	 * Character identifier for this event class.
	 */
	// This is due to the ridiculous bug of the Metro implementation of JAXB.
	// Without an Adapter, you would have an Integer encoding the Character as a result.
    public Character identifier;
	/**
	 * Event class mapper (can be depending on the XES event log, a pure string, etc.).
	 */
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
	 * Returns the string version (say, human-readable name) of {@link #taskClass this.taskClass}.
	 */
    public String getName() {
    	return this.taskClass.toString();
    }

	/**
	 * Returns a string version (though not human-readable) of this instance.
	 * In particular, it is the juxtaposition of {@link #TASK_NUM_PREFIX TASK_NUM_PREFIX} and the UTF-8 code of {@link #identifier this.identifier}.
	 */
    public String getTaskNumericId() {
    	return TASK_NUM_PREFIX.concat(String.valueOf((int)this.identifier));
    }

}