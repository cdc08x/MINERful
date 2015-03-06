package minerful.logparser;

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
}