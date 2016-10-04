package org.cmdb4j.core.ext.process;

import java.io.Serializable;

public class CommandResultDTO extends ResultDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;
	
	private String id;
    private String hostId;
    private String serverId;
    
    private String cmdLine;
    private int exitCode;
    private String stdout;
    private String stderr;
    
    // ------------------------------------------------------------------------

    public CommandResultDTO() {
    }

    // ------------------------------------------------------------------------
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getHostId() {
        return hostId;
    }
    
    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    public String getServerId() {
        return serverId;
    }
    
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getCmdLine() {
        return cmdLine;
    }

    public void setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    // ------------------------------------------------------------------------
    
	public String outputAsString() {
		String res = "";
		if (exitCode != 0) {
			res += "exitCode:" + exitCode; 
		}
		if (stdout != null && !stdout.isEmpty()) {
			res += "stdout:" + stdout + "\n";
		}
		if (stderr != null && !stderr.isEmpty()) {
			res += "stderr:" + stderr + "\n";
		}
		return res;
	}


    
}
