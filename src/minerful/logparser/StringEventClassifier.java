package minerful.logparser;


public class StringEventClassifier  extends AbstractLogEventClassifier implements LogEventClassifier {
	public StringEventClassifier(ClassificationType eventClassificationType) {
		super(eventClassificationType);
	}

	public String classify(Character chr) {
		return String.valueOf(chr);
	}
}
