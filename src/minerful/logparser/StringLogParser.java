package minerful.logparser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskClass;
import minerful.io.encdec.TaskCharEncoderDecoder;

public class StringLogParser extends AbstractLogParser implements LogParser {
    StringEventClassifier strEventClassifier;

    public StringLogParser(String[] strings, LogEventClassifier.ClassificationType evtClassType) throws Exception {
        init(evtClassType);
        
        super.archiveTaskChars(this.parseLog(strings));
	}
	
	public StringLogParser(File stringsLogFile, LogEventClassifier.ClassificationType evtClassType) throws Exception {
        if (!stringsLogFile.canRead()) {
        	throw new IllegalArgumentException("Unparsable log file: " + stringsLogFile.getAbsolutePath());
        }
        
        init(evtClassType);
        
        super.archiveTaskChars(this.parseLog(stringsLogFile));
	}

	private void init(LogEventClassifier.ClassificationType evtClassType) {
		this.taChaEncoDeco = new TaskCharEncoderDecoder();
        this.traceParsers = new ArrayList<LogTraceParser>();
        this.strEventClassifier = new StringEventClassifier(evtClassType);
	}
	
	protected Collection<TaskClass> parseLog(String[] strings) {
		Set<TaskClass> classes = new TreeSet<TaskClass>();
		
		for (String strLine : strings) {
        	strLine = strLine.trim();
        	
        	updateTraceMetrics(strLine);
        	updateTraceParsers(strLine);
            updateClasses(classes, strLine);
		}
        return classes;
	}

	private void updateTraceParsers(String strLine) {
		this.traceParsers.add(new StringTraceParser(strLine, this));
	}

	private void updateClasses(Set<TaskClass> classes, String strLine) {
		for (char chr : strLine.toCharArray()) {
			classes.add(this.strEventClassifier.classify(chr));
		}
	}

	private void updateTraceMetrics(String strLine) {
		updateMaximumTraceLength(strLine.length());
		updateMinimumTraceLength(strLine.length());
		updateNumberOfEvents(strLine.length());
	}

	@Override
	protected Collection<TaskClass> parseLog(File stringsLogFile) throws Exception {
		Set<TaskClass> classes = new TreeSet<TaskClass>();
        FileInputStream fstream = new FileInputStream(stringsLogFile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = br.readLine();
        
        while (strLine != null) {
        	strLine = strLine.trim();
        	
        	updateTraceMetrics(strLine);
        	updateTraceParsers(strLine);
            updateClasses(classes, strLine);

            strLine = br.readLine();
        }
        in.close();
        return classes;
	}

	@Override
	public LogEventClassifier getEventClassifier() {
		return this.strEventClassifier;
	}
}