package minerful.logparser;


public abstract class AbstractLogEventClassifier implements LogEventClassifier {
	protected final LogEventClassifier.ClassificationType eventClassificationType;

	public AbstractLogEventClassifier(LogEventClassifier.ClassificationType eventClassificationType) {
		this.eventClassificationType = eventClassificationType;
	}

	@Override
	public LogEventClassifier.ClassificationType getEventClassificationType() {
		return eventClassificationType;
	}
}