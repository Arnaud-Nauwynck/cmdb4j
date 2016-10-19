package org.cmdb4j.overthere.docker;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.cmdb4j.overthere.utils.OverthereProcessUtils;
import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.local.LocalConnection;

public class DockerContainerOverthereConnectionTest {

	@Test
	public void testExecute() throws Exception {
		OverthereConnection localEngineConn = LocalConnection.getLocalConnection();
		CmdLine dockerBashSleepCmd = OverthereProcessUtils.buildCmdLine("docker", "run", "-d", "ubuntu", "bash", "-c", "sleep 100");
		OverthereProcess dockerBashProcess = localEngineConn.startProcess(dockerBashSleepCmd);
		// read first line for container id, without closing stdout
		BufferedReader reader = new BufferedReader (new InputStreamReader(dockerBashProcess.getStdout())); 
		String containerId = reader.readLine();
		try {
			DockerContainerOverthereConnection sut = new DockerContainerOverthereConnection(localEngineConn, containerId);
			
			String executeRes = OverthereProcessUtils.execSimple(sut, "echo", "Hello Docker");
			String dockerStdoutRes = reader.readLine();
			System.out.println("test docker execute ... echo \"Hello Docker\" =>\n" + executeRes 
					+ ((dockerStdoutRes != null)? "dockerStdoutRes\n:" + dockerStdoutRes : ""));
			
		} finally {
			OverthereProcessUtils.execSimple(localEngineConn, "docker", "stop", containerId);
			OverthereProcessUtils.execSimple(localEngineConn, "docker", "rm", containerId);
		}
	}
	
}
