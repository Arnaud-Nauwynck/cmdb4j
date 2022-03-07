package org.cmdb4j.server.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class PropertiesUtils {

	/**
	 * static constructor from reading File
	 * @param file
	 * @return
	 */
	public static Properties readPropertiesFile(File file) {
		Properties res = new Properties(); 
		loadPropertiesFile(file, res);
		return res;
	}

	/**
	 * static constructor from reading File
	 * @param file
	 * @return
	 */
	public static void loadPropertiesFile(File file, Properties res) {
		try (InputStream inStream = new BufferedInputStream(new FileInputStream(file))) {
			res.load(inStream);
		} catch (Exception e) {
			String msg = "Failed to read configuration properties file '" + file.getAbsolutePath() + "'";
			throw new RuntimeException(msg, e);
		}
	}
	
	/**
	 * static constructor from reading File
	 * @param file
	 * @return
	 */
	public static Properties readPropertiesURL(URL url) {
		Properties res = new Properties(); 
		try (InputStream inStream = new BufferedInputStream(url.openStream())) {
			res.load(inStream);
		} catch (Exception e) {
			String msg = "Failed to read configuration properties url '" + url + "'";
			throw new RuntimeException(msg, e);
		}
		return res;
	}

	/**
	 * static constructor from reading File
	 * @param file
	 * @return
	 */
	public static Properties readPropertiesClassResource(ClassLoader cl, String resourceName) {
		Properties res = new Properties(); 
		InputStream inStream = null;
		try {
			inStream = cl.getResourceAsStream(resourceName);
			if (inStream == null) {
				throw new RuntimeException("Can not read classloader resource, resource name not found: '" + resourceName + "'");	
			}
			inStream = new BufferedInputStream(inStream);
			res.load(inStream);
		} catch (Exception e) {
			String msg = "Failed to read configuration properties resource '" + resourceName + "'";
			throw new RuntimeException(msg, e);
		} finally {
			CloseableIOUtils.closeQuietly(inStream);
		}
		return res;
	}
	
}
