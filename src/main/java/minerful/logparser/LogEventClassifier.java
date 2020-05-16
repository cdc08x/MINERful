package minerful.logparser;

import java.util.Collection;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;

public interface LogEventClassifier {
	public enum ClassificationType {
		NAME("name"),
		LOG_SPECIFIED("logspec");
		
		public final String type;

		private ClassificationType(String type) {
			this.type = type;
		}
	}

	ClassificationType getEventClassificationType();

	Collection<AbstractTaskClass> getTaskClasses();
}