package org.cmdb4j.overthere.utils;

/**
 * simple result for a Process execution
 */
public class ProcessOutErrResult {
	
	private int exitCode;
	private byte[] stdout;
	private byte[] stderr;
	
	public ProcessOutErrResult(int exitCode, byte[] stdout, byte[] stderr) {
		this.exitCode = exitCode;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public int getExitCode() {
		return exitCode;
	}

	public byte[] getStdout() {
		return stdout;
	}

	public byte[] getStderr() {
		return stderr;
	}
	
}
