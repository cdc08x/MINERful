package minerful.logparser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskCharArchive;
import minerful.io.encdec.TaskCharEncoderDecoder;

public class StringLogParser extends AbstractLogParser implements LogParser {
    StringEventClassifier strEventClassifier;
    
	protected StringLogParser(TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive, List<LogTraceParser> traceParsers,
			StringEventClassifier strEventClassifier,
			Integer startingTrace,
			Integer subLogLength) {
		super(taChaEncoDeco, taskCharArchive, traceParsers, startingTrace, subLogLength);
		this.strEventClassifier = strEventClassifier;
	}
	
	public StringLogParser(String[] strings,
    		LogEventClassifier.ClassificationType evtClassType) {
		this(strings, evtClassType, 0, 0);
	}

    public StringLogParser(String[] strings,
    		LogEventClassifier.ClassificationType evtClassType,
			Integer startingTrace,
			Integer subLogLength) {
        this.init(evtClassType, startingTrace, subLogLength);
        
        super.archiveTaskChars(this.parseLog(strings));
        
        super.postInit();
	}

	public StringLogParser(File stringsLogFile,
    		LogEventClassifier.ClassificationType evtClassType) throws Exception {
		this(stringsLogFile, evtClassType, 0, 0);
	}

	public StringLogParser(File stringsLogFile,
			LogEventClassifier.ClassificationType evtClassType,
			Integer startingTrace,
			Integer subLogLength) throws Exception {
        if (!stringsLogFile.canRead()) {
        	throw new IllegalArgumentException("Unparsable log file: " + stringsLogFile.getAbsolutePath());
        }
        this.init(evtClassType, startingTrace, subLogLength);
        
        super.archiveTaskChars(this.parseLog(stringsLogFile));
        
        super.postInit();
	}

	private void init(
			LogEventClassifier.ClassificationType evtClassType,
			Integer startingTrace,
			Integer subLogLength) {
		this.taChaEncoDeco = new TaskCharEncoderDecoder();
        this.strEventClassifier = new StringEventClassifier(evtClassType);
        this.traceParsers = new ArrayList<LogTraceParser>();
        
        super.init(startingTrace, subLogLength);
	}
	
	protected Collection<AbstractTaskClass> parseLog(String[] strings) {
		for (String strLine : strings) {
        	strLine = strLine.trim();
        	this.updateClasses(strLine);
		}
        return this.strEventClassifier.getTaskClasses();
	}

	private void updateTraceParsers(String strLine) {
		this.traceParsers.add(new StringTraceParser(strLine, this));
	}

	private void updateClasses(String strLine) {
		for (char chr : strLine.toCharArray()) {
			this.strEventClassifier.classify(chr);
		}
	}

	@Override
	protected Collection<AbstractTaskClass> parseLog(File stringsLogFile) throws Exception {
        FileInputStream fstream = new FileInputStream(stringsLogFile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine = br.readLine();
        
        while (strLine != null) {
        	strLine = strLine.trim();

        	updateTraceParsers(strLine);
            updateClasses(strLine);

            strLine = br.readLine();
        }
        in.close();
        return this.strEventClassifier.getTaskClasses();
	}

	@Override
	public LogEventClassifier getEventClassifier() {
		return this.strEventClassifier;
	}

	@Override
	protected AbstractLogParser makeACopy(
			TaskCharEncoderDecoder taChaEncoDeco,
			TaskCharArchive taskCharArchive,
			List<LogTraceParser> traceParsers,
			Integer startingTrace,
			Integer subLogLength) {
		return new StringLogParser(taChaEncoDeco, taskCharArchive, traceParsers, strEventClassifier, startingTrace, subLogLength);
	}
}