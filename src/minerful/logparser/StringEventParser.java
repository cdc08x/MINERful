package minerful.logparser;

import minerful.concept.Event;
import minerful.concept.TaskClass;

public class StringEventParser implements LogEventParser {
	private StringTraceParser strTraceParser;
	public final Character strEvent;

	public StringEventParser(StringTraceParser stringTraceParser, Character strEvent) {
		this.strTraceParser = stringTraceParser;
		this.strEvent = strEvent;
	}

	@Override
	public Character evtIdentifier() {
		TaskClass logEventClass = this.strTraceParser.strLogParser.strEventClassifier.classify(strEvent);
		return this.strTraceParser.strLogParser.taChaEncoDeco.encode(logEventClass);
	}

	@Override
	public Event getEvent() {
		TaskClass logEventClass = this.strTraceParser.strLogParser.strEventClassifier.classify(strEvent);
		return new Event(logEventClass);
	}

}
