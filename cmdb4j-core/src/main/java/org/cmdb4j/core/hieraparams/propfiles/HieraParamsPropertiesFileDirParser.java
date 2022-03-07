package org.cmdb4j.core.hieraparams.propfiles;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.util.IOUtils;
import org.cmdb4j.core.util.PathId;

/**
 * HieraParams directory properties files parser for "* * / *.properties"
 * 
 * a typical param directory layout may be:
 * <PRE>
 * / <<rootParamsDir>>
 * 
 * +/ Default/
 *       dev/
 *         param.properties
 *         team1/
 *           param.properties
 *           app1/
 *             param.properties
 *           app2/
 *             param.properties
 *         team2/
 *         ..  
 *           
 *       prod/
 *         team1/
 *           param.properties
 *           app1/
 *              param.properties
 *         team2/
 *           ..
 *         per-zone/
 *           ..
 *           
 *  /DEV1   (env 1 for DEV)
 *  /UAT1   (env 1 for User Acceptance Test)
 *  /INT1   (env 1 for Integration)
 *  /PROD   ... the one
 *  /ISOPROD1   (env 1 for PROD-like app versions, day minus 1 databases)
 *  
 *  /<<env>>
 *    param.properties
 *    /team1
 *      param.properties
 *    	/app1
 *         param.properties
 *    /infra
 *      /host1   (either real host, virtual machine, or host parameters template)
 *        param.properties
 *        /container1
 *          param.properties
 *      /host2
 *        param.properties
 *        /container1
 *          param.properties
 * </PRE>
 */
public class HieraParamsPropertiesFileDirParser {

	private HieraParamsPropertiesFilesDef propDef;
	
	// ------------------------------------------------------------------------
	
	public HieraParamsPropertiesFileDirParser(HieraParamsPropertiesFilesDef propDef) {
		this.propDef = propDef;
	}

	public static HieraParamsPropertiesFileDirParser defaultParamsPropParser() {
		HieraParamPropertiesFileDef paramPropFileDef = 
				new HieraParamPropertiesFileDef("param.properties", null, null, null);
		HieraParamPropertiesFileDef[] propFilesDef = new HieraParamPropertiesFileDef[] { paramPropFileDef  };
		HieraParamsPropertiesFilesDef propDef = new HieraParamsPropertiesFilesDef(propFilesDef);
		return new HieraParamsPropertiesFileDirParser(propDef);
	}

	public static HieraParamsPropertiesFileDirParser defaultObjPropParser() {
	    HieraParamPropertiesFileDef objPropFileDef = 
	            new HieraParamPropertiesFileDef("obj.properties", null, null, null);
	    HieraParamPropertiesFileDef[] propFilesDef = new HieraParamPropertiesFileDef[] { objPropFileDef  };
	    HieraParamsPropertiesFilesDef propDef = new HieraParamsPropertiesFilesDef(propFilesDef);
	    return new HieraParamsPropertiesFileDirParser(propDef);
	}

	// ------------------------------------------------------------------------
	
	public HieraParams loadHieraFromDir(File rootDir) {
		HieraParams res = new HieraParams();
		PathId rootPath = PathId.emptyPath();
		IOUtils.checkDirExists(rootDir);

		recursiveLoadPropertiesFromDir(res, rootDir, rootPath);
		return res;
	}

	private void recursiveLoadPropertiesFromDir(HieraParams res, 
			File currDir, PathId currPath) {
		File[] childFiles = currDir.listFiles();
		if (childFiles != null && childFiles.length != 0) {
			for(File childFile : childFiles) {
				String fileName = childFile.getName();
				if (fileName.startsWith(".")) {
					continue; // ignore metadata .git, .svn, etc...
				}
				PathId childPath = currPath.child(fileName);
				
				if (childFile.isDirectory()) {
					// chlid is dir => recurse
					recursiveLoadPropertiesFromDir(res, childFile, childPath);
				} else {
					// child is regular file => check for match, load properties + add as params override
					HieraParamPropertiesFileDef propFileDef = propDef.findFirstMatchPropFileDef(fileName);
					if (propFileDef == null) {
						// ignore file
					} else {
						Properties props = IOUtils.loadPropertiesFile(childFile);
						@SuppressWarnings("unchecked")
						Map<String,String> propMap = (Map<String,String>) (Map<?,?>) props;
						propFileDef.putPropertiesOverrideToHieraParams(res, currPath, fileName, propMap);
					}
				}
			}
		}
	}

	
	@Override
	public String toString() {
		return "HieraParamsPropertiesFileDirParser [propDef=" + propDef + "]";
	}


}
