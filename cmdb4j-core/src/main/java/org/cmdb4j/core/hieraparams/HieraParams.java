package org.cmdb4j.core.hieraparams;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class HieraParams {

	private Map<HieraPath,Map<String,String>> path2OverrideParams;

	// ------------------------------------------------------------------------

	public HieraParams(Map<HieraPath, Map<String, String>> path2OverrideParams) {
		this.path2OverrideParams = path2OverrideParams;
	}
	
	public HieraParams() {
		this(new HashMap<HieraPath,Map<String,String>>());
	}
	
	// ------------------------------------------------------------------------
	
	public String putOverride(HieraPath path, String key, String value) {
		Map<String, String> overrides = getOrCreatePathOverrides(path);
		return overrides.put(key, value);
	}
	
	public void putAllOverrides(HieraPath path, Map<String, String> params) {
		Map<String, String> overrides = getOrCreatePathOverrides(path);
		overrides.putAll(params);
	}
	
	public String removeOverride(HieraPath path, String key) {
		Map<String, String> overrides = getPathOverridesOrNull(path);
		if (overrides == null) {
			return null;
		}
		return overrides.remove(key);
	}

	public String getOverride(HieraPath path, String key) {
		Map<String, String> overrides = getPathOverridesOrNull(path);
		return (overrides != null)? overrides.get(key) : null;
	}

	public Map<String, String> getOrCreatePathOverrides(HieraPath path) {
		Map<String, String> res = path2OverrideParams.get(path);
		if (res == null) {
			res = new HashMap<String,String>();
			path2OverrideParams.put(path, res);
		}
		return res;
	}
	
	public Map<String, String> getPathOverridesOrNull(HieraPath path) {
		return path2OverrideParams.get(path);
	}
	
	public Map<String,String> resolveAllParamsFor(HieraPath... paths) {
		return resolveAllParamsFor(Arrays.asList(paths));
	}
	
	public Map<String,String> resolveAllParamsFor(Collection<HieraPath> paths) {
		Map<String,String> res = new HashMap<String,String>();
		collectAllParamsFor(res, paths);
		return res;
	}

	private void collectAllParamsFor(Map<String, String> res, Collection<HieraPath> paths) {
		for(HieraPath path : paths) {
			collectAllParamsFor(res, path);
		}
	}

	public void collectAllParamsFor(Map<String, String> res, HieraPath path) {
		int len = path.size();
		for (int i = 0; i <= len; i++) {
			HieraPath ancestorPath = path.subPath(0, i);
			Map<String, String> params = path2OverrideParams.get(ancestorPath);
			if (params != null) {
				res.putAll(params);
			}
		}
	}

	// ------------------------------------------------------------------------
	
}
