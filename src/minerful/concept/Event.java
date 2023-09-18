package minerful.concept;

public class Event {
	public final AbstractTaskClass taskClass;

	public Event(AbstractTaskClass taskClass) {
		this.taskClass = taskClass;
	}
	
	public AbstractTaskClass getTaskClass() {
		return taskClass;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Event [taskClass=");
		builder.append(taskClass);
		builder.append("]");
		return builder.toString();
	}
}