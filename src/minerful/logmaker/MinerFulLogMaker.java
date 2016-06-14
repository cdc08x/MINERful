package minerful.logmaker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import minerful.automaton.AutomatonRandomWalker;
import minerful.automaton.utils.AutomatonUtils;
import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;
import minerful.logmaker.params.LogMakerCmdParameters;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;

public class MinerFulLogMaker {
	private LogMakerCmdParameters parameters;
	private XLog log;

	public MinerFulLogMaker(LogMakerCmdParameters parameters) throws IllegalArgumentException {
		this.setParameters(parameters);
	}

	public void setParameters(LogMakerCmdParameters parameters) {
		String errors = parameters.checkValidity();
		
		if (errors != null)
			throw new IllegalArgumentException(errors);

		this.parameters = parameters;
	}

	public XLog createLog(AssignmentModel declareMapModel) {
		this.log = createLog(DeclareMapEncoderDecoder.fromDeclareMapToMinerfulProcessModel(declareMapModel));
		return this.log;
	}

	public XLog createLog(ProcessModel processModel) {
		XFactory xFactory = new XFactoryBufferedImpl();
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

		concExtino.assignName(this.log, "Synthetic log for process: " + processModel.getName());
		lifeExtension.assignModel(this.log, XLifecycleExtension.VALUE_MODEL_STANDARD);
		
		Automaton automaton = processModel.buildAutomaton();
		automaton = AutomatonUtils.limitRunLength(automaton, this.parameters.minEventsPerTrace, this.parameters.maxEventsPerTrace);

		AutomatonRandomWalker walker = new AutomatonRandomWalker(automaton);
		
		TaskChar firedTransition = null;
		Character pickedTransitionChar = 0;
		
		Date currentDate = null;
		int padder = (int)(Math.ceil(Math.log10(this.parameters.tracesInLog)));
		String traceNameTemplate = "Synthetic trace no. " + (padder < 1 ? "" : "%0" + padder) + "d";

		for (int traceNum = 0; traceNum < this.parameters.tracesInLog; traceNum++) {
			walker.goToStart();
			xTrace = xFactory.createTrace();
			concExtino.assignName(
					xTrace,
					String.format(traceNameTemplate, (traceNum))
				);

			pickedTransitionChar = walker.walkOn();
			while (pickedTransitionChar != null) {
				firedTransition = processModel.getTaskCharArchive().getTaskChar(pickedTransitionChar);
//System.out.print(firedTransition + ",");
				
				currentDate = generateRandomDateTimeForLogEvent(currentDate);
				xEvent = makeXEvent(xFactory, concExtino, lifeExtension, timeExtension, firedTransition, currentDate);
				xTrace.add(xEvent);
				pickedTransitionChar = walker.walkOn();
			}
			this.log.add(xTrace);
//System.out.println();
		}
		
		return this.log;
	}
	
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

	public String printEncodedLog() throws IOException {
		checkParametersForLogEncoding();
		OutputStream outStream = new ByteArrayOutputStream();
		this.printEncodedLogInStream(outStream);
		outStream.flush();
		outStream.close();
		return outStream.toString();
	}

	private boolean printEncodedLogInStream(OutputStream outStream) throws IOException {
		switch(this.parameters.outputEncoding) {
		case xes:
			new XesXmlSerializer().serialize(this.log, outStream);
			break;
		case mxml:
			new XMxmlSerializer().serialize(this.log, outStream);
			break;
		case string:
		default:
			outStream.flush();
			outStream.close();
			throw new UnsupportedOperationException("Support for this encoding is still work-in-progress");
		}
		return true;
	}

	private void checkParametersForLogEncoding() {
		if (this.log == null)
			throw new IllegalStateException("Log not yet generated");
		if (this.parameters.outputEncoding == null)
			throw new IllegalStateException("Output encoding not specified in given parameters");
	}

	private XEvent makeXEvent(XFactory xFactory, XConceptExtension concExtino,
			XLifecycleExtension lifeExtension, XTimeExtension timeExtension,
			TaskChar firedTransition, Date currentDate) {
		XEvent xEvent = xFactory.createEvent();
		concExtino.assignName(xEvent, firedTransition.toString());
		lifeExtension.assignStandardTransition(xEvent, XLifecycleExtension.StandardModel.COMPLETE);
		timeExtension.assignTimestamp(xEvent, currentDate);
		return xEvent;
	}

	private Date generateRandomDateTimeForLogEvent() {
		return generateRandomDateTimeForLogEvent(null);
	}

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