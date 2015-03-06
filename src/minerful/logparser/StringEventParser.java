package minerful.logparser;

public class StringEventParser implements LogEventParser {
	private StringTraceParser strTraceParser;

	public StringEventParser(StringTraceParser stringTraceParser) {
		this.strTraceParser = stringTraceParser;
	}

	public Character encode(Character stringEvent) {
		String logEventClass = this.strTraceParser.strLogParser.strEventClassifier.classify(stringEvent);
		return this.strTraceParser.strLogParser.taChaEncoDeco.encode(logEventClass);
	}

}
