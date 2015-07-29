package minerful.logparser;

import java.util.Iterator;
import java.util.List;

import minerful.concept.TaskCharArchive;
import minerful.io.encdec.TaskCharEncoderDecoder;

public interface LogParser {
	int UNDEFINED_MAXIMUM_LENGTH = -1;
	int UNDEFINED_MINIMUM_LENGTH = Integer.MAX_VALUE;
	
	int length();
	int minimumTraceLength();
	int maximumTraceLength();

	Iterator<LogTraceParser> traceIterator();
	TaskCharEncoderDecoder getEventEncoderDecoder();
	LogEventClassifier getEventClassifier();
	int numberOfEvents();
	TaskCharArchive getTaskCharArchive();
	
	List<LogParser> split(Integer parts);
}