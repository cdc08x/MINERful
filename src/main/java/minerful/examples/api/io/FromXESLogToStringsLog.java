package minerful.examples.api.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;

import minerful.MinerFulMinerLauncher;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.params.InputLogCmdParameters;

/**
 * This example class demonstrates how to use MINERful to convert an existing XES log into a strings-based new log, and store it.
 * @author Claudio Di Ciccio (dc.claudio@gmail.com)
 */
public class FromXESLogToStringsLog {
	public static void main(String[] args) throws FileNotFoundException {
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters();
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters();
		
		inputParams.inputLogFile = new File("/home/claudio/Code/MINERful/logs/BPIC2012/financial_log.xes.gz");
		File outputStringLogFile = new File("/home/claudio/Code/MINERful/logs/BPIC2012/financial_log.txt");
		PrintWriter outWriter = new PrintWriter(outputStringLogFile);
		
		// Parser to read the event log. Please notice that LogParser is an interface, 
		//   regardless of the specific file format (XES, string...).
		//   The static "deriveLogParserFromLogFile" method
		//   takes care of the assignment of the correct class instance to implement the interface.
		LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputParams, minerFulParams);
		
		// Print out the decoding map
		System.out.println(logParser.getEventEncoderDecoder().printDecodingMap());
		
		// This iterator reads the event log, trace by trace.
		Iterator<LogTraceParser> traceParsersIterator = logParser.traceIterator();
		String[] encodedLog = new String[logParser.length()];
		
		// This class reads within each trace, event by event.
		LogTraceParser auXTraPar = null;
		int i = 0;
		String encodedTrace = null;
		
		while (traceParsersIterator.hasNext()) {
			auXTraPar = traceParsersIterator.next();
			auXTraPar.init();
			encodedTrace = auXTraPar.encodeTrace();
			encodedLog[i++] = encodedTrace;
			outWriter.println(encodedTrace);
		}
		
		outWriter.flush();
		outWriter.close();
		
		System.out.println("Converted log saved in " + outputStringLogFile);
	}
}
