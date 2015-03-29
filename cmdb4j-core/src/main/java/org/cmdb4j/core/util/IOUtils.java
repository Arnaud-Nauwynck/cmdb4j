package org.cmdb4j.core.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

}
