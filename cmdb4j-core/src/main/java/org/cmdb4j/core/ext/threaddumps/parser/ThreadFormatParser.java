package org.cmdb4j.core.ext.threaddumps.parser;

import org.cmdb4j.core.ext.threaddumps.model.ThreadInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadLineInfo;

/**
 * interface for parsing a line element of a ThreadDump (either thread, or item line)
 */
public interface ThreadFormatParser {

    public abstract ThreadInfo parseThread(String line);

    public abstract ThreadLineInfo parseThreadLine(String line);
    
}
