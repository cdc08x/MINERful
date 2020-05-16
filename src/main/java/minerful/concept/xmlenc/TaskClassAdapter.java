package minerful.concept.xmlenc;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import minerful.concept.AbstractTaskClass;
import minerful.logparser.StringTaskClass;

public class TaskClassAdapter extends XmlAdapter<String, AbstractTaskClass> {
	@Override
	public String marshal(AbstractTaskClass c) throws Exception {
		return c.getName();
	}

	@Override
	public AbstractTaskClass unmarshal(String s) throws Exception {
		return new StringTaskClass(s);
	}
}