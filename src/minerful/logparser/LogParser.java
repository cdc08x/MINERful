package minerful.logparser;

import java.util.Iterator;

import minerful.concept.TaskCharArchive;
import minerful.io.encdec.TaskCharEncoderDecoder;

public interface LogParser {
	int UNDEFINED_LENGTH = -1;
	
	int length();
	int minimumTraceLength();
	int maximumTraceLength();

	Iterator<LogTraceParser> traceIterator();
	TaskCharEncoderDecoder getEventEncoderDecoder();
	LogEventClassifier getEventClassifier();
	int numberOfEvents();
	TaskCharArchive getTaskCharArchive();
}