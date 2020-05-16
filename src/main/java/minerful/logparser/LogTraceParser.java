package minerful.logparser;

public interface LogTraceParser {
	enum SenseOfReading {
		ONWARDS,
		BACKWARDS;
		
		public SenseOfReading switchSenseOfReading() {
			return (this.equals(ONWARDS) ? BACKWARDS : ONWARDS);
		}
	}

	SenseOfReading reverse();
	SenseOfReading getSenseOfReading();
	int length();
	LogParser getLogParser();
	boolean isParsing();
	LogEventParser parseSubsequent();
	Character parseSubsequentAndEncode();
	boolean isParsingOver();
	boolean stepToSubsequent();
	void init();
	String encodeTrace();
	String printStringTrace();
	String getName();
}