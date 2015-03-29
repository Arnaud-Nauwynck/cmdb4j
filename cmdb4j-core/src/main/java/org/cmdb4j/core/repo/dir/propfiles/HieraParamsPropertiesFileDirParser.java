package org.cmdb4j.core.repo.dir.propfiles;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.hieraparams.HieraPath;
import org.cmdb4j.core.repo.dir.propfiles.HieraParamPropertiesDef.HieraParamPropertiesFileDef;
import org.cmdb4j.core.util.IOUtils;

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
 *         per-zone/
 *           Amer/
 *             param.properties
 *           Europe/
 *             param.properties
 *           ..
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
 *  /QNR1   (env 1 for Quality Non Regression)
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

	private HieraParamPropertiesDef propDef;
	
	// ------------------------------------------------------------------------
	
	public HieraParamsPropertiesFileDirParser(HieraParamPropertiesDef propDef) {
		this.propDef = propDef;
	}

	public static HieraParamsPropertiesFileDirParser defaultParamsPropParser() {
		HieraParamPropertiesFileDef paramPropFileDef = 
				new HieraParamPropertiesFileDef("param.properties", null, null);
		HieraParamPropertiesFileDef[] propFilesDef = new HieraParamPropertiesFileDef[] { paramPropFileDef  };
		HieraParamPropertiesDef propDef = new HieraParamPropertiesDef(propFilesDef);
		return new HieraParamsPropertiesFileDirParser(propDef);
	}
	
	// ------------------------------------------------------------------------
	
	public HieraParams loadHieraFromDir(File rootDir) {
		HieraParams res = new HieraParams();
		HieraPath rootPath = HieraPath.emptyPath();
		IOUtils.checkDirExists(rootDir);

		recursiveLoadPropertiesFromDir(res, rootDir, rootPath);
		return res;
	}

	private void recursiveLoadPropertiesFromDir(HieraParams res, 
			File currDir, HieraPath currPath) {
		File[] childFiles = currDir.listFiles();
		if (childFiles != null && childFiles.length != 0) {
			for(File childFile : childFiles) {
				String fileName = childFile.getName();
				if (fileName.startsWith(".")) {
					continue; // ignore metadata .git, .svn, etc...
				}
				HieraPath childPath = currPath.child(fileName);
				
				if (childFile.isDirectory()) {
					// chlid is dir => recurse
					recursiveLoadPropertiesFromDir(res, childFile, childPath);
				} else {
					// child is regular file => load properties + add as params override
					String inlinedName = propDef.nameMatchToInlinedName(fileName);
					if (inlinedName != null) {
						if (inlinedName.isEmpty()) {
							childPath = currPath;
						} else {
							childPath = currPath.parent().child(inlinedName);
						}
					}
					
					Properties props = loadPropertiesFile(childFile);
					Map<String,String> params = new HashMap<String,String>();
					params.putAll((Map<String,String>) (Map) props);
					res.putAllOverrides(childPath, params);
				}
			}
		}
	}
	
	private static Properties loadPropertiesFile(File file) {
		Properties props = new Properties();
		InputStream inStream = null;
		try {
			inStream = new BufferedInputStream(new FileInputStream(file));
			props.load(inStream);
		} catch(IOException ex) {
			throw new RuntimeException("Failed to read prop file '" + file + "'", ex);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
		return props;
	}
	
}
