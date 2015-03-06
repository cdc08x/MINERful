package minerful.logparser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import minerful.io.encdec.TaskCharEncoderDecoder;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XesLogParser extends AbstractLogParser implements LogParser {
    protected XesXmlParser parser;
    XesEventClassifier xesEventClassifier;

    public XesLogParser(File xesFile, LogEventClassifier.ClassificationType evtClassType) throws Exception {
        this.traceParsers = new ArrayList<LogTraceParser>();
        this.taChaEncoDeco = new TaskCharEncoderDecoder();
        this.parser = new XesXmlParser();
        this.xesEventClassifier = new XesEventClassifier(evtClassType);
        if (!this.parser.canParse(xesFile)) {
        	this.parser = new XesXmlGZIPParser();
        	if (!this.parser.canParse(xesFile)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFile.getAbsolutePath());
        	}
        }

		this.parseLog(xesFile);
	}
	
    @Override
	protected void parseLog(File xesFile) throws Exception {
        List<XLog> xLogs = parser.parse(xesFile);
        Collection<String> classes = new TreeSet<String>();
        XesTraceParser auXTraPar = null;

        for (XLog xLog : xLogs) {
        	List<XEventClassifier> logSpecifiedEventClassifiers = xLog.getClassifiers();

        	if (!logSpecifiedEventClassifiers.isEmpty()) {
        		this.xesEventClassifier.addXesClassifiers(logSpecifiedEventClassifiers, xLog);
        	}
	        for (XTrace trace : xLog) {
	        	auXTraPar = new XesTraceParser(trace, this);
	        	this.traceParsers.add(auXTraPar);
	        	updateMaximumTraceLength(auXTraPar.length());
	        	updateMinimumTraceLength(auXTraPar.length());
	        	updateNumberOfEvents(auXTraPar.length());
	        }
        }
        for (XLog xLog : xLogs) {
        	classes.addAll(this.xesEventClassifier.getClasses(xLog));
        }
        super.archiveTaskChars(classes);
	}

	@Override
	public LogEventClassifier getEventClassifier() {
		return this.xesEventClassifier;
	}

}