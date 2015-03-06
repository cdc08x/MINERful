package minerful.logparser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import minerful.io.encdec.TaskCharEncoderDecoder;

public class StringLogParser extends AbstractLogParser implements LogParser {
    StringEventClassifier strEventClassifier;

    public StringLogParser(String[] strings, LogEventClassifier.ClassificationType evtClassType) throws Exception {
        init(evtClassType);
        
        this.parseLog(strings);
	}
	
	public StringLogParser(File stringsLogFile, LogEventClassifier.ClassificationType evtClassType) throws Exception {
        if (!stringsLogFile.canRead()) {
        	throw new IllegalArgumentException("Unparsable log file: " + stringsLogFile.getAbsolutePath());
        }
        
        init(evtClassType);
        
        this.parseLog(stringsLogFile);
	}

	private void init(LogEventClassifier.ClassificationType evtClassType) {
		this.taChaEncoDeco = new TaskCharEncoderDecoder();
        this.traceParsers = new ArrayList<LogTraceParser>();
        this.strEventClassifier = new StringEventClassifier(evtClassType);
	}
	
	protected void parseLog(String[] strings) {
		Set<String> classes = new TreeSet<String>();
		
		for (String strLine : strings) {
        	strLine = strLine.trim();
        	
        	updateTraceMetrics(strLine);
        	updateTraceParsers(strLine);
            updateClasses(classes, strLine);
		}
        super.archiveTaskChars(classes);
	}

	private void updateTraceParsers(String strLine) {
		this.traceParsers.add(new StringTraceParser(strLine, this));
	}

	private void updateClasses(Set<String> classes, String strLine) {
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
	protected void parseLog(File stringsLogFile) throws Exception {
		Set<String> classes = new TreeSet<String>();
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
        super.archiveTaskChars(classes);
	}

	@Override
	public LogEventClassifier getEventClassifier() {
		return this.strEventClassifier;
	}

}
