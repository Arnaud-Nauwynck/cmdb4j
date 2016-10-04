package org.cmdb4j.core.util.props;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableSet;

/**
 * replacement for java.util.Properties .. for keeping track of properties
 * detail location
 *
 */
public class Props implements IProps {

	public static final Supplier<Props> SUPPLIER = () -> new Props();

	private static class Entry {
		final String key;
		String value;
		String source;

		public Entry(String key, String value, String source) {
			this.key = key;
			this.value = value;
			this.source = source;
		}

		public void set(String value, String source) {
			this.value = value;
			this.source = source;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String getSource() {
			return source;
		}

	}

	private Props parent;

	private final Map<String, Entry> entries = new LinkedHashMap<>();

	// ------------------------------------------------------------------------

	public static Props newWithParent(Props parent, Props override) {
		Props res = new Props();
		res.parent = parent;
		if (parent != null) {
			res.putAll(parent);
		}
		if (override != null) {
			res.putAll(override);
		}
		return res;
	}

	public Props() {
	}

	public Props(Props src) {
		putAll(src);
	}

	// public Props(IProps src, String source) {
	// putAll(src, source);
	// }

	// ------------------------------------------------------------------------

	public Props getParent() {
		return parent;
	}

	public String get(String key) {
		String res;
		Entry e = entries.get(key);
		if (e != null) {
			res = e.value;
		} else if (parent != null) {
			res = parent.get(key);
		} else {
			res = null;
		}
		return res;
	}

	public String getProperty(String key, String defaultValue) {
		String res = get(key);
		if (res == null && !entries.containsKey(key)) {
			res = defaultValue;
		}
		return res;
	}

	public String getSource(String key) {
		String res;
		Entry e = entries.get(key);
		if (e != null) {
			res = e.source;
		} else if (parent != null) {
			res = parent.getSource(key);
		} else {
			res = null;
		}
		return res;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public int size() {
		return entries.size();
	}

	@FunctionalInterface
	public static interface KeyValueSourceConsumer {
		public void accept(String key, String value, String source);
	}

	public void eachEntry(KeyValueConsumer callback) {
		for (Entry e : entries.values()) {
			String key = e.getKey(), value = e.getValue();
			callback.accept(key, value);
		}
	}

	public void eachEntrySource(KeyValueSourceConsumer callback) {
		for (Entry e : entries.values()) {
			String key = e.getKey(), value = e.getValue(), source = e.getSource();
			callback.accept(key, value, source);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void putAll(Properties src, String source) {
		putAll((Map<String, String>) (Map) src, source);
	}

	public void putAll(Props src) {
		for (Entry e : src.entries.values()) {
			put(e.getKey(), e.getValue(), e.getSource());
		}
	}

	public void putAll(Props src, String source) {
		for (Entry e : src.entries.values()) {
			put(e.getKey(), e.getValue(), source);
		}
	}

	public void putAll(IProps src, String source) {
		src.eachEntry((key, value) -> {
			put(key, value, source);
		});
	}

	public void putAll(Map<String, String> overloadProps, String source) {
		for (Map.Entry<String, String> e : overloadProps.entrySet()) {
			put(e.getKey(), e.getValue(), source);
		}
	}

	public void put(String key, String value, String source) {
		Entry e = entries.get(key);
		if (e == null) {
			e = new Entry(key, value, source);
			entries.put(key, e);
		}
		e.set(value, source);
	}

	public void remove(String key) {
		entries.remove(key);
	}

	@FunctionalInterface
	public static interface KeyValuePredicate {
		public boolean accept(String key, String value);
	}

	public void removeWhere(KeyValuePredicate pred) {
		for (Iterator<Entry> iter = entries.values().iterator(); iter.hasNext();) {
			Entry e = iter.next();
			if (pred.accept(e.getKey(), e.getValue())) {
				iter.remove();
			}
		}
	}

	public boolean containsKey(String key) {
		return entries.containsKey(key);
	}

	public ImmutableSet<String> keySet() {
		LinkedHashSet<String> tmp = new LinkedHashSet<String>();
		for (Entry e : entries.values()) {
			tmp.add(e.getKey());
		}
		return ImmutableSet.copyOf(tmp);
	}

	public Map<String, String> getMapCopy() {
		Map<String, String> res = new LinkedHashMap<String, String>();
		for (Entry e : entries.values()) {
			res.put(e.getKey(), e.getValue());
		}
		return res;
	}

	public static class PropsEntry {
		public String key, value, source;

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String getSource() {
			return source;
		}

		/* pp */void set(String key, String value, String source) {
			this.key = key;
			this.value = value;
			this.source = source;
		}
	}

	public Iterable<PropsEntry> entrySet() {
		return new Iterable<PropsEntry>() {
			@Override
			public Iterator<PropsEntry> iterator() {
				return new PropsEntryIterator(entries.values().iterator()); // may
																			// make
																			// value
																			// copy?
			}
		};
	}

	protected static class PropsEntryIterator implements Iterator<PropsEntry> {
		private Iterator<Entry> currIter;
		private PropsEntry next = new PropsEntry();

		PropsEntryIterator(Iterator<Entry> iter) {
			this.currIter = iter;
		}

		@Override
		public boolean hasNext() {
			return currIter.hasNext();
		}

		@Override
		public PropsEntry next() {
			Entry e = currIter.next();
			next.set(e.getKey(), e.getValue(), e.getSource());
			return next;
		}
	}

}
