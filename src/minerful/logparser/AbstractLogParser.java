package minerful.logparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

public abstract class AbstractLogParser implements LogParser {

	private int minimumTraceLength = UNDEFINED_MINIMUM_LENGTH;
	private int maximumTraceLength = UNDEFINED_MAXIMUM_LENGTH;
	private int numberOfEvents = UNDEFINED_MAXIMUM_LENGTH;
	protected TaskCharEncoderDecoder taChaEncoDeco;
	protected TaskCharArchive taskCharArchive;
    protected List<LogTraceParser> traceParsers;
	

	protected AbstractLogParser(TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive, List<LogTraceParser> traceParsers) {
		this.taChaEncoDeco = taChaEncoDeco;
		this.taskCharArchive = taskCharArchive;
		this.traceParsers = traceParsers;
		for (LogTraceParser logTraceParser : traceParsers) {
        	updateMaximumTraceLength(logTraceParser.length());
        	updateMinimumTraceLength(logTraceParser.length());
        	updateNumberOfEvents(logTraceParser.length());
		}
	}
	
	public AbstractLogParser() {
	}

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
	
	protected abstract AbstractLogParser makeACopy(TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive, List<LogTraceParser> traceParsers);

	@Override
	public List<LogParser> split(Integer parts) {
		if (parts <= 0)
			throw new IllegalArgumentException("The log cannot be split in " + parts + " parts. Only positive integer values are allowed");

		int tracesPerSlice = this.traceParsers.size() / parts;
		
		List<LogParser> logParsers = new ArrayList<LogParser>(parts);
		List<LogTraceParser> auxTraceParsers = new ArrayList<LogTraceParser>(tracesPerSlice);
		List<List<LogTraceParser>> portions = new ArrayList<List<LogTraceParser>>();

		int
			traceRunner = 0,
			traceCounter = 0,
			traceParsersListCounter = 0;
		
		/*
		 * If you read this line, please feel free to curse me: Claudio Di Ciccio, cdc08x@gmail.com
		 */
		for (traceParsersListCounter = parts; traceParsersListCounter > 0; traceParsersListCounter--) {
			portions.add(new ArrayList<LogTraceParser>(tracesPerSlice));
		}

		auxTraceParsers = portions.get(traceParsersListCounter);
		
		for (; traceRunner < tracesPerSlice * parts; traceRunner++, traceCounter++) {
			if (traceCounter >= tracesPerSlice) {
				traceCounter = 0;
				traceParsersListCounter++;
				auxTraceParsers = portions.get(traceParsersListCounter);
			}
			auxTraceParsers.add(traceParsers.get(traceRunner));
		}
		for (; traceRunner < this.traceParsers.size(); traceRunner++) {
			auxTraceParsers.add(traceParsers.get(traceRunner));
		}
		for (List<LogTraceParser> portion : portions) {
			logParsers.add(
					this.makeACopy(taChaEncoDeco, taskCharArchive, portion)
			);
		}
		return logParsers;
	}
}