package minerful.logmaker;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.out.XesXmlSerializer;

import minerful.logmaker.params.SortingCriterion;
import minerful.utils.MessagePrinter;

public class XesLogTracesSorter implements Comparator<XTrace> {
	public SortingCriterion[] sortingCriteria;
	
	public XesLogTracesSorter(SortingCriterion... sortingCriteria) {
		this.sortingCriteria = sortingCriteria;
	}

	@Override
	public int compare(XTrace x1, XTrace x2) {
		if (x1.equals(x2))
			return 0;
		
		if (x1.size() == 0 && x2.size() == 0)
			return ( (x1.hashCode() < x2.hashCode()) ? -1 : 1 );
		if (x1.size() == 0)
			return 1;
		if (x2.size() == 0)
			return -1;
		
		int result = 0;

		for (int i = 0; i < sortingCriteria.length && result == 0; i++) {
			SortingCriterion sortingCriterion = sortingCriteria[i];
			switch (sortingCriterion) {
			case FIRST_EVENT_ASC:
				result = compareByFirstEventAsc(x1,x2);
				break;
			case LAST_EVENT_ASC:
				result = compareByLastEventAsc(x1, x2);
				break;
			case TRACE_LENGTH_ASC:
				result = compareByLengthAsc(x1, x2);
				break;
			case TRACE_LENGTH_DESC:
				result = compareByLengthDesc(x1, x2);
				break;
			default:
				throw new UnsupportedOperationException(String.format("Sorting criterion %s not yet implemented", sortingCriterion));
			}				
		}
		
		if ( result == 0 ) {
			result = compareByHash(x1, x2);
		}
		
		return result;
	}
	
	public int compareByHash(XTrace x1, XTrace x2) {
		int hashCode1 = x1.hashCode(), hashCode2 = x2.hashCode();
		if (hashCode1 == hashCode2)
			return 0;
		return ( (hashCode1 < hashCode2) ? -1 : 1 );
	}
	
	public int compareByFirstEventAsc(XTrace x1, XTrace x2) {
		XEvent
			evt1 = x1.get(0),
			evt2 = x2.get(0);
		return compareEventsByTimestampAsc(evt1, evt2);
	}
	
	public int compareByLastEventAsc(XTrace x1, XTrace x2) {
		XEvent
			evt1 = x1.get(x1.size()-1),
			evt2 = x2.get(x2.size()-1);
		return compareEventsByTimestampAsc(evt1, evt2);
	}
	
	public int compareByLengthAsc(XTrace x1, XTrace x2) {
		return Integer.compare(x1.size(), x2.size());
	}
	
	public int compareByLengthDesc(XTrace x1, XTrace x2) {
		return compareByLengthAsc(x1,x2) * -1;
	}		

	private int compareEventsByTimestampAsc(XEvent evt1, XEvent evt2) {
		int result = 0;
		XAttribute
			ext1cmpVal = evt1.getAttributes().get(XTimeExtension.KEY_TIMESTAMP),
			ext2cmpVal = evt2.getAttributes().get(XTimeExtension.KEY_TIMESTAMP);
		result = ext1cmpVal.compareTo(ext2cmpVal);

		return result;
	}

	public void renameEventLog(XLog evtLog) {
		String nameSortingSuffix = String.format("Event log sorted by %s", MessagePrinter.printValues(sortingCriteria));
    	XAttribute logName = evtLog.getAttributes().get(XConceptExtension.KEY_NAME);
    	XAttributeLiteral logNameString = null;
    	if (logName != null) {
    		logNameString = (XAttributeLiteral)logName;
    		logNameString.setValue(logNameString.getValue() + " -- " + nameSortingSuffix);
    	}
    	else {
    		logNameString = new XAttributeLiteralImpl(
    				XConceptExtension.KEY_NAME,
    				nameSortingSuffix);
    	}
    	evtLog.getAttributes().put(XConceptExtension.KEY_NAME, logNameString);
    	System.out.println(evtLog.getAttributes().get(XConceptExtension.KEY_NAME));
	}

	public XLog sortXesLog(XLog xLog) {
		SortedSet<XTrace> sorTraces = new TreeSet<XTrace>(this);
    	while (xLog.size() > 0) {
    		// Adding in the sorted set the next trace (removed from the original list)
    		sorTraces.add(xLog.remove(0));
    	}
    	// Adding traces again, according to the desired order
    	for (XTrace sorTrace : sorTraces) {
    		xLog.add(sorTrace);
    	}
		return xLog;
	}

	/**
	 * For debugging purposes only
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage: java " + XesLogTracesSorter.class.getName() + " <xes-file-in> <xes-file-out> <sort-by>[;<sort-by>...]");
			System.err.println("sort-by: either of " + MessagePrinter.printValues(SortingCriterion.values()));
			System.exit(1);
		}

		File xesFileIn = new File(args[0]);
		File xesFileOut = new File(args[1]);
		
		// Setting up the right XesParser
		XesXmlParser parser = new XesXmlParser();
        if (!parser.canParse(xesFileIn)) {
        	parser = new XesXmlGZIPParser();
        	if (!parser.canParse(xesFileIn)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFileIn.getAbsolutePath());
        	}
        }
        List<XLog> xLogs = parser.parse(xesFileIn);
        
        SortingCriterion[] criteria = new SortingCriterion[]{
        		SortingCriterion.LAST_EVENT_ASC, SortingCriterion.FIRST_EVENT_ASC
		};
        
        XesLogTracesSorter trSort = new XesLogTracesSorter(criteria);

        XLog evtLog = trSort.sortXesLog(xLogs.get(0));

    	// Rename the event log
        trSort.renameEventLog(evtLog);
        
        new XesXmlSerializer().serialize(evtLog, new FileOutputStream(xesFileOut));
        System.exit(0);
	}

}
