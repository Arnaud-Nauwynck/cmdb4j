package org.cmdb4j.core.util.props;

import java.util.Map;

public interface IProps {

	public String get(String key);

	public boolean containsKey(String key);

	public boolean isEmpty();

	// public Iterator<Map.Entry<String,String>> entryIterator();

	public void eachEntry(KeyValueConsumer callback);

	public Map<String, String> getMapCopy();

}
