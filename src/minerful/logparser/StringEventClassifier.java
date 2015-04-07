package minerful.logparser;

import minerful.concept.TaskClass;


public class StringEventClassifier  extends AbstractLogEventClassifier implements LogEventClassifier {
	public StringEventClassifier(ClassificationType eventClassificationType) {
		super(eventClassificationType);
	}

	public TaskClass classify(Character chr) {
		return new CharTaskClass(chr);
	}
}
