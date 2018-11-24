package minerful.concept;

import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.logparser.StringTaskClass;

public class TaskCharFactory {
	private TaskCharEncoderDecoder taChaEncDec;
	
	public TaskCharFactory() {
		this(new TaskCharEncoderDecoder());
	}

	public TaskCharEncoderDecoder getTaChaEncDec() {
		return this.taChaEncDec;
	}

	public TaskCharFactory(TaskCharEncoderDecoder taskCharEncoderDecoder) {
		this.taChaEncDec = taskCharEncoderDecoder;
	}
	
	public TaskChar makeTaskChar(AbstractTaskClass taskClass) {
		Character tChId = this.taChaEncDec.encode(taskClass);
		return new TaskChar(tChId,taskClass);
	}
	
	public TaskChar makeTaskChar(String taskName) {
		return this.makeTaskChar(new StringTaskClass(taskName));
	}
}