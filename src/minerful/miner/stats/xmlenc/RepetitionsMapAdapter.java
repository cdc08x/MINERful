package minerful.miner.stats.xmlenc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RepetitionsMapAdapter extends XmlAdapter<RepetitionsMapAdapter.KeyValueList, Map<Integer, Integer>>{
	@XmlType(name="repetitionsCollection")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class KeyValueList {
		@XmlType(name="repetitionsCounter")
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Item {
			@XmlAttribute(name="times")
			public Integer key;
			@XmlElement(name="counted")
			public Integer value;

			public Item(Integer key, Integer value) {
				this.key = key;
				this.value = value;
			}
			public Item() {
			}
		}

		@XmlElementWrapper(name="repetitionsCounters")
		@XmlElement(name="repetitionsCounter")
		public final List<Item> list;
		
		public KeyValueList() {
			this.list = new ArrayList<RepetitionsMapAdapter.KeyValueList.Item>();
		}
		public KeyValueList(List<Item> list) {
			this.list = list;
		}
	}

	@Override
	public KeyValueList marshal(
			Map<Integer, Integer> v) throws Exception {
		Set<Integer> keys = v.keySet();
		ArrayList<RepetitionsMapAdapter.KeyValueList.Item> results = new ArrayList<RepetitionsMapAdapter.KeyValueList.Item>(v.size());
        for (Integer key : keys) {
            results.add(new RepetitionsMapAdapter.KeyValueList.Item(key, v.get(key)));
        }
        return new RepetitionsMapAdapter.KeyValueList(results);
	}

	@Override
	public Map<Integer, Integer> unmarshal(
			KeyValueList v)
			throws Exception {
		Map<Integer, Integer> repetitionsMap = new HashMap<Integer, Integer>(v.list.size());
		for (RepetitionsMapAdapter.KeyValueList.Item item : v.list) {
			repetitionsMap.put(item.key, item.value);
		}
		return repetitionsMap;
	}
}
