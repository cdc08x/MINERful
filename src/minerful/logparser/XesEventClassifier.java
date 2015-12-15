package minerful.logparser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import minerful.concept.AbstractTaskClass;

import org.deckfour.xes.classification.XEventClasses;
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
	private XLog xLog;
	private XEventClasses xEvtClasses;
	
	public XesEventClassifier(LogEventClassifier.ClassificationType eventClassificationType) {
		super(eventClassificationType);
		this.xesNativeEventClassifier = (eventClassificationType.equals(ClassificationType.LOG_SPECIFIED) ? null : DEFAULT_XES_EVENT_CLASSIFIER);
		this.xEvtClasses = ( this.xesNativeEventClassifier == null ? null : new XEventClasses(this.xesNativeEventClassifier) );
	}

	public XesTaskClass classify(XEvent xesNativeEvent) {
		if (this.xEvtClasses == null)
			throw new IllegalStateException("No classes for events available, until at least an instance of XLog has been parsed");

		return new XesTaskClass(xEvtClasses.getClassOf(xesNativeEvent));
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
			for(int i = 0; i < logSpecifiedEventClassifiers.size() && this.xesNativeEventClassifier == null; i++) {
				this.xesNativeEventClassifier = logSpecifiedEventClassifiers.get(i);
			}
			newClassifierConsidered = true;
		}
		if (newClassifierConsidered || this.xEvtClasses.size() == 0) {
			this.xEvtClasses = XEventClasses.deriveEventClasses(xesNativeEventClassifier, xLog);
		}
		return newClassifierConsidered;
	}
	
	@Override
	public Collection<AbstractTaskClass> getTaskClasses() {
		if (this.xEvtClasses == null)
			throw new IllegalStateException("No classes for events available, until at least an instance of XLog has been parsed");

		Collection<AbstractTaskClass> taskClasses = new ArrayList<AbstractTaskClass>(this.xEvtClasses.size());
		for ( int i = 0; i < this.xEvtClasses.size(); i++ ) {
			taskClasses.add(new XesTaskClass(this.xEvtClasses.getByIndex(i)));
		}
		return taskClasses;
	}

	@Deprecated
	public String getClassNameOf(XEvent xesNativeEvent) {
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

	@Deprecated
	public Collection<String> getClassNames() {
		Collection<String> classes = new TreeSet<String>();
		
		if (this.xesNativeEventClassifier != null) {
			for (XTrace xTrace : xLog) {
/*
				for (XEventClass xEvClass : xLog.getInfo(this.xesNativeEventClassifier).getEventClasses().getClasses()) {
					classes.add(xEvClass.getId());
				}
*/
				for (XEvent xEvent : xTrace) {
					classes.add(this.xesNativeEventClassifier.getClassIdentity(xEvent));
				}
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