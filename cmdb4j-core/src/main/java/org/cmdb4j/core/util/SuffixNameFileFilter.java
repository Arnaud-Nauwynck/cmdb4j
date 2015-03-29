package org.cmdb4j.core.util;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class SuffixNameFileFilter implements FileFilter {

	private List<String> noEndsWith;
	
	public SuffixNameFileFilter(List<String> noEndsWith) {
		this.noEndsWith = noEndsWith;
	}

	public boolean accept(File pathname) {
		boolean res = null != findFirstEndsWith(pathname.getName(), noEndsWith);
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
	
}