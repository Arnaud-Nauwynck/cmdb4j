package org.cmdb4j.overthere.jvm;

import org.cmdb4j.core.ext.threaddumps.analyzer.ThreadDumpUtils;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.core.ext.threaddumps.parser.ThreadDumpParserUtils;
import org.cmdb4j.overthere.utils.OverthereProcessUtils;

import com.xebialabs.overthere.OverthereConnection;

public class PidLinuxRemoteJvm {

	protected OverthereConnection connection;
	protected int pid;
	
	private String cachedJreDir;
	
	// ------------------------------------------------------------------------
	
	public PidLinuxRemoteJvm(OverthereConnection connection, int pid) {
		super();
		this.connection = connection;
		this.pid = pid;
	}
	
	// ------------------------------------------------------------------------

	public String getJreDir() {
		String res = cachedJreDir;
		if (res == null) {
			res = cachedJreDir = doGetJreDir(); 
		}
		return res;
	}
	
	public String getJdkDir() {
		String res = getJreDir();
		if (res != null && res.endsWith("/jre")) {
			res = res.substring(0, res.length() - "/jre".length());
		}
		return res;
	}
	
	protected String doGetJreDir() {
		// read symbolic link information of "/proc/<<pid>>/exe"
		String procPidExe = "/proc/" + pid + "/exe";
		String exeFile = OverthereProcessUtils.execSimple(connection, "/bin/readlink", procPidExe);
		if (exeFile == null) {
			return null;
		}
		if (exeFile.endsWith("\n")) {
			exeFile = exeFile.substring(0, exeFile.length()-1);
		}
		if (exeFile.endsWith("/bin/java")) {
			return exeFile.substring(0, exeFile.length() - "/bin/java".length()); 
		}
		return null;
	}

	public ThreadDumpInfo jstack(boolean simplify) {
		String jdkDir = getJdkDir();
		if (jdkDir == null) {
			return null; // no jdk detected?
		}
		String threadDumpText = OverthereProcessUtils.execSimple(connection, jdkDir + "/bin/jstack", Integer.toString(pid));
		ThreadDumpInfo res = ThreadDumpParserUtils.parseThreadDump(threadDumpText);
		if (simplify) {
			ThreadDumpUtils.simplifyThreadDump(res);
		}
		return res;
	}
	
	
}
