package minerful.logparser;

import java.util.ListIterator;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class XesTraceParser extends AbstractTraceParser implements LogTraceParser {
	private XTrace xesTrace;
	private XEvent currentXesEvent;
	private final XesEventParser xesEventParser;

	protected XesLogParser xesLogParser;
	private ListIterator<XEvent> traceIterator;

	public XesTraceParser(XTrace xesTrace, XesLogParser xesLogParser) {
		this.xesTrace = xesTrace;
		this.xesLogParser = xesLogParser;
		this.traceIterator = xesTrace.listIterator();
		this.xesEventParser = new XesEventParser(this);
		this.parsing = true;
		this.currentXesEvent = null;
	}

	@Override
	public int length() {
		return this.xesTrace.size();
	}
	
	@Override
	public LogParser getLogParser() {
		return this.xesLogParser;
	}
	
	@Override
	public void init() {
		switch (this.getSenseOfReading()) {
		case BACKWARDS:
			this.traceIterator = xesTrace.listIterator(this.length());
			break;
		case ONWARDS:
		default:
			this.traceIterator = xesTrace.listIterator();
			break;
		}
		this.parsing = true;
	}
	
	@Override
	public boolean isParsingOver() {
		return (this.isParsing() &&
// For some unforeseeable reason, if this.traceIterator.previousIndex() == 0, this.traceIterator.hasPrevious() returns false, even though it is by all means WRONG. Is it a bug in Java 7.0?
				(this.senseOfReading.equals(SenseOfReading.BACKWARDS) && this.traceIterator.previousIndex() < 0)
				||
				(this.senseOfReading.equals(SenseOfReading.ONWARDS) && !this.traceIterator.hasNext()));
	}
	
	@Override
	public Character parseSubsequentAndEncode() {
		Character encodedEvent = null;
		if (stepToSubsequent()) {
			encodedEvent = xesEventParser.encode(this.currentXesEvent);
		}
		return encodedEvent;
	}

	@Override
	public boolean stepToSubsequent() {
		if (!isParsingOver()) {
			switch(this.senseOfReading) {
			case ONWARDS:
				this.currentXesEvent = this.traceIterator.next();
				break;
			case BACKWARDS:
				this.currentXesEvent = this.traceIterator.previous();
			default:
				break;
			}
		} else {
			this.currentXesEvent = null;
			this.parsing = false;
		}
		return isParsing();
	}
}