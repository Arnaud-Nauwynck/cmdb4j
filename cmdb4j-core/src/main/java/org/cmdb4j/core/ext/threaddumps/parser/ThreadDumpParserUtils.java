package org.cmdb4j.core.ext.threaddumps.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpList;
import org.cmdb4j.core.ext.threaddumps.parser.sourcereader.BufferedSourceReader;
import org.cmdb4j.core.ext.threaddumps.parser.sourcereader.SourceReader;

public final class ThreadDumpParserUtils {

	/** private to force all static */
	private ThreadDumpParserUtils() {}

	public static ThreadDumpInfo parseThreadDump(String text) {
		ThreadDumpList threadDumpList = new ThreadDumpList();
		SourceReader sourceReader = new BufferedSourceReader(new BufferedReader(new StringReader(text)));
	    ThreadDumpListParser parser = new ThreadDumpListParser(threadDumpList, sourceReader); 
	    try {
	        parser.parse();
	    } catch(IOException ex) {
	    	throw new RuntimeException("should not occur: in-memory io", ex);
	    } finally {
	    	if (sourceReader != null) sourceReader.close();
	    }
	    List<ThreadDumpInfo> tmpres = threadDumpList.getThreadDumps();
	    return (!tmpres.isEmpty())? tmpres.get(0) : null;
	}
	
}
