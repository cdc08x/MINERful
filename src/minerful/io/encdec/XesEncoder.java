package minerful.io.encdec;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

public class XesEncoder implements IOutEncoder {
	protected XLog xLog = null;
	
	public XesEncoder(String[] traces) {
		this.xLog = this.encode(traces);
	}
	
	/* (non-Javadoc)
	 * @see it.uniroma1.dis.mailofmine.utils.IOutEncoder#setTraces(java.lang.String[])
	 */
	@Override
	public void setTraces(String[] traces) {
		this.encode(traces);
	}

	private XLog encode(String[] traces) {
		XFactory xFactory = new XFactoryBufferedImpl();
		XLog xLog = xFactory.createLog();
		
		XTrace xTrace = null;
		XEvent xEvent = null;
		XConceptExtension concExtino = XConceptExtension.instance();
		XLifecycleExtension lifeExtension = XLifecycleExtension.instance();
		XTimeExtension timeExtension = XTimeExtension.instance();
		xLog.getExtensions().add(concExtino);
		xLog.getExtensions().add(lifeExtension);
		xLog.getExtensions().add(timeExtension);
		xLog.getClassifiers().add(new XEventNameClassifier());
		
		concExtino.assignName(xLog, "Synthetic log");
		lifeExtension.assignModel(xLog, XLifecycleExtension.VALUE_MODEL_STANDARD);
		
		int tracesCounter = 1;
		for (String trace : traces) {
			xTrace = xFactory.createTrace();
			
			int padder = (int)(Math.ceil(Math.log10(traces.length)));
			concExtino.assignName(
					xTrace,
					String.format("Synthetic trace no. " +
							(padder < 1 ? "" : "%0" + padder) + "d", (tracesCounter++)
						)
				);
			
			if (trace.length() > 0) {
				for (Character charTask : trace.toCharArray()) {
					xEvent = xFactory.createEvent();
					concExtino.assignName(xEvent, charTask.toString());
					lifeExtension.assignStandardTransition(xEvent, XLifecycleExtension.StandardModel.COMPLETE);
					timeExtension.assignTimestamp(xEvent, new Date());
					xTrace.add(xEvent);
				}
			}
			
			xLog.add(xTrace);
		}
		
		return xLog;
	}
	
	/* (non-Javadoc)
	 * @see it.uniroma1.dis.mailofmine.utils.IOutEncoder#encodeToFile(java.io.File)
	 */
	@Override
	public File encodeToFile(File outFile) throws IOException {
		OutputStream outStream = new FileOutputStream(outFile);
		new XesXmlSerializer().serialize(this.xLog, outStream);
		return outFile;
	}
	
	/* (non-Javadoc)
	 * @see it.uniroma1.dis.mailofmine.utils.IOutEncoder#encodeToString()
	 */
	@Override
	public String encodeToString() throws IOException {
		OutputStream outStream = new ByteArrayOutputStream();
		new XesXmlSerializer().serialize(this.xLog, outStream);
		return outStream.toString();
	}
}