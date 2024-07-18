package minerful.logmaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Scanner;

import minerful.automaton.AutomatonRandomWalker;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.logmaker.params.LogMakerParameters;
import minerful.logparser.LogEventClassifier;
import minerful.logparser.XesLogParser;
import minerful.utils.MessagePrinter;
import minerful.concept.constraint.Constraint;


import org.deckfour.spex.SXTag;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XElement;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import dk.brics.automaton.Automaton;

/**
 * Generates a log out of a MINERful declarative process specification.
 * @author Claudio Di Ciccio
 */
public class MinerFulLogMaker {
	/**
	 * Log generation parameters
	 */
	private LogMakerParameters parameters;
	/**
	 * Event log
	 */
	private XLog log;
	/**
	 * Event log as strings
	 */
	private String[] stringsLog;

	private String[] strLog;

	private String legend;
	/**
	 * Maximum amount of traces we want to save as strings
	 */
	public static int MAX_SIZE_OF_STRINGS_LOG = Integer.MAX_VALUE;

	/**
	 * For debugging purposes
	 */
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulLogMaker.class);


	public MinerFulLogMaker(LogMakerParameters parameters) throws IllegalArgumentException {
		this.setParameters(parameters);
	}

	public void setParameters(LogMakerParameters parameters) {
		String errors = parameters.checkValidity();

		if (errors != null)
			throw new IllegalArgumentException(errors);

		this.parameters = parameters;

		this.stringsLog = new String[(parameters.tracesInLog < MAX_SIZE_OF_STRINGS_LOG ?
				Integer.parseInt(String.valueOf(parameters.tracesInLog)) :
					MAX_SIZE_OF_STRINGS_LOG)];

		this.strLog = new String[(parameters.tracesInLog < MAX_SIZE_OF_STRINGS_LOG ?
				Integer.parseInt(String.valueOf(parameters.tracesInLog)) :
					MAX_SIZE_OF_STRINGS_LOG)];
	}


	/**
	 * Generates an event log based on a MINERful process specification. To do so, it
	 * extracts an automaton out of the declarative process specification. Every finite
	 * random walk on it generates a trace. Every trace is included in the
	 * returned event log. The minimum and maximum length of the trace, as well
	 * as the number of traces to be generated, are specified in
	 * {@link #parameters parameters}.
	 * @param processSpecification The process specification that the generated event log complies to
	 * @return The generated event log
	 */
	public XLog createLog(ProcessSpecification processSpecification, ProcessSpecification negProcessSpecification) {
		XFactory xFactory = new XFactoryNaiveImpl();
		this.log = xFactory.createLog();

		XTrace xTrace = null;
		XEvent xEvent = null;
		XConceptExtension concExtino = XConceptExtension.instance();
		XLifecycleExtension lifeExtension = XLifecycleExtension.instance();
		XTimeExtension timeExtension = XTimeExtension.instance();
		this.log.getExtensions().add(concExtino);
		this.log.getExtensions().add(lifeExtension);
		this.log.getExtensions().add(timeExtension);
		this.log.getClassifiers().add(new XEventNameClassifier());

		concExtino.assignName(this.log,
				"Synthetic log for process: " + processSpecification.getName()
		);
		lifeExtension.assignModel(this.log, XLifecycleExtension.VALUE_MODEL_STANDARD);

		///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
		 String ncf = "";


		if (negProcessSpecification != null){
				for (Constraint con : negProcessSpecification.getAllConstraints()){
					ncf += con + ",";
				}
			}
		
		/////////////////////////////////////////////////////////////////////////////////////////////


		// Automaton automaton = processSpecification.buildAutomaton();
		// automaton = AutomatonUtils.limitRunLength(automaton, this.parameters.minEventsPerTrace, this.parameters.maxEventsPerTrace);

		// AutomatonRandomWalker walker = new AutomatonRandomWalker(automaton);

		///////////////////////////// modified by Ralph Angelo Almoneda ///////////////////////////////
		Automaton automaton = processSpecification.buildAutomaton(ncf);//this.parameters.negativeConstraints
		automaton = AutomatonUtils.limitRunLength(automaton, this.parameters.minEventsPerTrace, this.parameters.maxEventsPerTrace);
		AutomatonRandomWalker walker = new AutomatonRandomWalker(automaton);
		///////////////////////////////////////////////////////////////////////////////////////////////

		///////////////////////////// added by Ralph Angelo Almoneda ///////////////////////////////
		Automaton automatonPositive = processSpecification.buildAutomaton();
		automatonPositive = AutomatonUtils.limitRunLength(automatonPositive, this.parameters.minEventsPerTrace, this.parameters.maxEventsPerTrace);
		AutomatonRandomWalker walkerPositive = new AutomatonRandomWalker(automatonPositive);
		/////////////////////////////////////////////////////////////////////////////////////////////


		TaskChar firedTransition = null;
		Character pickedTransitionChar = 0;

		Date currentDate = null;
		int padder = (int)(Math.ceil(Math.log10(this.parameters.tracesInLog)));
		String traceNameTemplate = "Synthetic trace no. " + (padder < 1 ? "" : "%0" + padder) + "d";
		StringBuffer sBuf = new StringBuffer();
		StringBuffer stringBuf = new StringBuffer();

		legend = "# Legend:" + "\n" + "# " + processSpecification.getTaskCharArchive().getTranslationMapById().toString() + "\n";

		///////////////////////////// modified by Ralph Angelo Almoneda ///////////////////////////////
		for (int traceNum = 0; traceNum < this.parameters.tracesInLog - (int) (this.parameters.negativesInLog * (1)); traceNum++) {
			sBuf.append("<");
			walkerPositive.goToStart();
			xTrace = xFactory.createTrace();
			concExtino.assignName(
					xTrace,
					String.format(traceNameTemplate, (traceNum))
			);

			pickedTransitionChar = walkerPositive.walkOn();
			while (pickedTransitionChar != null) {
				firedTransition = processSpecification.getTaskCharArchive().getTaskChar(pickedTransitionChar);
				if (traceNum < this.parameters.tracesInLog-(int) (this.parameters.negativesInLog * (1))) {
					stringBuf.append(pickedTransitionChar);
					sBuf.append(pickedTransitionChar + "=" + firedTransition + ";");
				}

				currentDate = generateRandomDateTimeForLogEvent(currentDate);
				xEvent = makeXEvent(xFactory, concExtino, lifeExtension, timeExtension, firedTransition, currentDate);
				xTrace.add(xEvent);
				pickedTransitionChar = walkerPositive.walkOn();
			}
			this.log.add(xTrace);
			if (traceNum < this.parameters.tracesInLog-(int) (this.parameters.negativesInLog * (1))) {
				this.stringsLog[traceNum] = sBuf.substring(0, Math.max(1, sBuf.length() -1)) + ">";
				this.strLog[traceNum] = stringBuf.substring(0, Math.max(1, stringBuf.length() -1));
				sBuf = new StringBuffer();
				stringBuf = new StringBuffer();
			}
		}
		for (int traceNum = (int) (this.parameters.tracesInLog - (int) (this.parameters.negativesInLog * (1))); traceNum < this.parameters.tracesInLog; traceNum++) {
			sBuf.append("<");
			walker.goToStart();
			xTrace = xFactory.createTrace();
			concExtino.assignName(
					xTrace,
					String.format(traceNameTemplate, (traceNum))
				);

			pickedTransitionChar = walker.walkOn();
			while (pickedTransitionChar != null) {
				firedTransition = processSpecification.getTaskCharArchive().getTaskChar(pickedTransitionChar);
				if (traceNum < MAX_SIZE_OF_STRINGS_LOG) {
					stringBuf.append(pickedTransitionChar);
					sBuf.append(pickedTransitionChar + "=" + firedTransition + ";");
				}

				currentDate = generateRandomDateTimeForLogEvent(currentDate);
				xEvent = makeXEvent(xFactory, concExtino, lifeExtension, timeExtension, firedTransition, currentDate);
				xTrace.add(xEvent);
				pickedTransitionChar = walker.walkOn();
			}
			this.log.add(xTrace);
			if (traceNum < MAX_SIZE_OF_STRINGS_LOG) {
				this.stringsLog[traceNum] = sBuf.substring(0, Math.max(1, sBuf.length() -1)) + ">";
				this.strLog[traceNum] = stringBuf.substring(0, Math.max(1, stringBuf.length() -1));
				sBuf = new StringBuffer();
				stringBuf = new StringBuffer();
			}
		}


		return this.log;
		///////////////////////////////////////////////////////////////////////////////////////////////
	}


	/**
	 * Stores the generated event log, {@link #log log}, in the file specified in
	 * {@link #parameters parameters}.
	 * @return The file in which the event log has been stored
	 * @throws IOException
	 */
	public File storeLog() throws IOException {
		checkParametersForLogEncoding();
		if (this.parameters.outputLogFile == null)
			throw new IllegalStateException("Output file not specified in given parameters");
		File outFile = this.parameters.outputLogFile;
		OutputStream outStream = new FileOutputStream(outFile);
		this.printEncodedLogInStream(outStream);
		outStream.flush();
		outStream.close();
		return outFile;
	}

	/**
	 * Prints the generated event log, {@link #log log}.
	 * @return The print-out of the event log
	 * @throws IOException
	 */
	public String printEncodedLog() throws IOException {
		checkParametersForLogEncoding();
		OutputStream outStream = new ByteArrayOutputStream();
		this.printEncodedLogInStream(outStream);
		outStream.flush();
		outStream.close();
		return outStream.toString();
	}

	/**
	 * Prints the generated event log, {@link #log log}, in the specified output stream.
	 * @return The print-out of the event log
	 * @throws IOException
	 */
	private boolean printEncodedLogInStream(OutputStream outStream) throws IOException {
		switch(this.parameters.outputEncoding) {
		case xes:
			new XesXmlSerializer().serialize(this.log, outStream);
			break;
		case mxml:
			new XMxmlSerializer().serialize(this.log, outStream);
			break;
		case strings:
			String legendname = this.parameters.outputLogFile.toString().substring(0, this.parameters.outputLogFile.toString().length() - 4) + "_legend.txt";
			File legendFile = new File(legendname);
			OutputStream legendStream = new FileOutputStream(legendFile);
			PrintWriter priLegend = new PrintWriter(legendStream);
			PrintWriter priWri = new PrintWriter(outStream);
			for (String stringTrace : this.strLog) {
				priWri.println(stringTrace);
			}
			priLegend.println(this.legend);
			for (String legendTrace : this.stringsLog) {
				priLegend.println(legendTrace);
			}
			MessagePrinter.printlnOut("log in file '"+this.parameters.outputLogFile+"' created successfully with legend in '"+legendname+"'");

			priWri.flush();
			priWri.close();
			priLegend.flush();
			priLegend.close();
			break;
		default:
			outStream.flush();
			outStream.close();
			throw new UnsupportedOperationException("Support for this encoding is still work-in-progress");
		}
		return true;
	}

	/**
	 * Checks that {@link #parameters parameters} and {@link #log log} are in a
	 * correct state for generating the event log. In case the check fails, an
	 * exception is fired.
	 * @throws IllegalArgumentException
	 */
	private void checkParametersForLogEncoding() throws IllegalArgumentException {
		if (this.log == null)
			throw new IllegalStateException("Log not yet generated");
		if (this.parameters.outputEncoding == null)
			throw new IllegalStateException("Output encoding not specified in given parameters");
	}

	/**
	 * Creates an event for the event log
	 */
	private XEvent makeXEvent(XFactory xFactory, XConceptExtension concExtino,
			XLifecycleExtension lifeExtension, XTimeExtension timeExtension,
			TaskChar firedTransition, Date currentDate) {
		XEvent xEvent = xFactory.createEvent();
		concExtino.assignName(xEvent, firedTransition.toString());
		lifeExtension.assignStandardTransition(xEvent, XLifecycleExtension.StandardModel.COMPLETE);
		timeExtension.assignTimestamp(xEvent, currentDate);
		return xEvent;
	}

	/**
	 * Generates a random date and time for a log event.
	 * @return A random date and time for the log event.
	 */
	private Date generateRandomDateTimeForLogEvent() {
		return generateRandomDateTimeForLogEvent(null);
	}

	/**
	 * Generates a random date and time for a log event, no sooner than the
	 * provided parameter.
	 *
	 * @param laterThan The date and time with respect to which the generated time stamp must be later
	 * @return A random date and time for the log event
	 */
	private Date generateRandomDateTimeForLogEvent(Date laterThan) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		if (laterThan == null) {
			cal.add(GregorianCalendar.YEAR, -1);
			cal.add(GregorianCalendar.MONTH, (int) ( Math.round(Math.random() * 12 )) * -1 );
			cal.add(GregorianCalendar.WEEK_OF_MONTH, (int) ( Math.round(Math.random() * 4  )) * -1 );
			cal.add(GregorianCalendar.DAY_OF_WEEK, (int) ( Math.round(Math.random() * 7  )) * -1 );
			laterThan = cal.getTime();
		}

		long
			randomAdditionalTime = (long) (Math.round(Math.random() * TimeUnit.DAYS.toMillis(1)));
		cal.setTimeInMillis(laterThan.getTime() + randomAdditionalTime);
		return cal.getTime();
	}
}