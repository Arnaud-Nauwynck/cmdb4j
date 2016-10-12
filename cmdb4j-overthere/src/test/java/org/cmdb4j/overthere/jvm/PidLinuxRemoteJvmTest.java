package org.cmdb4j.overthere.jvm;

import org.cmdb4j.core.ext.threaddumps.analyzer.ThreadDumpUtils;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.local.LocalConnection;

public class PidLinuxRemoteJvmTest {

	private static PidLinuxRemoteJvm selfJvm;
	
	@BeforeClass
	public static void setup() {
		OverthereConnection localConn = LocalConnection.getLocalConnection();
		int selfPid = selfPid();
		selfJvm = new PidLinuxRemoteJvm(localConn, selfPid);
	}
	
	@Test
	public void testGetJreDir() {
		String jre = selfJvm.getJreDir();
		Assert.assertNotNull(jre);
		Assert.assertTrue(jre.contains("jdk"));
	}
	
	@Test
	public void testJstack() {
		ThreadDumpInfo threadDump = selfJvm.jstack(false);
		Assert.assertNotNull(threadDump);
		Assert.assertTrue(threadDump.getThreads().size() > 20);
		ThreadDumpUtils.simplifyThreadDump(threadDump);
		Assert.assertTrue(threadDump.getThreads().size() > 1);
	}
	
	@SuppressWarnings("restriction")
	public static int selfPid() {
		// waiting for java9 ..
		// long pid = ProcessHandle.current().getPid();
		try {
			java.lang.management.RuntimeMXBean runtime = 
			    java.lang.management.ManagementFactory.getRuntimeMXBean();
			java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			sun.management.VMManagement mgmt =  
			    (sun.management.VMManagement) jvm.get(runtime);
			java.lang.reflect.Method pid_method =  
			    mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);
	
			int pid = (Integer) pid_method.invoke(mgmt);
			return pid;
		} catch(Exception ex) {
			throw new RuntimeException("Failed to get pid");
		}
	}
	
}
