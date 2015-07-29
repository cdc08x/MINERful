package minerful.logparser;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.TaskClass;


public class StringEventClassifier extends AbstractLogEventClassifier implements LogEventClassifier {
	public StringEventClassifier(ClassificationType eventClassificationType) {
		super(eventClassificationType);
	}
	
	private Set<TaskClass> classes = new TreeSet<TaskClass>();

	public TaskClass classify(Character chr) {
		CharTaskClass chaTaCla = new CharTaskClass(chr);
		this.classes.add(chaTaCla);
		return chaTaCla;
	}
	
	public Collection<TaskClass> classify(String trace) {
		for (Character chr : trace.toCharArray()) {
			this.classes.add(new CharTaskClass(chr));
		}
		return classes;
	}

	@Override
	public Collection<TaskClass> getTaskClasses() {
		return this.classes;
	}
}
