package minerful.logmaker.params;

import java.io.File;


public class LogMakerCmdParameters {
	/**
	 * Output encoding for the generated event log.
	 * It can be either XES ({@link http://www.xes-standard.org/openxes/start}),
	 * MXML ({@link http://www.processmining.org/logs/mxml}), or
	 * string-based (events are turned into characters and traces into strings).
	 */
	public static enum Encoding {
		/**
		 * XES ({@link http://www.xes-standard.org/openxes/start})
		 */
		xes,
		/**
		 * MXML ({@link http://www.processmining.org/logs/mxml})
		 */
		mxml,
		/**
		 * String-based (events are turned into characters and traces into strings)
		 */
		string;
	}
	
	/**
	 * Minimum number of events that have to be included in the generated traces.
	 */
	public Integer minEventsPerTrace;	// mandatory assignment
	/**
	 * Maximum number of events that have to be included in the generated traces.
	 */
    public Integer maxEventsPerTrace;	// mandatory assignment
	/**
	 * Number of traces in the log.
	 */
    public Long tracesInLog;	// mandatory assignment
    /**
     * File in which the generated event log is going to be stored.
     */
    public File outputLogFile;
    /**
     * Event log encoding (see {@link Encoding #Encoding}).
     */
    public LogMakerCmdParameters.Encoding outputEncoding;

	public LogMakerCmdParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog,
			File outputLogFile, Encoding outputEncoding) {
		this.minEventsPerTrace = minEventsPerTrace;
		this.maxEventsPerTrace = maxEventsPerTrace;
		this.tracesInLog = tracesInLog;
		this.outputLogFile = outputLogFile;
		this.outputEncoding = outputEncoding;
	}

	public LogMakerCmdParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog,
			Encoding outputEncoding) {
		this(minEventsPerTrace, maxEventsPerTrace, tracesInLog, null, outputEncoding);
	}

	public LogMakerCmdParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog) {
		this(minEventsPerTrace, maxEventsPerTrace, tracesInLog, null, null);
	}

	/**
	 * Checks that the assigned parameters are valid.
	 * @return <code>null</code> in case of valid parameters. A string describing the assignment errors otherwise.
	 */
	public String checkValidity() {
		StringBuilder checkFailures = new StringBuilder();

		// Mandatory assignments check
		if (minEventsPerTrace == null)
			checkFailures.append("Minimum number of events per trace unspecified\n");
		if (maxEventsPerTrace == null)
			checkFailures.append("Maximum number of events per trace unspecified\n");
		if (tracesInLog == null) {
			checkFailures.append("Number of traces in log unspecified\n");
		}
		
		if (checkFailures.length() > 0)
			return checkFailures.toString();
 
		// Correct assignments check
		if (minEventsPerTrace < 0)
			checkFailures.append("Negative minimum number of events per trace specified\n");
		if (maxEventsPerTrace < 0)
			checkFailures.append("Negative maximum number of events per trace specified\n");
		if (minEventsPerTrace > maxEventsPerTrace)
			checkFailures.append("Maximum number of events per trace are specified to be less than the minimum\n");
		if (tracesInLog < 0)
			checkFailures.append("Negative number of traces specified\n");
		if (outputLogFile != null && outputLogFile.isDirectory()) {
			checkFailures.append("Directory specified in place of a file to save the log\n");
		}
		
		if (checkFailures.length() > 0)
			return checkFailures.toString();
		
		return null;
	}
}