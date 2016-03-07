package org.cmdb4j.core.hieraparams.propfiles;

import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.util.PathId;
import org.cmdb4j.core.util.StringListUtils;

public class HieraParamPropertiesFileDef {

	private final String propertyFileName;
	private final String[] excludedPropertiesPrefix;
	private final String[] includedPropertiesPrefix;
	private final String[] includedInlinedPropertiesPrefix;
	
	// ------------------------------------------------------------------------
	
	public HieraParamPropertiesFileDef(String propertyFileName, 
			String[] excludedPropertiesPrefix,
			String[] includedPropertiesPrefix,
			String[] includedInlinedPropertiesPrefix) {
		this.propertyFileName = propertyFileName;
		this.excludedPropertiesPrefix = excludedPropertiesPrefix;
		this.includedPropertiesPrefix = includedPropertiesPrefix;
		this.includedInlinedPropertiesPrefix = includedInlinedPropertiesPrefix;
	}

	// ------------------------------------------------------------------------
	
	public String getPropertyFileName() {
		return propertyFileName;
	}

	public String[] getExcludedPropertiesPrefix() {
		return excludedPropertiesPrefix;
	}

	public String[] getIncludedPropertiesPrefix() {
		return includedPropertiesPrefix;
	}

	public boolean isMatchName(String name) {
		return name.endsWith(propertyFileName);
	}
	
	/**
	 * @return <<inlinedName>> when name matches "<<inlinedName>>-<<propertyFileName>>", 
	 *  or empty string when name matches "<<propertyFileName>>", otherwise null
	 */
	public String nameMatchToInlinedName(String name) {
		String res = null;
		if (name.endsWith(propertyFileName)) {
			int prefixLen = name.length() - propertyFileName.length();
			if (prefixLen == 0) {
				res = "";
			} else if (prefixLen > 1 && name.charAt(prefixLen) == '-') {
				res = name.substring(0, prefixLen);
			} // else no match "-" ??
		}
		return res;
	}

	public void putPropertiesOverrideToHieraParams(HieraParams res, PathId currPath, String fileName, Map<String,String> props) {
		PathId hieraPath;
		String inlinedName = nameMatchToInlinedName(fileName);
		if (inlinedName == null) {
			return; // should not occur
		} else {
			if (inlinedName.isEmpty()) {
				hieraPath = currPath;
			} else {
				hieraPath = currPath.parent().child(inlinedName);
			}
		}
		Map<String, String> params = filterAndInlineProperties(props);		
		res.putAllOverrides(hieraPath, params);
	}

	public Map<String, String> filterAndInlineProperties(Map<String, String> props) {
		Map<String,String> res = new HashMap<String,String>();
		for(Map.Entry<String,String> e : props.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			if (excludedPropertiesPrefix != null 
					&& null != StringListUtils.findFirstStartsWith(key, excludedPropertiesPrefix)) {
				// ignore property
			} else {
				String foundInlinedPropPrefix = (includedInlinedPropertiesPrefix != null)?
						StringListUtils.findFirstStartsWith(key, includedInlinedPropertiesPrefix) : null;
				if (foundInlinedPropPrefix != null) {
					// add key, but with pruned prefix "inlined"
					// example: "<<prefix.>><<subKey>> = param" 
					// => put "<<subKey>> = param" 
					String subKey = key.substring(foundInlinedPropPrefix.length());
					res.put(subKey, value);
				} else {
					if (includedPropertiesPrefix == null 
							|| null != StringListUtils.findFirstStartsWith(key, includedPropertiesPrefix)) {
						// include key
						res.put(key, value);
					} else {
						// ignore key!
					}
				}
			}
		}
		return res;
	}
	
	
}