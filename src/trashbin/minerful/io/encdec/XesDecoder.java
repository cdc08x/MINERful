package trashbin.minerful.io.encdec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

@Deprecated
public class XesDecoder {
	// TODO It must become a user-specified parameter, not a constant
    public static final boolean CONSIDER_EVENT_LIFECYCLE = false;

	public static final String EVENT_TRANSITION_STATE_INFIX = "+";

	private static Logger logger;

    protected File xesFile;
    protected XesXmlParser parser;
	
	public XesDecoder(File xesFile) throws Exception {
        if (logger == null) {
            logger = Logger.getLogger(this.getClass().getCanonicalName());
        }

        parser = new XesXmlParser();
        if (!parser.canParse(xesFile)) {
        	parser = new XesXmlGZIPParser();
        	if (!parser.canParse(xesFile)) {
        		throw new IllegalArgumentException("Unparsable log file: " + xesFile.getAbsolutePath());
        	}
        }
		this.xesFile = xesFile;
	}
	
	public static final String cleanEvtIdentifierTransitionStatus (String evtIdentifier) {
		if (evtIdentifier.contains(EVENT_TRANSITION_STATE_INFIX))
			return evtIdentifier.substring(0,evtIdentifier.lastIndexOf(EVENT_TRANSITION_STATE_INFIX));
		return evtIdentifier;
	}
	
	public static final boolean matchesEvtIdentifierWithTransitionStatus (String yourEvtIdentifier, String comparedEvtIdentifier) {
		return yourEvtIdentifier.matches(comparedEvtIdentifier + "(\\+.+)?");
	}
	
	public static final String glueActivityNameWithTransitionStatus (String evtIdentifier, String transitionStatus) {
		if (!evtIdentifier.contains(EVENT_TRANSITION_STATE_INFIX)) {
			return evtIdentifier + EVENT_TRANSITION_STATE_INFIX + transitionStatus;
		}
		logger.warn("The event identifer already contained the transition-status infix separator, " + EVENT_TRANSITION_STATE_INFIX);
		return evtIdentifier;
	}

	public List<List<String>> decode() throws Exception {
        StringBuffer debugSBuffer = new StringBuffer();
        
        List<List<String>> outTraces = new ArrayList<List<String>>();
        
        List<XTrace> traces = null;
        List<XEvent> events = null;
                
        List<String> outTrace = null;
        String evtIdentifier = null;
        
        List<XLog> xLogs = parser.parse(xesFile);
        
        for (XLog xLog : xLogs) {
	        traces = xLog;
	        
	        for (XTrace trace : traces) {
	            debugSBuffer.append("\n<");
	            
	            events = trace;
	
	            outTrace = new ArrayList<String>(events.size());
	            for (XEvent event : events) {

	            	if (event.getAttributes().get(XConceptExtension.KEY_NAME) != null) {
		                evtIdentifier = ((XAttributeLiteral)(event.getAttributes().get(XConceptExtension.KEY_NAME))).getValue();
		                // TODO It must become a user-specified parameter, not a constant
		                if (CONSIDER_EVENT_LIFECYCLE) {
			                if (event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION) != null) {
			                	evtIdentifier = evtIdentifier + EVENT_TRANSITION_STATE_INFIX + event.getAttributes().get(XLifecycleExtension.KEY_TRANSITION);
			                }
		                }
		                outTrace.add(evtIdentifier);
		                
		                debugSBuffer.append(evtIdentifier);
		                debugSBuffer.append(", ");
	            	}
	            }
	            outTraces.add(outTrace);
	        
	            debugSBuffer.delete(debugSBuffer.length() -2, debugSBuffer.length());
	            debugSBuffer.append(">");
	        }
        }
        logger.trace(debugSBuffer.toString());
		return outTraces;
	}
}