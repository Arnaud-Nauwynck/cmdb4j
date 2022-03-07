package org.cmdb4j.server.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseableIOUtils {

	
	/**
	 * replacement for commons-io IOUtils.closeQuietly() ... to avoid importing jar for 1 method...
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable) {
	    try {
	        if (closeable != null) {
	            closeable.close();
	        }
	    } catch (IOException ioe) {
	        // ignore
	    }
	}


}
