package org.cmdb4j.core.ext.process.server;

import java.io.Serializable;

import org.cmdb4j.core.ext.process.ResultDTO;

public class ServerThreadDumpDTO extends ResultDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;
	
	private String id;
    private String serverId;

    private String threadDump;
    
    // ------------------------------------------------------------------------

    public ServerThreadDumpDTO() {
    }

    // ------------------------------------------------------------------------
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getThreadDump() {
        return threadDump;
    }

    public void setThreadDump(String threadDump) {
        this.threadDump = threadDump;
    }

    public void setCopyOf(ServerThreadDumpDTO src) {
        super.setCopyOf(src);
        this.id = src.id;
        this.serverId = src.serverId;
        this.threadDump = src.threadDump;
    }
    
    // ------------------------------------------------------------------------
    
}
