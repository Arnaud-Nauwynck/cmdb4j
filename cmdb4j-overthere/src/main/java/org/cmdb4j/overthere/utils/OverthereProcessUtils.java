package org.cmdb4j.overthere.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcess;

public class OverthereProcessUtils {

	public static ExecutorService stdExecutorService = Executors.newFixedThreadPool(4);
	
	public static String execSimple(OverthereConnection conn, String cmd, String... args) {
		ProcessOutErrResult res = execGetOutErr(conn, cmd, args);
		if (res.getExitCode() != 0) {
			throw new RuntimeException("process execution failed: exitCode(" + res.getExitCode() + ") " + cmd + " " + args);
		}
		return new String(res.getStdout()); // default encoding
	}

	public static ProcessOutErrResult execGetOutErr(OverthereConnection conn, String cmd, String... args) {
		CmdLine cmdLine = buildCmdLine(cmd, args);

		// ??? does not wait until stdout flush ...  int exitCode = conn.execute(stdoutHandler, stderrHandler, cmdLine);
		OverthereProcess process = conn.startProcess(cmdLine);
		
		ByteArrayOutputStreamConsumer stdoutConsumer = new ByteArrayOutputStreamConsumer(process.getStdout());
		ByteArrayOutputStreamConsumer stderrConsumer = new ByteArrayOutputStreamConsumer(process.getStderr());

		Future<?> futureStdout = stdoutConsumer.submitConsumeStream(stdExecutorService);
		Future<?> futureStderr = stderrConsumer.submitConsumeStream(stdExecutorService);
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			// exitCode = -1;
			throw new RuntimeException("interrupted");
		} finally {
			// flush consume output (timeout after 10 secs)
			stdoutConsumer.waitFinishConsumeStream(futureStdout);
			stderrConsumer.waitFinishConsumeStream(futureStderr);
		}

		int exitCode = process.exitValue();
		return new ProcessOutErrResult(exitCode, stdoutConsumer.getBuffer(), stderrConsumer.getBuffer());
	}

	public static CmdLine buildCmdLine(String cmd, String... args) {
		CmdLine cmdLine = new CmdLine();
		cmdLine.addArgument(cmd);
		if (args != null && args.length != 0) {
			for (String arg : args) {
				cmdLine.addArgument(arg);
			}
		}
		return cmdLine;
	}
	
}
