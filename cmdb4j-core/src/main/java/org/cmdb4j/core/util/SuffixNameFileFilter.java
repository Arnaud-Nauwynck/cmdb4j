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
		boolean res = null != StringListUtils.findFirstEndsWith(pathname.getName(), noEndsWith);
		return res;
	}
	
}