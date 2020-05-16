package minerful.logparser;

public class StringTraceParser extends AbstractTraceParser implements LogTraceParser {
	private String strTrace;
	StringLogParser strLogParser;
	private int currentIndex;
	private StringEventParser strEventParser;

	public StringTraceParser(String strTrace, StringLogParser strLogParser) {
		this.strTrace = strTrace;
		this.strLogParser = strLogParser;
		this.parsing = true;
		
		this.init();
	}

	@Override
	public LogParser getLogParser() {
		return this.strLogParser;
	}

	@Override
	public Character parseSubsequentAndEncode() {
		Character encodedEvent = null;
		if (stepToSubsequent()) {
			encodedEvent = strEventParser.evtIdentifier();
		}
		return encodedEvent;
	}

	@Override
	public LogEventParser parseSubsequent() {
		if (stepToSubsequent()) {
			return strEventParser;
		}
		return null;
	}

	@Override
	public boolean isParsingOver() {
		return (
			this.isParsing() &&
			(this.senseOfReading.equals(SenseOfReading.BACKWARDS) && this.currentIndex <= 0)
			||
			(this.senseOfReading.equals(SenseOfReading.ONWARDS) && this.currentIndex >= this.strTrace.length() -1));
	}

	@Override
	public boolean stepToSubsequent() {
		if (!isParsingOver()) {
			switch(this.senseOfReading) {
			case ONWARDS:
				this.currentIndex++;
				this.strEventParser = new StringEventParser(this, this.strTrace.charAt(currentIndex));
				break;
			case BACKWARDS:
				this.currentIndex--;
				this.strEventParser = new StringEventParser(this, this.strTrace.charAt(currentIndex));
				break;
			default:
				break;
			}
		} else {
			this.strEventParser = null;
			this.parsing = false;
		}
		return isParsing();
	}

	@Override
	public void init() {
		switch (this.getSenseOfReading()) {
		case BACKWARDS:
			this.currentIndex = strTrace.length();
			break;
		case ONWARDS:
		default:
			this.currentIndex = -1;
			break;
		}
		this.parsing = true;
	}

	@Override
	public int length() {
		return strTrace.length();
	}

	@Override
	public String encodeTrace() {
		return strTrace;
	}

	@Override
	public String printStringTrace() {
		return strTrace;
	}
	
	@Override
	public String getName() {
		return strTrace;
	}
}