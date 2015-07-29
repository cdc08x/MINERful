package minerful.logparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import minerful.concept.TaskCharArchive;
import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XesLogParser extends AbstractLogParser implements LogParser {
    protected XesXmlParser parser;
    protected XesEventClassifier xesEventClassifier;
    protected List<XLog> xLogs = null;
    
	protected XesLogParser(TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive, List<LogTraceParser> traceParsers,
			XesXmlParser parser, XesEventClassifier xesEventClassifier, List<XLog> xLogs) {
		super(taChaEncoDeco, taskCharArchive, traceParsers);
		this.parser = parser;
		this.xesEventClassifier = xesEventClassifier;
		this.xLogs = xLogs;
	}

    
    private void init(LogEventClassifier.ClassificationType evtClassType) {
        this.traceParsers = new ArrayList<LogTraceParser>();
        this.taChaEncoDeco = new TaskCharEncoderDecoder();
        this.parser = new XesXmlParser();
        this.xesEventClassifier = new XesEventClassifier(evtClassType);
    }

    public XesLogParser(File xesFile, LogEventClassifier.ClassificationType evtClassType) throws Exception {
    	this.init(evtClassType);
    	
        if (!this.parser.canParse(xesFile)) {
        	this.parser = new XesXmlGZIPParser();
        	if (!this.parser.canParse(xesFile)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFile.getAbsolutePath());
        	}
        }

        super.archiveTaskChars(this.parseLog(xesFile));
	}
    
    public XesLogParser(XLog xLog, LogEventClassifier.ClassificationType evtClassType) {
    	this.init(evtClassType);
    	
    	super.archiveTaskChars(this.parseLog(xLog));
    }
	
    @Override
	protected Collection<TaskClass> parseLog(File xesFile) throws Exception {
        this.xLogs = parser.parse(xesFile);

        XesTraceParser auXTraPar = null;

        for (XLog xLog : xLogs) {
        	List<XEventClassifier> logSpecifiedEventClassifiers = xLog.getClassifiers();

        	this.xesEventClassifier.addXesClassifiers(logSpecifiedEventClassifiers, xLog);

	        for (XTrace trace : xLog) {
	        	auXTraPar = new XesTraceParser(trace, this);
	        	this.traceParsers.add(auXTraPar);
	        	updateMaximumTraceLength(auXTraPar.length());
	        	updateMinimumTraceLength(auXTraPar.length());
	        	updateNumberOfEvents(auXTraPar.length());
	        }
        }
        
        return this.xesEventClassifier.getTaskClasses();
	}
    
    protected Collection<TaskClass> parseLog(XLog xLog) {
    	List<XEventClassifier> logSpecifiedEventClassifiers = xLog.getClassifiers();
        XesTraceParser auXTraPar = null;

    	this.xesEventClassifier.addXesClassifiers(logSpecifiedEventClassifiers, xLog);

        for (XTrace trace : xLog) {
        	auXTraPar = new XesTraceParser(trace, this);
        	this.traceParsers.add(auXTraPar);
        	updateMaximumTraceLength(auXTraPar.length());
        	updateMinimumTraceLength(auXTraPar.length());
        	updateNumberOfEvents(auXTraPar.length());
        }
        return this.xesEventClassifier.getTaskClasses();
    }

	@Override
	public LogEventClassifier getEventClassifier() {
		return this.xesEventClassifier;
	}

	public XLog getFirstXLog() {
		return this.xLogs.get(0);
	}


	@Override
	protected AbstractLogParser makeACopy(
			TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive, List<LogTraceParser> traceParsers) {
		return new XesLogParser(taChaEncoDeco, taskCharArchive, traceParsers, parser, xesEventClassifier, xLogs);
	}
}