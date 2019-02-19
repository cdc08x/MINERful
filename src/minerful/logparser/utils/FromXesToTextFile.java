package minerful.logparser.utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;

import minerful.logparser.LogEventClassifier.ClassificationType;
import minerful.logparser.LogTraceParser;
import minerful.logparser.XesLogParser;

public class FromXesToTextFile {
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("Usage: java " + FromXesToTextFile.class.getName() + " <xes-file-in> <string-file-out> <dictionary-file-out>");
			System.exit(1);
		}

		File xesFileIn = new File(args[0]);
		File textFileOut = new File(args[1]);
		File dicFileOut = new File(args[2]);
		
		XesLogParser logParser = new XesLogParser(xesFileIn, ClassificationType.LOG_SPECIFIED);
		
		Iterator<LogTraceParser> traParserIt = logParser.traceIterator();
		
		PrintWriter priWri = new PrintWriter(textFileOut);

		// Encode the event log and store it in args[1]
		while (traParserIt.hasNext()) {
			priWri.println(traParserIt.next().encodeTrace());
		}
		priWri.flush();
		priWri.close();
		
		// Print out the dictionary in args[2]
		priWri = new PrintWriter(dicFileOut);
		priWri.println(logParser.getEventEncoderDecoder());
		priWri.flush();
		priWri.close();

		System.exit(0);
	}
}
