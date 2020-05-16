package minerful.concept.xmlenc;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CharAdapter extends XmlAdapter<String, Character> {
	@Override
	public String marshal(Character c) throws Exception {
		return String.valueOf(c);
	}

	@Override
	public Character unmarshal(String s) throws Exception {
		return s.charAt(0);
	}
}