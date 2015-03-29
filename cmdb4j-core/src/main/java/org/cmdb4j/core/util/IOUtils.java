package org.cmdb4j.core.util;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class IOUtils {

	public static void closeQuietly(Closeable reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore, no rethrow
			}
		}
	}

	public static void checkDirExists(File dir) {
		if (! dir.exists()) {
			throw new RuntimeException("file not found: " + dir);
		}
		if (! dir.isDirectory()) {
			throw new RuntimeException("not a directory: " + dir);
		}
	}
	
	
	public static void copyFile(File fromFile, File toFile) {
		try {
			Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch(IOException ex) {
			throw new RuntimeException("Failed to copy file " + toFile, ex);
		}
	}

	
	public static Properties loadPropertiesFile(File file) {
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
