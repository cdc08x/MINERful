package minerful.logparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskCharArchive;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.utils.MessagePrinter;

public abstract class AbstractLogParser implements LogParser {
	public static MessagePrinter logger = MessagePrinter.getInstance(AbstractLogParser.class);
			
	private int minimumTraceLength = UNDEFINED_MINIMUM_LENGTH;
	private int maximumTraceLength = UNDEFINED_MAXIMUM_LENGTH;
	private int numberOfEvents = UNDEFINED_MAXIMUM_LENGTH;
	protected TaskCharEncoderDecoder taChaEncoDeco;
	protected TaskCharArchive taskCharArchive;
	
	protected Integer startingTrace;
	protected Integer subLogLength;

	protected List<LogTraceParser> traceParsers;
	/**
	 * Differently than {@link #traceParsers this.traceParser}, this attribute
	 * does not consider all traces but just those that we want to make visible
	 * to the miner.
	 * This decision will be affected by
	 * {@link #startingTrace this.startingTrace}
	 * and
	 * {@link #subLogLength this.subLogLength}.
	 */
	protected List<LogTraceParser> navigableTraceParsers;
	
	protected AbstractLogParser(TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive,
			List<LogTraceParser> traceParsers,
			Integer startingTrace,
			Integer subLogLength) {
		this.taChaEncoDeco = taChaEncoDeco;
		this.taskCharArchive = taskCharArchive;
		this.traceParsers = traceParsers;
		init(startingTrace, subLogLength);

		this.postInit();
	}

	protected void init(Integer startingTrace, Integer subLogLength) {
		if (subLogLength < 0) {
			throw new IllegalArgumentException("The length of the sub-log should be a positive integer!");
		}
		if (startingTrace < 0) {
			throw new IllegalArgumentException("The initial trace number should be a positive integer!");
		}

		this.startingTrace = startingTrace;
		this.subLogLength = subLogLength;
	}

	protected void postInit() {
		this.setUpNavigableTraceParsers();
		this.updateLogStats();
	}

	protected void updateLogStats() {
		if (this.navigableTraceParsers == null)
			throw new IllegalStateException("You should invoke AbstractLogParser.setUpNavigableTraceParsers() before AbstractLogParser.updateLogStats()");

		for (LogTraceParser logTraceParser : this.navigableTraceParsers) {
			updateMaximumTraceLength(logTraceParser.length());
			updateMinimumTraceLength(logTraceParser.length());
			updateNumberOfEvents(logTraceParser.length());
		}
	}

	protected void setUpNavigableTraceParsers() {
		if (this.startingTrace >= this.wholeLength()) {
			logger.warn("The given starting trace number (" + this.startingTrace + ") is higher than the size of the event log (" + this.wholeLength() + "). Restoring it to default (0)");
			this.startingTrace = 0;
		}
		if (this.subLogLength > this.wholeLength() - this.startingTrace) {
			logger.warn("The given length of the sub-log (" + this.subLogLength + ") is too high. Changing its value to the maximum possible value");
			this.subLogLength = this.wholeLength() - this.startingTrace;
		}
		
		if (this.subLogLength > 0 || this.startingTrace > 0) {
			int 
				i = 0,
				actualLength = Math.min(
						this.subLogLength,
						this.wholeLength() - this.startingTrace);
			
			this.navigableTraceParsers =
				new ArrayList<LogTraceParser>(actualLength);
			Iterator<LogTraceParser> parsers =
					this.traceParsers.listIterator(this.startingTrace);
			while (parsers.hasNext() && i < actualLength) {
				this.navigableTraceParsers.add(i++, parsers.next());
			}
		} else {
			this.navigableTraceParsers = this.traceParsers;
		}
	}
	
	protected AbstractLogParser() {
	}

	protected abstract Collection<AbstractTaskClass> parseLog(File logFile) throws Exception;

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
		return this.navigableTraceParsers.listIterator(0);
	}

	@Override
	public int length() {
		return this.navigableTraceParsers.size();
	}

	@Override
	public int wholeLength() {
		return this.traceParsers.size();
	}

	protected void archiveTaskChars(Collection<AbstractTaskClass> classes) {
		this.taChaEncoDeco.encode(classes.toArray(new AbstractTaskClass[classes.size()]));
	    this.taskCharArchive = new TaskCharArchive(this.taChaEncoDeco.getTranslationMap());
	}

	@Override
	public TaskCharArchive getTaskCharArchive() {
		return this.taskCharArchive;
	}
	
	protected abstract AbstractLogParser makeACopy(
			TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive,
			List<LogTraceParser> navigableTraceParsers,
			Integer startingTrace,
			Integer subLogLength);
	
	@Override
	public List<LogParser> split(Integer parts) {
		if (parts <= 0)
			throw new IllegalArgumentException("The log cannot be split in " + parts + " parts. Only positive integer values are allowed");

		int tracesPerSlice = this.navigableTraceParsers.size() / parts;
		
		List<LogParser> logParsers = new ArrayList<LogParser>(parts);
		List<LogTraceParser> auxTraceParsers = new ArrayList<LogTraceParser>(tracesPerSlice);
		List<List<LogTraceParser>> portions = new ArrayList<List<LogTraceParser>>();

		int
			traceRunner = 0,
			traceCounter = 0,
			traceParsersListCounter = 0;
		
		/*
		 * If you read this line, and feel the urge to curse me, I cannot blame you.
		 * Yours, Claudio Di Ciccio (dc.claudio@gmail.com)
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
			auxTraceParsers.add(navigableTraceParsers.get(traceRunner));
		}
		for (; traceRunner < this.navigableTraceParsers.size(); traceRunner++) {
			auxTraceParsers.add(navigableTraceParsers.get(traceRunner));
		}
		for (List<LogTraceParser> portion : portions) {
			logParsers.add(
					this.makeACopy(
							taChaEncoDeco,
							taskCharArchive,
							portion,
							0,
							0)
			);
		}
		return logParsers;
	}
	
	@Override
	public LogParser takeASlice(Integer from, Integer length) {
		return this.makeACopy(taChaEncoDeco, taskCharArchive, traceParsers, from, length);
	}

	@Override
	public void excludeTasksByName(Collection<String> tasksToExcludeFromResult) {
		Collection<AbstractTaskClass> taskClassesToExclude = getEventEncoderDecoder().excludeThese(tasksToExcludeFromResult);
		this.taskCharArchive.removeAllByClass(taskClassesToExclude);
	}
}