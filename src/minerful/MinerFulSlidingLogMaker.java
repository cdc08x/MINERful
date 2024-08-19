package minerful;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XMxmlGZIPParser;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.out.XMxmlSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskCharArchive;
import minerful.io.ConstraintsPrinter;
import minerful.io.params.OutputSpecificationParameters;
import minerful.logparser.LogParser;
import minerful.miner.core.MinerFulKBCore;
import minerful.miner.core.MinerFulQueryingCore;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.params.InputLogCmdParameters;
import minerful.params.SlidingLogXtractorCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.params.InputLogCmdParameters.InputEncoding;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulSlidingLogMaker extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulSlidingLogMaker.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = super.setupOptions();
		
		Options slidingOptions = SlidingLogXtractorCmdParameters.parseableOptions();
		
    	for (Object opt: slidingOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}

	public static void main(String[] args) {
		MinerFulSlidingLogMaker subLogExtraSlider = new MinerFulSlidingLogMaker();
		Options cmdLineOptions = subLogExtraSlider.setupOptions();

		SlidingLogXtractorCmdParameters slideParams =
				new SlidingLogXtractorCmdParameters(
						cmdLineOptions,
						args);
		InputLogCmdParameters inputParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		SystemCmdParameters systemParams =
				new SystemCmdParameters(
						cmdLineOptions,
						args);

		if (systemParams.help) {
			systemParams.printHelp(cmdLineOptions);
			System.exit(0);
		}
		if (inputParams.inputLogFile == null) {
			systemParams.printHelpForWrongUsage("Input log file missing!",
					cmdLineOptions);
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);

		logger.info("Loading log...");

		try {
			slideAndExtract(slideParams, inputParams);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String[] slideAndExtract(SlidingLogXtractorCmdParameters slideParams, InputLogCmdParameters inputParams) throws Exception {
		if (inputParams.inputLanguage != InputEncoding.xes &&  inputParams.inputLanguage != InputEncoding.mxml) {
			throw new UnsupportedOperationException("Implementation not available for the specified input event log type: " + inputParams.inputLanguage);
		}

		File eventLogFile = inputParams.inputLogFile;

		switch(inputParams.inputLanguage) {
			case xes:
			case mxml:
				XParser parser = new XesXmlParser();  // Parses XES log files
				if (!parser.canParse(eventLogFile)) {
					parser = new XesXmlGZIPParser();  // Parses gun-zipped XES log files
					if (!parser.canParse(eventLogFile)) {
						parser = new XMxmlParser();  // Parses MXML log files
						if (!parser.canParse(eventLogFile)) {
							parser = new XMxmlGZIPParser();  // Parses gun-zipped MXML log files
							if (!parser.canParse(eventLogFile)) {
								throw new IllegalArgumentException("Unparsable log file: " + eventLogFile.getAbsolutePath());
							}
						}
					}
				}

				// Take the first event log in the file (typically we never have more than a log in an event log file)
				XLog inEvLog = parser.parse(eventLogFile).get(0);
				XFactory xFactory = new XFactoryBufferedImpl();
				XLog outEvLog =  xFactory.createLog(inEvLog.getAttributes());
				List<XTrace> traceList = new ArrayList<XTrace>(inEvLog);
				File outFile =  null;


				OutputStream outStream = null;
				int from = inputParams.startFromTrace,
					to   = inputParams.startFromTrace + inputParams.subLogLength;
				
				boolean stopAtNextIteration = false;

				do {
					if (to >= inEvLog.size()) {
						to = inEvLog.size();
						stopAtNextIteration = true;
					}
					outEvLog.addAll(traceList.subList(from, to));
					outFile = new File(slideParams.outDir.getAbsolutePath(), eventLogFile.getName() + "." + from + "-" + (to-1) + "." + slideParams.outputEncoding.toString());
					outStream = new FileOutputStream(outFile);
					printEncodedLogInStream(outStream, slideParams, outEvLog);
					outEvLog.removeAll(outEvLog);

					if (!slideParams.stickTail) {
						from += inputParams.subLogLength;
					}
					to += inputParams.subLogLength;
				} while (!stopAtNextIteration);


				// TODO: Continue the cycle

				break;
			default:
				throw new UnsupportedOperationException("Implementation not available for the specified input event log type: " + inputParams.inputLanguage);
		}
		List<String> savedLogFilePaths = new ArrayList<String>();
/*		

		LogParser slicedLogParser = logParser.takeASlice(inputParams.startFromTrace, inputParams.subLogLength);
		
		int step = slideParams.slidingStep;

		if (slideParams.slidingStep > 0) {   		
    		//
    		// In the ASCII picture below, every column is a trace.
    		// The '=' symbols denote the original sub-log.
    		// The length of the shift is denoted with '>'
    		// The '-' symbol denotes the traces for which entries have to be removed from the KB
    		// The '+' symbol indicates the traces for which entries have to be added to the KB
    		//
    		//  ========>>>>     ===     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ----    ++++     ---     +++
    		//  ========>>>>     ===     >>>
    		//
    		int
				subtraLen = Math.min(step, inputParams.subLogLength),
				addiStartGap = (step < inputParams.subLogLength ? inputParams.subLogLength : step),
				addiLen = Math.min(step, inputParams.subLogLength);

    		for (int i = 0; inputParams.startFromTrace + i + addiStartGap + addiLen <= logParser.wholeLength(); i += step) {
				if (!slideParams.stickTail) {
					slicedLogParser = logParser.takeASlice(
							inputParams.startFromTrace + i,
							subtraLen
					);
				}
				slicedLogParser = logParser.takeASlice(inputParams.startFromTrace + i + addiStartGap, addiLen);
				
				from = inputParams.startFromTrace + i + step;
				to = inputParams.startFromTrace + i + addiStartGap + addiLen;
			}
			
			if (!slideParams.stickTail) {
			}
    	}
*/
		return savedLogFilePaths.toArray(new String[savedLogFilePaths.size()]);
	}

	/**
	 * Prints the generated event log, {@link #log log}, in the specified output stream.
	 * @return The print-out of the event log
	 * @throws IOException
	 */
	private static boolean printEncodedLogInStream(OutputStream outStream, SlidingLogXtractorCmdParameters slideParams, XLog log) throws IOException {

		switch(slideParams.outputEncoding) {
		case xes:
			new XesXmlSerializer().serialize(log, outStream);
			break;
		case mxml:
			new XMxmlSerializer().serialize(log, outStream);
			break;
		case strings:
		default:
			outStream.flush();
			outStream.close();
			throw new UnsupportedOperationException("Support for this encoding is still work-in-progress");
		}
		return true;
	}
/*
	public PrintWriter setUpCSVPrintWriter(SlidingMiningCmdParameters slideParams) {
		PrintWriter outWriter = null;
    	try {
    		outWriter = new PrintWriter(slideParams.intermediateOutputCsvFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.warn("Redirecting intermediate model measures to standard output");
			outWriter = new PrintWriter(System.out);
		}
		return outWriter;
	}
*/
}