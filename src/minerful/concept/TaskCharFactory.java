package minerful.concept;

import minerful.io.encdec.TaskCharEncoderDecoder;

public class TaskCharFactory {
	private TaskCharEncoderDecoder taChaEncDec;

	public TaskCharFactory(TaskCharEncoderDecoder taskCharEncoderDecoder) {
		this.taChaEncDec = taskCharEncoderDecoder;
	}
	
	public TaskChar makeTaskChar(AbstractTaskClass taskClass) {
		Character tChId = this.taChaEncDec.encode(taskClass);
		return new TaskChar(tChId,taskClass);
	}
}