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
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.io.encdec.json.JsonPojoEncoderDecoder;
import minerful.io.encdec.pojo.ProcessModelPojo;
import minerful.logparser.LogParser;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * This class marshals and unmarshals process models to/from XML files.
 * It also reads and saves process models in JSON format to/from JSON-formatted text files and strings. 
 * @author Claudio Di Ciccio
 *
 */
public class ProcessModelEncoderDecoder {
	/**
	 * Changes the identifier of the
	 * {@link TaskChar TaskChar}
	 * elements in the given process model according to the encoding of the event log.
	 * Notice that it does so as a side effect on the original process model passed in input and on the
	 * {@link TaskChar TaskChar} elements themselves.
	 * @param processModel A process model
	 * @param logPar An event log parser
	 * @return The process model having the {@link TaskChar TaskChar} re-encoded according to the event log identifiers
	 */
	public ProcessModel reEncodeTaskCharsAccordingToEventLog(ProcessModel processModel, LogParser logPar) {
		logPar.getEventEncoderDecoder().mergeWithConstraintsAndUpdateTheirParameters(
				processModel.getAllConstraints().toArray(new Constraint[processModel.howManyConstraints()]));
		return processModel;
	}
	
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
	
	public StringBuffer marshalProcessModel(ProcessModel processModel)
			throws JAXBException, PropertyException, FileNotFoundException, IOException {
		String pkgName = processModel.getClass().getCanonicalName().toString();
		pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
		JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		Marshaller marsh = jaxbCtx.createMarshaller();
		marsh.setProperty("jaxb.formatted.output", true);
		StringWriter strixWriter = new StringWriter();
		marsh.marshal(processModel, strixWriter);
		strixWriter.flush();
		StringBuffer strixBuffer = strixWriter.getBuffer();
		
		return strixBuffer;
	}

	public void marshalProcessModel(ProcessModel processModel, File procSchmOutFile)
			throws JAXBException, PropertyException, FileNotFoundException, IOException {
		StringBuffer strixBuffer = this.marshalProcessModel(processModel);
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

	public ProcessModel readFromJsonFile(File processModelJsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessModelPojo pojo = jsonPojoMgr.fromJsonToProcessModelPojo(processModelJsonFile);
		ProcessModelTransferObject proModTO = new ProcessModelTransferObject(pojo);
		TransferObjectToProcessModelTranslator translator = new TransferObjectToProcessModelTranslator();
		return translator.createProcessModel(proModTO);
	}

	/**
	 * read a process model from a Json file with the guaranties to respect the given encoding-mapping
	 *
	 * @param processModelJsonFile
	 * @param alphabet encoding-mapping
	 * @return
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws FileNotFoundException
	 */
	public ProcessModel readFromJsonFile(File processModelJsonFile, TaskCharArchive alphabet) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessModelPojo pojo = jsonPojoMgr.fromJsonToProcessModelPojo(processModelJsonFile);
		ProcessModelTransferObject proModTO = new ProcessModelTransferObject(pojo);
		TransferObjectToProcessModelTranslator translator = new TransferObjectToProcessModelTranslator();
		return translator.createProcessModel(proModTO, alphabet);
	}

	public void writeToJsonFile(ProcessModel processModel, File processModelJsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ProcessModelTransferObject proModTO = new ProcessModelTransferObject(processModel);
		ProcessModelPojo pojo = proModTO.toPojo();
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		jsonPojoMgr.saveProcessModelPojo(pojo, processModelJsonFile);

		return;
	}

	public ProcessModel readFromJsonString(String processModelJson) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessModelPojo pojo = jsonPojoMgr.fromJsonToProcessModelPojo(processModelJson);
		ProcessModelTransferObject proModTO = new ProcessModelTransferObject(pojo);
		TransferObjectToProcessModelTranslator translator = new TransferObjectToProcessModelTranslator();
		return translator.createProcessModel(proModTO);
	}

	public String toJsonString(ProcessModel processModel) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ProcessModelTransferObject proModTO = new ProcessModelTransferObject(processModel);
		ProcessModelPojo pojo = proModTO.toPojo();
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		return jsonPojoMgr.fromProcessModelPojoToJson(pojo);
	}
}