package minerful.logparser;

import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XesEventClassifier extends AbstractLogEventClassifier implements LogEventClassifier {
	private XEventClassifier DEFAULT_XES_EVENT_CLASSIFIER = new XEventNameClassifier();
	
	private XEventClassifier xesNativeEventClassifier;
	
	public XesEventClassifier(LogEventClassifier.ClassificationType eventClassificationType) {
		super(eventClassificationType);
		this.xesNativeEventClassifier = (eventClassificationType.equals(ClassificationType.LOG_SPECIFIED) ? null : DEFAULT_XES_EVENT_CLASSIFIER);
	}
	
	public String classify(XEvent xesNativeEvent) {
		String classString = null;
		if (this.eventClassificationType.equals(ClassificationType.NAME)) {
			classString = ((XAttributeLiteral)(xesNativeEvent.getAttributes().get(XConceptExtension.KEY_NAME))).getValue();
		} else {
			if (this.xesNativeEventClassifier != null) {
				classString = this.xesNativeEventClassifier.getClassIdentity(xesNativeEvent);
			} else {
				throw new IllegalStateException("Native event classifier not yet defined!");
			}
		}
		return classString;
	}

	/**
	 * TODO It should not ignore any other classifier but the first one!
	 * @param logSpecifiedEventClassifiers
	 * @param xLog 
	 * @return
	 */
	public boolean addXesClassifiers(List<XEventClassifier> logSpecifiedEventClassifiers, XLog xLog) {
		boolean newClassifierConsidered = false;
		if (this.xesNativeEventClassifier == null) {
			this.xesNativeEventClassifier = logSpecifiedEventClassifiers.get(0);
			newClassifierConsidered = true;
		}
		return newClassifierConsidered;
	}
	
	public Collection<String> getClasses(XLog xLog) {
		Collection<String> classes = new TreeSet<String>();
		if (xLog.getInfo(this.xesNativeEventClassifier) != null && xLog.getInfo(this.xesNativeEventClassifier).getEventClasses() != null) {
			for (XEventClass xEvClass : xLog.getInfo(this.xesNativeEventClassifier).getEventClasses().getClasses()) {
				classes.add(xEvClass.getId());
			}
		} else {
			if (this.xesNativeEventClassifier.equals(DEFAULT_XES_EVENT_CLASSIFIER)) {
				for (XTrace xTrace : xLog) {
					for (XEvent xEvent : xTrace) {
						classes.add(((XAttributeLiteral)(xEvent.getAttributes().get(XConceptExtension.KEY_NAME))).getValue());
					}
				}
			} else {
				throw new UnsupportedOperationException("To date, no other classification than log-native or name-based is supported");
			}
		}
		return classes;
	}
}