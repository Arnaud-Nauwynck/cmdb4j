package org.cmdb4j.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Path for HieraParams
 * <p/>
 * similar to Path for file, but relative to root hierarchy of repo, 
 * not to root dir where repo may be stored
 * 
 */
public final class PathId implements Comparable<PathId> {

	private static final String[] EMPTY_PATH_ELEMENTS = new String[0];
	
	private final String[] pathElements;

	// ------------------------------------------------------------------------
	
	private PathId(String[] pathElements) {
		this.pathElements = pathElements;
	}
	
	public static PathId valueOf(String... pathElements) {
		return new PathId(splitEltsToArray(pathElements));
	}
	
	public static PathId emptyPath() {
		return new PathId(EMPTY_PATH_ELEMENTS);
	}

	// ------------------------------------------------------------------------

	public PathId child(String... childPathElements) {
		List<String> tmp = new ArrayList<String>(pathElements.length + childPathElements.length);
		tmp.addAll(toList());
		splitAddElts(tmp, childPathElements);
		return new PathId(tmp.toArray(new String[tmp.size()]));
	}

	public PathId parent() {
		if (pathElements.length == 0) return this;
		String[] res = new String[pathElements.length - 1];
		System.arraycopy(pathElements, 0, res, 0, pathElements.length - 1);
		return new PathId(res);
	}

	public PathId subPath(int from, int to) {
		if (from == 0 && to == pathElements.length) return this;
		int len = to - from;
		String[] res = new String[len];
		System.arraycopy(pathElements, from, res, 0, len);
		return new PathId(res);
	}
	
	private static void splitAddElts(List<String> res, String... elts) {
		for(String elt : elts) {
			res.addAll(Arrays.asList(elt.split("/")));
		}
	}
	
	private static String[] splitEltsToArray(String... elts) {
		List<String> res = new ArrayList<String>(elts.length);
		splitAddElts(res, elts);
		return res.toArray(new String[res.size()]);
	}

	
	public PathId child(PathId childPath) {
		return child(childPath.pathElements);
	}
	
	public int size() {
		return pathElements.length;
	}

	public String get(int i) {
		return pathElements[i];
	}

	public List<String> toList() {
		return Arrays.asList(pathElements);
	}

	public boolean startsWith(PathId prefix) {
	    final int prefixSize = prefix.size();
        if (size() < prefixSize) return false;
	    boolean res = true;
	    for(int i = 0; i < prefixSize; i++) {
	        if (! get(i).equals(prefix.get(i))) {
	            res = false;
	            break;
	        }
	    }
	    return res;
	}

	public boolean endsWith(PathId suffix) {
        final int suffixSize = suffix.size();
        if (size() < suffixSize) return false;
        boolean res = true;
        for(int i = size() - 1, suffixI = suffixSize - 1; suffixI >= 0; i--,suffixI--) {
            if (! get(i).equals(suffix.get(suffixI))) {
                res = false;
                break;
            }
        }
        return res;
    }

	// ------------------------------------------------------------------------
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pathElements);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathId other = (PathId) obj;
		if (!Arrays.equals(pathElements, other.pathElements))
			return false;
		return true;
	}

	
	public int compareTo(PathId other) {
		int res = 0;
		int len = Math.min(pathElements.length, other.pathElements.length);
		for(int i = 0; i < len; i++) {
			res = pathElements[i].compareTo(other.pathElements[i]);
			if (res != 0) {
				return res;
			}
		}
		return Integer.compare(pathElements.length, other.pathElements.length);
	}

	@Override
	public String toString() {
        StringBuilder b = new StringBuilder();
        final int len = pathElements.length;
        for (int i = 0; i < len; i++) {
            b.append(pathElements[i]);
            if (i != len-1) {
                b.append('/');
            }
        }
		return b.toString();
	}

}
