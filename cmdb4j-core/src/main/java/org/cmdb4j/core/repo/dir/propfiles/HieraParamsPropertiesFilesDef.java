package org.cmdb4j.core.repo.dir.propfiles;

/**
 * 
 */
public class HieraParamsPropertiesFilesDef {

	private final HieraParamPropertiesFileDef[] propFileDefs;

	// ------------------------------------------------------------------------
	
	public HieraParamsPropertiesFilesDef(HieraParamPropertiesFileDef[] propFileDefs) {
		this.propFileDefs = propFileDefs;
	}
	
	// ------------------------------------------------------------------------

	public HieraParamPropertiesFileDef findFirstMatchPropFileDef(String name) {
		HieraParamPropertiesFileDef res = null;
		for(HieraParamPropertiesFileDef propFileDef : propFileDefs) {
			if (propFileDef.isMatchName(name)) {
				res = propFileDef;
				break;
			}
		}
		return res;
	}
	
}