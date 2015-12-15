package minerful.logparser;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskClass;


public class StringEventClassifier extends AbstractLogEventClassifier implements LogEventClassifier {
	public StringEventClassifier(ClassificationType eventClassificationType) {
		super(eventClassificationType);
	}
	
	private Set<AbstractTaskClass> classes = new TreeSet<AbstractTaskClass>();

	public AbstractTaskClass classify(Character chr) {
		CharTaskClass chaTaCla = new CharTaskClass(chr);
		this.classes.add(chaTaCla);
		return chaTaCla;
	}
	
	public Collection<AbstractTaskClass> classify(String trace) {
		for (Character chr : trace.toCharArray()) {
			this.classes.add(new CharTaskClass(chr));
		}
		return classes;
	}

	@Override
	public Collection<AbstractTaskClass> getTaskClasses() {
		return this.classes;
	}
}
