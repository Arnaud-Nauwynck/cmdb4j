package org.cmdb4j.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * utility methods on string list
 */
public final class StringListUtils {

    private StringListUtils() {
    }
    
	public static Pattern findFirstMatches(String text, Collection<Pattern> patterns) {
		Pattern res = null;
		for(Pattern pattern : patterns) {
			if (pattern.matcher(text).matches()) {
				res = pattern;
				break;
			}
		}
		return res;
	}

	public static String findFirstEndsWith(String text, Collection<String> ends) {
		String res = null;
		for(String end : ends) {
			if (text.endsWith(end)) {
				res = end;
				break;
			}
		}
		return res;
	}

	public static String findFirstEndsWith(String text, String[] ends) {
		return findFirstEndsWith(text, Arrays.asList(ends));
	}

	public static String findFirstStartsWith(String text, Collection<String> starts) {
		String res = null;
		for(String start : starts) {
			if (text.startsWith(start)) {
				res = start;
				break;
			}
		}
		return res;
	}

	public static String findFirstStartsWith(String text, String[] starts) {
		return findFirstStartsWith(text, Arrays.asList(starts));
	}


}
