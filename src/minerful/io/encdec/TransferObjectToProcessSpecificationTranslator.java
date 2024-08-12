package minerful.io.encdec;

import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessSpecification;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.TaskCharFactory;
import minerful.concept.constraint.ConstraintsBag;

public class TransferObjectToProcessSpecificationTranslator {
	public TransferObjectToProcessSpecificationTranslator() {}

	/**
	 * Create a process specification from a Json file with the guaranties to respect the given encoding-mapping
	 * @param proSpecTO
	 * @param alphabet encoding-mapping
	 * @return
	 */
	public ProcessSpecification createProcessModel(ProcessSpecificationTransferObject proSpecTO, TaskCharArchive alphabet) {
		TaskCharEncoderDecoder alphabetEncoder= new TaskCharEncoderDecoder();
		alphabetEncoder.encode(alphabet.getTaskChars());
		/* Create/update the TaskCharArchive */
		TaskCharFactory taskCharFactory = new TaskCharFactory(alphabetEncoder);
		Set<TaskChar> taskChars = new TreeSet<TaskChar>();
		for (String taskName : proSpecTO.tasks) {
			taskChars.add(taskCharFactory.makeTaskChar(taskName));
		}
		TaskCharArchive taskCharArchive = new TaskCharArchive(taskChars);

		/* Create the constraints translator */
		TransferObjectToConstraintTranslator conTranslator = new TransferObjectToConstraintTranslator(taskCharArchive);
		ConstraintsBag bag = new ConstraintsBag(taskChars);

		for(DeclareConstraintTransferObject conTO : proSpecTO.constraints) {
			bag.add(conTranslator.createConstraint(conTO));
		}

		return new ProcessSpecification(taskCharArchive, bag, proSpecTO.name);
	}

	public ProcessSpecification createProcessModel(ProcessSpecificationTransferObject proSpecTO) {
		/* Create/update the TaskCharArchive */
		TaskCharFactory taskCharFactory = new TaskCharFactory();
		Set<TaskChar> taskChars = new TreeSet<TaskChar>();
		for (String taskName : proSpecTO.tasks) {
			taskChars.add(taskCharFactory.makeTaskChar(taskName));
		}
		TaskCharArchive taskCharArchive = new TaskCharArchive(taskChars);
		
		/* Create the constraints translator */
		TransferObjectToConstraintTranslator conTranslator = new TransferObjectToConstraintTranslator(taskCharArchive);
		ConstraintsBag bag = new ConstraintsBag(taskChars);
		
		for(DeclareConstraintTransferObject conTO : proSpecTO.constraints) {
			bag.add(conTranslator.createConstraint(conTO));
		}
		
		return new ProcessSpecification(taskCharArchive, bag, proSpecTO.name);
	}
}