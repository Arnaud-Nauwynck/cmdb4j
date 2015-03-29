package org.cmdb4j.core.repo.dir.propfiles;

import java.util.List;

public class HieraParamPropertiesDef {

	public static class HieraParamPropertiesFileDef {
		private final String propertyFileName;
		private final String[] excludedPropertiesPrefix;
		private final String[] includedPropertiesPrefix;
		
		public HieraParamPropertiesFileDef(String propertyFileName, 
				String[] excludedPropertiesPrefix,
				String[] includedPropertiesPrefix) {
			this.propertyFileName = propertyFileName;
			this.excludedPropertiesPrefix = excludedPropertiesPrefix;
			this.includedPropertiesPrefix = includedPropertiesPrefix;
		}

		public String getPropertyFileName() {
			return propertyFileName;
		}

		public String[] getExcludedPropertiesPrefix() {
			return excludedPropertiesPrefix;
		}

		public String[] getIncludedPropertiesPrefix() {
			return includedPropertiesPrefix;
		}

		/**
		 * @return <<inlinedName>> when name matches "<<inlinedName>>-<<propertyFileName>>", otherwise null
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
		
	}
	

	private final HieraParamPropertiesFileDef[] propFileDefs;

	// ------------------------------------------------------------------------
	
	public HieraParamPropertiesDef(HieraParamPropertiesFileDef[] propFileDefs) {
		this.propFileDefs = propFileDefs;
	}
	
	// ------------------------------------------------------------------------

	public String nameMatchToInlinedName(String name) {
		String res = null;
		for(HieraParamPropertiesFileDef propFileDef : propFileDefs) {
			res = propFileDef.nameMatchToInlinedName(name);
			if (res != null) {
				break;
			}
		}
		return res;
	}
	
}