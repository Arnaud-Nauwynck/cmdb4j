package org.cmdb4j.core.util.props;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public abstract class ImmutableProps implements IProps {

	public abstract String get(String key);

	public abstract boolean isEmpty();

	// public abstract Iterator<Map.Entry<String,String>> entryIterator();

	public abstract void eachEntry(KeyValueConsumer callback);

	public Map<String, String> getMapCopy() {
		Map<String, String> res = new LinkedHashMap<>();
		eachEntry((key, value) -> {
			res.put(key, value);
		});
		return res;
	}

	// ------------------------------------------------------------------------

	public static ImmutableProps of(Properties p) {
		return new PropertiesImmutableProps(MapStringUtils.asMap(p));
	}

	public static ImmutableProps of(Map<String, String> p) {
		return new PropertiesImmutableProps(p);
	}

	public static ImmutableProps of(Props p) {
		return new PropsImmutableProps(p);
	}

	// -----------------------------------------------------------------------

	public static class PropertiesImmutableProps extends ImmutableProps {
		private Map<String, String> delegate;

		public PropertiesImmutableProps(Map<String, String> delegate) {
			this.delegate = delegate;
		}

		@Override
		public String get(String key) {
			return delegate.get(key);
		}

		@Override
		public boolean containsKey(String key) {
			return delegate.containsKey(key);
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public void eachEntry(KeyValueConsumer callback) {
			for (Map.Entry<String, String> e : delegate.entrySet()) {
				String key = e.getKey(), value = e.getValue();
				callback.accept(key, value);
			}
		}
	}

	// ------------------------------------------------------------------------

	public static class PropsImmutableProps extends ImmutableProps {
		private Props delegate;

		public PropsImmutableProps(Props delegate) {
			this.delegate = delegate;
		}

		public Props getDelegate() {
			return delegate;
		}

		@Override
		public String get(String key) {
			return delegate.get(key);
		}

		@Override
		public boolean containsKey(String key) {
			return delegate.containsKey(key);
		}

		@Override
		public boolean isEmpty() {
			return delegate.isEmpty();
		}

		@Override
		public void eachEntry(KeyValueConsumer callback) {
			delegate.eachEntry(callback);
		}
	}

}
