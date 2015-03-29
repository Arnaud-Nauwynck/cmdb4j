package org.cmdb4j.core.util;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

public class PatternFileFilter implements FileFilter {

	private List<Pattern> patterns;
	
	public PatternFileFilter(List<Pattern> patterns) {
		this.patterns = patterns;
	}

	public boolean accept(File pathname) {
		boolean res = null != findFirstMatches(pathname.getName(), patterns);
		return res;
	}

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
	
}