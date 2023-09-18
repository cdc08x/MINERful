package minerful.io.encdec;

import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.ConstraintsBag;

public class TransferObjectToProcessModelTranslator {
	public TransferObjectToProcessModelTranslator() {}

	/**
	 * Create a process model from a Json file with the guaranties to respect the given encoding-mapping
	 * @param proModTO
	 * @param alphabet encoding-mapping
	 * @return
	 */
	public ProcessSpecification createProcessModel(ProcessModelTransferObject proModTO, TaskCharArchive alphabet) {
		TaskCharEncoderDecoder alphabetEncoder= new TaskCharEncoderDecoder();
		alphabetEncoder.encode(alphabet.getTaskChars());
		/* Create/update the TaskCharArchive */
		TaskCharFactory taskCharFactory = new TaskCharFactory(alphabetEncoder);
		Set<TaskChar> taskChars = new TreeSet<TaskChar>();
		for (String taskName : proModTO.tasks) {
			taskChars.add(taskCharFactory.makeTaskChar(taskName));
		}
		TaskCharArchive taskCharArchive = new TaskCharArchive(taskChars);

		/* Create the constraints translator */
		TransferObjectToConstraintTranslator conTranslator = new TransferObjectToConstraintTranslator(taskCharArchive);
		ConstraintsBag bag = new ConstraintsBag(taskChars);

		for(DeclareConstraintTransferObject conTO : proModTO.constraints) {
			bag.add(conTranslator.createConstraint(conTO));
		}

		return new ProcessSpecification(taskCharArchive, bag, proModTO.name);
	}

	public ProcessSpecification createProcessModel(ProcessModelTransferObject proModTO) {
		/* Create/update the TaskCharArchive */
		TaskCharFactory taskCharFactory = new TaskCharFactory();
		Set<TaskChar> taskChars = new TreeSet<TaskChar>();
		for (String taskName : proModTO.tasks) {
			taskChars.add(taskCharFactory.makeTaskChar(taskName));
		}
		TaskCharArchive taskCharArchive = new TaskCharArchive(taskChars);
		
		/* Create the constraints translator */
		TransferObjectToConstraintTranslator conTranslator = new TransferObjectToConstraintTranslator(taskCharArchive);
		ConstraintsBag bag = new ConstraintsBag(taskChars);
		
		for(DeclareConstraintTransferObject conTO : proModTO.constraints) {
			bag.add(conTranslator.createConstraint(conTO));
		}
		
		return new ProcessSpecification(taskCharArchive, bag, proModTO.name);
	}
}