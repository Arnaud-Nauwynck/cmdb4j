package org.cmdb4j.core.ext.threaddumps.analyzer;

import java.util.Iterator;

import org.cmdb4j.core.ext.threaddumps.model.DefaultThreadItemInfoVisitor;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadInfo;



/**
 * helper to remove MethodThreadLineInfo, using methodCaterories rules
 */
public class SystemThreadRemover extends DefaultThreadItemInfoVisitor { 

	// ------------------------------------------------------------------------

	public SystemThreadRemover() {
	}

	// -------------------------------------------------------------------------
	
	@Override
	public void caseThreadDumpInfo(ThreadDumpInfo p) {
		// no call super!
		for (Iterator<ThreadInfo> iter = p.getThreads().iterator(); iter.hasNext(); ) {
			ThreadInfo elt = iter.next();
			if (ThreadDumpUtils.isSystemThread(elt.getName())) {
				iter.remove();
			}
		}
	}
		
}
