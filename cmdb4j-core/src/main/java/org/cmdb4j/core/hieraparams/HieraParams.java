package org.cmdb4j.core.hieraparams;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.util.PathId;

/**
 * 
 */
public class HieraParams {

	private Map<PathId,Map<String,String>> path2OverrideParams;

	// ------------------------------------------------------------------------

	public HieraParams(Map<PathId, Map<String, String>> path2OverrideParams) {
		this.path2OverrideParams = path2OverrideParams;
	}
	
	public HieraParams() {
		this(new HashMap<PathId,Map<String,String>>());
	}
	
	// ------------------------------------------------------------------------
	
	public String putOverride(PathId path, String key, String value) {
		Map<String, String> overrides = getOrCreatePathOverrides(path);
		return overrides.put(key, value);
	}
	
	public void putAllOverrides(PathId path, Map<String, String> params) {
		Map<String, String> overrides = getOrCreatePathOverrides(path);
		overrides.putAll(params);
	}
	
	public String removeOverride(PathId path, String key) {
		Map<String, String> overrides = getPathOverridesOrNull(path);
		if (overrides == null) {
			return null;
		}
		return overrides.remove(key);
	}

	public String getOverride(PathId path, String key) {
		Map<String, String> overrides = getPathOverridesOrNull(path);
		return (overrides != null)? overrides.get(key) : null;
	}

	public Map<String, String> getOrCreatePathOverrides(PathId path) {
		Map<String, String> res = path2OverrideParams.get(path);
		if (res == null) {
			res = new HashMap<String,String>();
			path2OverrideParams.put(path, res);
		}
		return res;
	}
	
	public Map<String, String> getPathOverridesOrNull(PathId path) {
		return path2OverrideParams.get(path);
	}

	// resolve Params
   // ------------------------------------------------------------------------

	public Map<String,String> resolveAllParamsFor(PathId... paths) {
		return resolveAllParamsFor(Arrays.asList(paths));
	}
	
	public Map<String,String> resolveAllParamsFor(Collection<PathId> paths) {
		Map<String,String> res = new HashMap<String,String>();
		collectAllParamsFor(res, paths);
		return res;
	}

	private void collectAllParamsFor(Map<String, String> res, Collection<PathId> paths) {
		for(PathId path : paths) {
			collectAllParamsFor(res, path);
		}
	}

	public void collectAllParamsFor(Map<String, String> res, PathId path) {
		int len = path.size();
		for (int i = 0; i <= len; i++) {
			PathId ancestorPath = path.subPath(0, i);
			Map<String, String> params = path2OverrideParams.get(ancestorPath);
			if (params != null) {
				res.putAll(params);
			}
		}
	}

	
	public static interface HieraPathParamsVisitor {
	    public void visit(PathId path, Map<String, String> resolvedParams);
	}

	public void scanPathWithResolveParams(HieraPathParamsVisitor visitor, PathId ancestorPath) {
	    // TODO inneficient implementation ... but fast enough for very small data 
	    for(Map.Entry<PathId,Map<String,String>> e : path2OverrideParams.entrySet()) {
	        PathId path = e.getKey();
	        if (ancestorPath != null && ! path.startsWith(ancestorPath)) {
	            return;
	        }
	        Map<String,String> resolvedParams = resolveAllParamsFor(path);
	        visitor.visit(path, resolvedParams);
	    }
	}
	
}
