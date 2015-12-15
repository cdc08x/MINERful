package minerful.logparser;

import minerful.concept.Event;
import minerful.concept.AbstractTaskClass;

import org.deckfour.xes.model.XEvent;

public class XesEventParser implements LogEventParser {
	private XesTraceParser xesTraceParser;
	public final XEvent xesEvent;
	
	public XesEventParser(XesTraceParser xesTraceParser, XEvent xesEvent) {
		this.xesTraceParser = xesTraceParser;
		this.xesEvent = xesEvent;
	}

	@Override
	public Character evtIdentifier() {
		AbstractTaskClass logEventClass = this.xesTraceParser.xesLogParser.xesEventClassifier.classify(xesEvent);
		return this.xesTraceParser.xesLogParser.taChaEncoDeco.encode(logEventClass);
	}
	
	public String getValue(String identifier) {
		return this.xesEvent.getAttributes().get(identifier).toString();
	}

	@Override
	public Event getEvent() {
		AbstractTaskClass logEventClass = this.xesTraceParser.xesLogParser.xesEventClassifier.classify(xesEvent);
		return new Event(logEventClass);
	}
}