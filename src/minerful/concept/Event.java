package minerful.concept;

public class Event {
	public final TaskClass taskClass;

	public Event(TaskClass taskClass) {
		this.taskClass = taskClass;
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