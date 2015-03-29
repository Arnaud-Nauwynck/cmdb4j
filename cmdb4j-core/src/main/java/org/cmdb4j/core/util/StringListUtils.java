package org.cmdb4j.core.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class StringListUtils {


	public static Pattern findFirstMatches(String text, List<Pattern> patterns) {
		Pattern res = null;
		for(Pattern pattern : patterns) {
			if (pattern.matcher(text).matches()) {
				res = pattern;
				break;
			}
		}
		return res;
	}

	public static String findFirstEndsWith(String text, List<String> ends) {
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

	public static String findFirstStartsWith(String text, List<String> starts) {
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
