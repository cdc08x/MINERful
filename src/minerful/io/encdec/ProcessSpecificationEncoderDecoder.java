package minerful.io.encdec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.MetaConstraintUtils;
import minerful.io.encdec.json.JsonPojoEncoderDecoder;
import minerful.io.encdec.pojo.ProcessSpecificationPojo;
import minerful.logparser.LogParser;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * This class marshals and unmarshals process specifications to/from XML files.
 * It also reads and saves process specifications in JSON format to/from JSON-formatted text files and strings. 
 * @author Claudio Di Ciccio
 *
 */
public class ProcessSpecificationEncoderDecoder {
	/**
	 * Changes the identifier of the
	 * {@link TaskChar TaskChar}
	 * elements in the given process specification according to the encoding of the event log.
	 * Notice that it does so as a side effect on the original process specification passed in input and on the
	 * {@link TaskChar TaskChar} elements themselves.
	 * @param processModel A process specification
	 * @param logPar An event log parser
	 * @return The process specification having the {@link TaskChar TaskChar} re-encoded according to the event log identifiers
	 */
	public ProcessSpecification reEncodeTaskCharsAccordingToEventLog(ProcessSpecification processModel, LogParser logPar) {
		logPar.getEventEncoderDecoder().mergeWithConstraintsAndUpdateTheirParameters(
				processModel.getAllConstraints().toArray(new Constraint[processModel.howManyConstraints()]));
		return processModel;
	}
	
	// public ProcessSpecification unmarshalProcessSpecification(File procSchmInFile) throws JAXBException, PropertyException, FileNotFoundException,
	// 		IOException {
	// 	String pkgName = ProcessSpecification.class.getCanonicalName().toString();
	// 	pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
	// 	JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
		
	// 	Unmarshaller unmarsh = jaxbCtx.createUnmarshaller();
	// 	unmarsh.setEventHandler(
	// 		    new ValidationEventHandler() {
	// 		        public boolean handleEvent(ValidationEvent event) {
	// 		            throw new RuntimeException(event.getMessage(),
	// 		                                       event.getLinkedException());
	// 		        }
	// 		});
	// 	ProcessSpecification proSpec = (ProcessSpecification) unmarsh.unmarshal(procSchmInFile);
		
	// 	MetaConstraintUtils.createHierarchicalLinks(proSpec.getAllConstraints());
		
	// 	return proSpec;
	// }
	
	// public StringBuffer marshalProcessModel(ProcessSpecification processModel)
	// 		throws JAXBException, PropertyException, FileNotFoundException, IOException {
	// 	String pkgName = processModel.getClass().getCanonicalName().toString();
	// 	pkgName = pkgName.substring(0, pkgName.lastIndexOf('.'));
	// 	JAXBContext jaxbCtx = JAXBContext.newInstance(pkgName);
	// 	Marshaller marsh = jaxbCtx.createMarshaller();
	// 	marsh.setProperty("jaxb.formatted.output", true);
	// 	StringWriter strixWriter = new StringWriter();
	// 	marsh.marshal(processModel, strixWriter);
	// 	strixWriter.flush();
	// 	StringBuffer strixBuffer = strixWriter.getBuffer();
		
	// 	return strixBuffer;
	// }

// 	public void marshalProcessSpecification(ProcessSpecification processModel, File procSchmOutFile)
// 			throws JAXBException, PropertyException, FileNotFoundException, IOException {
// 		StringBuffer strixBuffer = this.marshalProcessModel(processModel);
// 		// OINK
// //		strixBuffer.replace(
// //				strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
// //				strixBuffer.indexOf(">", strixBuffer.indexOf("?>") + 3),
// //				" xmlns=\"" + ProcessModel.MINERFUL_XMLNS + "\"");
// 		FileWriter strixFileWriter = new FileWriter(procSchmOutFile);
// 		strixFileWriter.write(strixBuffer.toString());
// 		strixFileWriter.flush();
// 		strixFileWriter.close();
// 	}

	public ProcessSpecification readFromJsonFile(File processModelJsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessSpecificationPojo pojo = jsonPojoMgr.fromJsonToProcessSpecificationPojo(processModelJsonFile);
		ProcessSpecificationTransferObject proSpecTO = new ProcessSpecificationTransferObject(pojo);
		TransferObjectToProcessSpecificationTranslator translator = new TransferObjectToProcessSpecificationTranslator();
		return translator.createProcessModel(proSpecTO);
	}

	/**
	 * read a process  from a Json file with the guaranties to respect the given encoding-mapping
	 *
	 * @param processModelJsonFile
	 * @param alphabet encoding-mapping
	 * @return
	 * @throws JsonSyntaxException
	 * @throws JsonIOException
	 * @throws FileNotFoundException
	 */
	public ProcessSpecification readFromJsonFile(File processModelJsonFile, TaskCharArchive alphabet) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessSpecificationPojo pojo = jsonPojoMgr.fromJsonToProcessSpecificationPojo(processModelJsonFile);
		ProcessSpecificationTransferObject proSpecTO = new ProcessSpecificationTransferObject(pojo);
		TransferObjectToProcessSpecificationTranslator translator = new TransferObjectToProcessSpecificationTranslator();
		return translator.createProcessModel(proSpecTO, alphabet);
	}

	public void writeToJsonFile(ProcessSpecification processModel, File processModelJsonFile) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ProcessSpecificationTransferObject proSpecTO = new ProcessSpecificationTransferObject(processModel);
		ProcessSpecificationPojo pojo = proSpecTO.toPojo();
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		jsonPojoMgr.saveProcessSpecificationPojo(pojo, processModelJsonFile);

		return;
	}

	public ProcessSpecification readFromJsonString(String processModelJson) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		ProcessSpecificationPojo pojo = jsonPojoMgr.fromJsonToProcessSpecificationPojo(processModelJson);
		ProcessSpecificationTransferObject proSpecTO = new ProcessSpecificationTransferObject(pojo);
		TransferObjectToProcessSpecificationTranslator translator = new TransferObjectToProcessSpecificationTranslator();
		return translator.createProcessModel(proSpecTO);
	}

	public String toJsonString(ProcessSpecification processModel) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		ProcessSpecificationTransferObject proSpecTO = new ProcessSpecificationTransferObject(processModel);
		ProcessSpecificationPojo pojo = proSpecTO.toPojo();
		JsonPojoEncoderDecoder jsonPojoMgr = new JsonPojoEncoderDecoder();
		return jsonPojoMgr.fromProcessSpecificationPojoToJson(pojo);
	}
}