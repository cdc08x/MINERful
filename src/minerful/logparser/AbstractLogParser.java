package minerful.logparser;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

public abstract class AbstractLogParser implements LogParser {

	private int minimumTraceLength = UNDEFINED_LENGTH;
	private int maximumTraceLength = UNDEFINED_LENGTH;
	private int numberOfEvents = UNDEFINED_LENGTH;
	protected TaskCharEncoderDecoder taChaEncoDeco;
	protected TaskCharArchive taskCharArchive;
    protected List<LogTraceParser> traceParsers;
	
	protected abstract Collection<TaskClass> parseLog(File logFile) throws Exception;

	protected void updateNumberOfEvents(int numberOfEvents) {
		this.numberOfEvents += numberOfEvents;
	}

	protected void updateMaximumTraceLength(int numberOfEvents) {
		if (numberOfEvents > this.maximumTraceLength) { this.maximumTraceLength = numberOfEvents; }
	}

	protected void updateMinimumTraceLength(int numberOfEvents) {
		if (numberOfEvents < this.minimumTraceLength) { this.minimumTraceLength = numberOfEvents; }
	}

	@Override
	public TaskCharEncoderDecoder getEventEncoderDecoder() {
		return this.taChaEncoDeco;
	}

	@Override
	public int minimumTraceLength() {
		return this.minimumTraceLength;
	}

	@Override
	public int maximumTraceLength() {
		return this.maximumTraceLength;
	}

	@Override
	public int numberOfEvents() {
		return this.numberOfEvents;
	}
	
	@Override
	public Iterator<LogTraceParser> traceIterator() {
		return this.traceParsers.listIterator(0);
	}

	@Override
	public int length() {
		return this.traceParsers.size();
	}

	protected void archiveTaskChars(Collection<TaskClass> classes) {
		this.taChaEncoDeco.encode(classes.toArray(new TaskClass[classes.size()]));
	    this.taskCharArchive = new TaskCharArchive(this.taChaEncoDeco.getTranslationMap());
	}

	@Override
	public TaskCharArchive getTaskCharArchive() {
		return this.taskCharArchive;
	}
}