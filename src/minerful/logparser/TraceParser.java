package minerful.logparser;

public interface TraceParser {
	enum SenseOfReading {
		ONWARDS,
		BACKWARDS
	}

	SenseOfReading reverse();

	int length();

	LogEventParser next();

}