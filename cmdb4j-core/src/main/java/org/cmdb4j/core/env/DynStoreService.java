package org.cmdb4j.core.env;

/**
 * TODO ARN
 *
 */
public abstract class DynStoreService {

	public abstract String get(String key);
	public abstract void set(String key, String value);

}
