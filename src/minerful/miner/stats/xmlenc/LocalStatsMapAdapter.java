package minerful.miner.stats.xmlenc;

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
import minerful.miner.stats.StatsCell;

public class LocalStatsMapAdapter extends XmlAdapter<LocalStatsMapAdapter.KeyValueList, Map<TaskChar, StatsCell>>{
	@XmlType(name="localStats")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class KeyValueList {
		@XmlType(name="localStat")
		@XmlAccessorType(XmlAccessType.FIELD)
		public static class Item {
			@XmlElement
			public TaskChar key;
			@XmlElement(name="details")
			public StatsCell value;

			public Item(TaskChar key, StatsCell value) {
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
			Map<TaskChar, StatsCell> v) throws Exception {
		Set<TaskChar> keys = v.keySet();
		ArrayList<LocalStatsMapAdapter.KeyValueList.Item> results = new ArrayList<LocalStatsMapAdapter.KeyValueList.Item>(v.size());
        for (TaskChar key : keys) {
            results.add(new LocalStatsMapAdapter.KeyValueList.Item(key, v.get(key)));
        }
        return new KeyValueList(results);
	}

	@Override
	public Map<TaskChar, StatsCell> unmarshal(
			LocalStatsMapAdapter.KeyValueList v)
			throws Exception {
		Map<TaskChar, StatsCell> globalStatsMap = new HashMap<TaskChar, StatsCell>(v.list.size());
		for (LocalStatsMapAdapter.KeyValueList.Item keyValue : v.list) {
			globalStatsMap.put(keyValue.key, keyValue.value);
		}
		return globalStatsMap;
	}
}
