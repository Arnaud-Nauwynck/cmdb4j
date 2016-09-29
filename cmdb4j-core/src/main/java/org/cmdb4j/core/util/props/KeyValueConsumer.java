package org.cmdb4j.core.util.props;

@FunctionalInterface
public interface KeyValueConsumer {

	public void accept(String key, String value);

}
