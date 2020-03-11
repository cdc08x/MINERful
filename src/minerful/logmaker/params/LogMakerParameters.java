package minerful.logmaker.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.params.ParamsManager;
import minerful.stringsmaker.params.StringTracesMakerCmdParameters;
import minerful.utils.MessagePrinter;


public class LogMakerParameters extends ParamsManager {
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
		xes, // default
		/**
		 * MXML ({@link http://www.processmining.org/logs/mxml})
		 */
		mxml,
		/**
		 * String-based (events are turned into characters and traces into strings)
		 */
		strings;
	}
	
	public static final String OUTPUT_FILE_PARAM_NAME = "oLF";
    public static final String OUT_ENC_PARAM_NAME = "oLE";
	public static final String SIZE_PARAM_NAME = "oLL";
	public static final String MAX_LEN_PARAM_NAME = "oLM";
	public static final String MIN_LEN_PARAM_NAME = "oLm";

    public static final Long DEFAULT_SIZE = 100L;
    public static final Integer DEFAULT_MIN_TRACE_LENGTH = 0;
    public static final Integer DEFAULT_MAX_TRACE_LENGTH = 100;
	public static final Encoding DEFAULT_OUTPUT_ENCODING = Encoding.xes;
    
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
    public LogMakerParameters.Encoding outputEncoding;
    
    public LogMakerParameters () {
    	this(DEFAULT_MIN_TRACE_LENGTH, DEFAULT_MAX_TRACE_LENGTH, DEFAULT_SIZE, null, DEFAULT_OUTPUT_ENCODING);
    }

	public LogMakerParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog,
			File outputLogFile, Encoding outputEncoding) {
    	super();
		this.minEventsPerTrace = minEventsPerTrace;
		this.maxEventsPerTrace = maxEventsPerTrace;
		this.tracesInLog = tracesInLog;
		this.outputLogFile = outputLogFile;
		this.outputEncoding = outputEncoding;
	}

	public LogMakerParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog,
			Encoding outputEncoding) {
		this(minEventsPerTrace, maxEventsPerTrace, tracesInLog, null, outputEncoding);
	}

	public LogMakerParameters(
			Integer minEventsPerTrace, Integer maxEventsPerTrace, Long tracesInLog) {
		this(minEventsPerTrace, maxEventsPerTrace, tracesInLog, null, null);
	}


    public LogMakerParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public LogMakerParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}
	
	@Override
	protected void setup(CommandLine line) {
        this.minEventsPerTrace =
        		Integer.valueOf(
        				line.getOptionValue(MIN_LEN_PARAM_NAME, this.minEventsPerTrace.toString()));
        this.maxEventsPerTrace =
        		Integer.valueOf(
        				line.getOptionValue(MAX_LEN_PARAM_NAME, this.maxEventsPerTrace.toString()));
        this.tracesInLog =
        		Long.valueOf(line.getOptionValue(SIZE_PARAM_NAME, this.tracesInLog.toString()));
        this.outputEncoding = Encoding.valueOf(
        		line.getOptionValue(OUT_ENC_PARAM_NAME, this.outputEncoding.toString())
		);
       	this.outputLogFile = openOutputFile(line, OUTPUT_FILE_PARAM_NAME);
	}
    
	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
         options.addOption(
                Option.builder(MIN_LEN_PARAM_NAME)
						.hasArg().argName("min-length")
						.longOpt("minlen")
						.desc("minimum length of the generated traces. It must be greater than or equal to 0"
						+ printDefault(DEFAULT_MIN_TRACE_LENGTH))
						.type(Integer.class)
						.build()
        );
        options.addOption(
                Option.builder(MAX_LEN_PARAM_NAME)
						.hasArg().argName("max-length")
						.longOpt("maxlen")
						.desc("maximum length of the generated traces. It must be greater than or equal to 0"
						+ printDefault(DEFAULT_MAX_TRACE_LENGTH))
						.type(Integer.class)
						.build()
        );
        options.addOption(
                Option.builder(SIZE_PARAM_NAME)
						.hasArg().argName("number of traces")
						.longOpt("size")
						.desc("number of traces to simulate"
						+ printDefault(DEFAULT_SIZE))
						.type(Long.class)
						.build()
        );
        options.addOption(
                Option.builder(OUT_ENC_PARAM_NAME)
						.hasArg().argName("language")
						.longOpt("out-log-encoding")
						.desc("encoding language for the output log " + printValues(LogMakerParameters.Encoding.values())
						+ printDefault(fromEnumValueToString(DEFAULT_OUTPUT_ENCODING)))
						.type(String.class)
						.build()
    	);
       options.addOption(
                Option.builder(OUTPUT_FILE_PARAM_NAME)
						.hasArg().argName("file path")
						.longOpt("out-log-file")
						.desc("path of the file in which the log should be written")
						.type(String.class)
						.build()
    	);
        
        return options;
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