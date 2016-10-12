package org.cmdb4j.core.ext.threaddumps.analyzer;

import org.cmdb4j.core.ext.threaddumps.model.MethodThreadLineInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpList;

/**
 * 
 */
public final class ThreadDumpUtils {

	/** private to force all static */
	private ThreadDumpUtils() {
	}

	// -------------------------------------------------------------------------

	public static void simplifyThreadDumps(ThreadDumpList threadDumpList) {
		// remove System threads: "Finalizer", "Reference Handler", ...
	    threadDumpList.visit(new SystemThreadRemover());
	
	    // remove iddle threads
	    InactiveThreadRemover inactiveThreadRemover = new InactiveThreadRemover();
	    threadDumpList.visit(inactiveThreadRemover);
	    
	    // remove intermediate stack entry for EJB skeletons
	    threadDumpList.visit(new EjbSkelMethodLineRemover());
	    
	    MethodLineRemover methodLineRemover = new MethodLineRemover(MethodCategory.DEFAULT_RULES);
	    threadDumpList.visit(methodLineRemover);
	}
	
	public static void simplifyThreadDump(ThreadDumpInfo threadDump) {
		// remove System threads: "Finalizer", "Reference Handler", ...
		threadDump.visit(new SystemThreadRemover());
	
	    // remove iddle threads
	    InactiveThreadRemover inactiveThreadRemover = new InactiveThreadRemover();
	    threadDump.visit(inactiveThreadRemover);
	    
	    // remove intermediate stack entry for EJB skeletons
	    threadDump.visit(new EjbSkelMethodLineRemover());
	    
	    MethodLineRemover methodLineRemover = new MethodLineRemover(MethodCategory.DEFAULT_RULES);
	    threadDump.visit(methodLineRemover);
	}
	
    
	public static boolean isSystemThread(String name) {
		return name.equals("Signal Dispatcher")
			|| name.equals("JDWP Event Helper Thread")
			|| name.startsWith("JDWP Transport Listener: ")
			|| name.equals("Surrogate Locker Thread (CMS)")
			|| name.equals("GC Daemon")
			| name.equals("Finalizer")
			|| name.equals("Reference Handler")
			|| name.equals("VM Thread")
			|| name.equals("VM Periodic Task Thread")
			|| name.equals("Suspend Checker Thread")
			|| name.startsWith("RMI ConnectionExpiration")
			|| name.startsWith("RM RenewClean-")
			|| name.equals("process reaper")
			
			|| name.equals("weblogic.security.SpinnerRandomSource")
			|| name.equals("weblogic.time.TimeEventGenerator")
			|| name.equals("SSLListenThread.Default")
			|| name.equals("ListenThread.Default")
			|| name.equals("weblogic.health.CoreHelthMonitor") 
			|| name.equals("DoSManager")
			|| name.equals("VDE Replication Thread")
			|| name.equals("VDE Transaction Processor Thread") 
			;
	}

	public static boolean isJavaLangObjectWait(MethodThreadLineInfo p) {
		return (p != null && p.getMethodFullName() != null 
				&& p.getMethodFullName().equals("java.lang.Object.wait") 
				// && p.getInfos() != null && p.getInfos().size() >= 1
				);
	}

	public static boolean isSocketRead0(MethodThreadLineInfo p) {
		return p.getMethodFullName().equals("java.nt.SocketInputStream.socketRead0");
	}
	
	
}
