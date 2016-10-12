package org.cmdb4j.overthere.jvm;

import java.net.URL;

import org.cmdb4j.core.ext.threaddumps.analyzer.ThreadDumpUtils;
import org.cmdb4j.core.ext.threaddumps.model.ThreadDumpInfo;
import org.cmdb4j.overthere.utils.OverthereProcessUtils;
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
		
		ClassLoader cl = PidLinuxRemoteJvmTest.class.getClassLoader();
		URL logbackURL = cl.getResource("logback.xml");
		if (logbackURL != null) {
			System.out.println("found logback.xml: " + logbackURL);
		}
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
		int threadCountBefore = threadDump.getThreads().size();
		Assert.assertTrue(threadCountBefore > 20);
		ThreadDumpUtils.simplifyThreadDump(threadDump);
		int threadCountAfter = threadDump.getThreads().size();
		Assert.assertTrue(threadCountAfter < threadCountBefore);
		Assert.assertTrue(threadCountAfter >= 2); // usually 4 .. 
		// System.out.println(threadDump);
	}
	
	@Test
	public void testJstack_eclipse() {
		OverthereConnection localConn = LocalConnection.getLocalConnection();
		String pidText = OverthereProcessUtils.execSimple(localConn, "/bin/bash", "-c", "jps | grep eclipse | cut -f1 -d' '"); 
		if (pidText == null || pidText.isEmpty()) {
			return;
		}
		pidText = pidText.trim();
		PidLinuxRemoteJvm jvm = new PidLinuxRemoteJvm(localConn, Integer.parseInt(pidText));
		
		ThreadDumpInfo threadDump = jvm.jstack(false);
		Assert.assertNotNull(threadDump);
		int threadCountBefore = threadDump.getThreads().size();
		Assert.assertTrue(threadCountBefore > 50);
		ThreadDumpUtils.simplifyThreadDump(threadDump);
		int threadCountAfter = threadDump.getThreads().size();
		Assert.assertTrue(threadCountAfter < threadCountBefore);
		Assert.assertTrue(threadCountAfter >= 42); // the intergalactic magic number ...
		System.out.println("Threads count: " + threadCountBefore + " => simplify: " + threadCountAfter);
		// System.out.println(threadDump);
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
