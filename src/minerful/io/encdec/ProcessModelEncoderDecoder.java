package minerful.io.encdec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import minerful.concept.ProcessModel;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;

public class ProcessModelEncoderDecoder {
	public ProcessModel unmarshalProcessModel(File procSchmInFile) throws JAXBException, PropertyException, FileNotFoundException,
			IOException {
		String pkgName = ProcessModel.class.getCanonicalName().toString();
		pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
		JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		
		Unmarshaller unmarsh = jaxbCtx.createUnmarshaller();
		unmarsh.setEventHandler(
			    new ValidationEventHandler() {
			        public boolean handleEvent(ValidationEvent event) {
			            throw new RuntimeException(event.getMessage(),
			                                       event.getLinkedException());
			        }
			});
		ProcessModel proMod = (ProcessModel) unmarsh.unmarshal(procSchmInFile);
		
		MetaConstraintUtils.createHierarchicalLinks(proMod.getAllConstraints());
		
		return proMod;
	}
	public void marshalProcessModel(ProcessModel processModel, File procSchmOutFile) 	throws JAXBException, PropertyException, FileNotFoundException, IOException {
		String pkgName = processModel.getClass().getCanonicalName().toString();
		pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
		JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(processModel, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();

		// OINK
//		strixBuffer.replace(
//				strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
//				strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
//				" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");
		FileWriter strixFileWriter = new FileWriter(procSchmOutFile);
		strixFileWriter.write(strixBuffer.toString());
		strixFileWriter.flush();
		strixFileWriter.close();
	}
	
	
}