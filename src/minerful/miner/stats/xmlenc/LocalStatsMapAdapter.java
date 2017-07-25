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
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import minerful.concept.xmlenc.CharAdapter;
import minerful.miner.stats.StatsCell;

public class LocalStatsMapAdapter extends XmlAdapter<LocalStatsMapAdapter.KeyValueList, Map<Character, StatsCell>>{
	@XmlType(name="localStats")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class KeyValueList {
		@XmlType(name="localStat")
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Item {
			// This is due to the ridiculous bug of the Metro implementation of JAXB.
			// Without an Adapter, you would have an Integer encoding the Character as a result.
			@XmlAttribute(name="task")
			@XmlJavaTypeAdapter(value=CharAdapter.class)
			public Character key;
			@XmlElement(name="details")
			public StatsCell value;

			public Item(Character key, StatsCell value) {
				this.key = key;
				this.value = value;
			}
			public Item() {
			}
		}

		@XmlElements({
			@XmlElement(name="interplayStatsWith"),
		})
		public final List<Item> list;
		
		public KeyValueList() {
			this.list = new ArrayList<LocalStatsMapAdapter.KeyValueList.Item>();
		}
		public KeyValueList(List<Item> list) {
			this.list = list;
		}
	}

	@Override
	public LocalStatsMapAdapter.KeyValueList marshal(
			Map<Character, StatsCell> v) throws Exception {
		Set<Character> keys = v.keySet();
		ArrayList<LocalStatsMapAdapter.KeyValueList.Item> results = new ArrayList<LocalStatsMapAdapter.KeyValueList.Item>(v.size());
        for (Character key : keys) {
            results.add(new LocalStatsMapAdapter.KeyValueList.Item(key, v.get(key)));
        }
        return new KeyValueList(results);
	}

	@Override
	public Map<Character, StatsCell> unmarshal(
			LocalStatsMapAdapter.KeyValueList v)
			throws Exception {
		Map<Character, StatsCell> globalStatsMap = new HashMap<Character, StatsCell>(v.list.size());
		for (LocalStatsMapAdapter.KeyValueList.Item keyValue : v.list) {
			globalStatsMap.put(keyValue.key, keyValue.value);
		}
		return globalStatsMap;
	}
}
