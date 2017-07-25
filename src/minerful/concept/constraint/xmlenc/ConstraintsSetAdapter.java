package minerful.concept.constraint.xmlenc;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import minerful.concept.constraint.Constraint;

@XmlRootElement
public class ConstraintsSetAdapter extends XmlAdapter<ConstraintsSetAdapter.SetList, Set<Constraint>>{
	public static class SetList {
		@XmlElements({
			@XmlElement(type=Constraint.class, name="constraint"),
		})
		public ArrayList<Constraint> list = null;

		private SetList() {}
		public SetList(Set<Constraint> list) {
			this();
			this.list = new ArrayList<Constraint>(list);
		}
		
		public Set<Constraint> getSetList() {
			return new TreeSet<Constraint>(this.list);
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("SetList [list=");
			builder.append(list);
			builder.append("]");
			return builder.toString();
		}
	}
	
	@XmlElement(name="constraints")
	public ConstraintsSetAdapter.SetList list;
	
	private ConstraintsSetAdapter() {}

	public ConstraintsSetAdapter(Set<Constraint> value) {
		this();
		this.list = new ConstraintsSetAdapter.SetList(value);
	}

	@Override
	public ConstraintsSetAdapter.SetList marshal(
			Set<Constraint> v) throws Exception {
		return new ConstraintsSetAdapter.SetList(v);
	}

	@Override
	public Set<Constraint> unmarshal(
			ConstraintsSetAdapter.SetList v)
			throws Exception {
		return v.getSetList();
	}
}
