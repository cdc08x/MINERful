package minerful.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;

import minerful.logmaker.XesLogTracesSorter;
import minerful.logmaker.params.XesLogSorterParameters;

/**
 * Launches the sorting of XES event logs.
 */
public class XesLogTracesSorterLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(XesLogTracesSorterLauncher.class);

	private XesLogSorterParameters xeSortParams;
	private XesXmlParser parser = null;
	
	public XesLogTracesSorterLauncher(XesLogSorterParameters xeSortParams) {
		this.xeSortParams = xeSortParams;
	}
	
	public void sortAndStoreXesLog() {
		logger.info("Loading the XES log from %s ...", xeSortParams.inputXesFile);
		XLog xLog = null;
		try {
			xLog = readXLog(xeSortParams);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Sorting the XES log...");
		XesLogTracesSorter trSort = new XesLogTracesSorter(xeSortParams.tracesSortingCriteria);
		XLog nuXLog = trSort.sortXesLog(xLog);
		logger.info("Saving the XES log on %s...", xeSortParams.outputXesFile);
		trSort.renameEventLog(nuXLog);
		try {
			storeSortedXesLog(nuXLog);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void storeSortedXesLog(XLog nuXLog) throws IOException, FileNotFoundException {
		new XesXmlSerializer().serialize(nuXLog, new FileOutputStream(xeSortParams.outputXesFile));
	}

	private XLog readXLog(XesLogSorterParameters xeSortParams) throws Exception {
		this.parser = new XesXmlParser();
        if (!parser.canParse(xeSortParams.inputXesFile)) {
        	parser = new XesXmlGZIPParser();
        	if (!parser.canParse(xeSortParams.inputXesFile)) {
        		throw new IllegalArgumentException(
        				"Unparsable log file: " + xeSortParams.inputXesFile.getAbsolutePath());
        	}
        }
        return parser.parse(xeSortParams.inputXesFile).get(0);
	}
}