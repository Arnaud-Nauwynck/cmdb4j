package org.cmdb4j.core.util.props;

import java.util.Map;
import java.util.Properties;

public class MapStringUtils {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, String> asMap(Properties p) {
		return (Map<String, String>) (Map) p;
	}

}
