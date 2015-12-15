package minerful.concept.constraint.xmlenc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import minerful.concept.TaskChar;
import minerful.concept.constraint.Constraint;

public class ConstraintsBagMapAdapter extends XmlAdapter<ConstraintsBagMapAdapter.KeyValueList, Map<TaskChar, Set<Constraint>>> {
	@XmlType(name="distances")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class KeyValueList {
		@XmlType(name="distance")
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Item {			
			@XmlElement(name="at")
			public TaskChar key;
			public Set<Constraint> value;

			public Item(TaskChar key, Set<Constraint> value) {
				this.key = key;
				this.value = value;
			}
			public Item() {
			}
			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("Item [key=");
				builder.append(key);
				builder.append(", value=");
				builder.append(value);
				builder.append("]");
				return builder.toString();
			}
		}

		@XmlElements({
			@XmlElement(name="constraints")
		})
		public final List<Item> list;
		
		public KeyValueList() {
			this.list = new ArrayList<ConstraintsBagMapAdapter.KeyValueList.Item>();
		}
		public KeyValueList(List<Item> list) {
			this.list = list;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("KeyValueList [list=");
			builder.append(list);
			builder.append("]");
			return builder.toString();
		}
	}

	@Override
	public KeyValueList marshal(
			Map<TaskChar, Set<Constraint>> v) throws Exception {
		Set<TaskChar> keys = v.keySet();
		ArrayList<ConstraintsBagMapAdapter.KeyValueList.Item> results = new ArrayList<ConstraintsBagMapAdapter.KeyValueList.Item>(v.size());
        for (TaskChar key : keys) {
            results.add(new ConstraintsBagMapAdapter.KeyValueList.Item(key, v.get(key)));
        }
        return new ConstraintsBagMapAdapter.KeyValueList(results);
	}

	@Override
	public Map<TaskChar, Set<Constraint>> unmarshal(
			KeyValueList v)
			throws Exception {
		Map<TaskChar, Set<Constraint>> repetitionsMap = new HashMap<TaskChar, Set<Constraint>>(v.list.size());
		for (ConstraintsBagMapAdapter.KeyValueList.Item item : v.list) {
			repetitionsMap.put(item.key, item.value);
		}
		return repetitionsMap;
	}
}