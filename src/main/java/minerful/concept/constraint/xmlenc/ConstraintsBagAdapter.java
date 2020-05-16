package minerful.concept.constraint.xmlenc;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import minerful.concept.constraint.ConstraintsBag;

public class ConstraintsBagAdapter extends XmlAdapter<ConstraintsBagDto, ConstraintsBag>{

	@Override
	public ConstraintsBag unmarshal(ConstraintsBagDto v) throws Exception {
		return v.toConstraintsBag();
	}

	@Override
	public ConstraintsBagDto marshal(ConstraintsBag v) throws Exception {
		return new ConstraintsBagDto(v);
	}

}