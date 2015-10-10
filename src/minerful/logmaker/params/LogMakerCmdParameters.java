package minerful.logmaker.params;

import java.io.File;


public class LogMakerCmdParameters {
	public static enum Encoding {
		xes, mxml, string;
	}
	
	public Integer minEventsPerTrace;	// mandatory assignment
    public Integer maxEventsPerTrace;	// mandatory assignment
    public Long tracesInLog;	// mandatory assignment
    public File outputLogFile;
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