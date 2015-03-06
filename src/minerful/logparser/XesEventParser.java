package minerful.logparser;

import org.deckfour.xes.model.XEvent;

public class XesEventParser implements LogEventParser {
	private XesTraceParser xesTraceParser;
	
	public XesEventParser(XesTraceParser xesTraceParser) {
		this.xesTraceParser = xesTraceParser;
	}

	public Character encode(XEvent xesEvent) {
		String logEventClass = this.xesTraceParser.xesLogParser.xesEventClassifier.classify(xesEvent);
		return this.xesTraceParser.xesLogParser.taChaEncoDeco.encode(logEventClass);
	}
}
