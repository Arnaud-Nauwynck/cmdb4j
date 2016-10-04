package org.cmdb4j.core.ext.process.server;

import java.io.Serializable;

import org.cmdb4j.core.ext.process.ResultDTO;

public class ServerStatusDTO extends ResultDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;

	private String id;
    
    private String status;
    
    private int pid;
    
    // ------------------------------------------------------------------------

    public ServerStatusDTO() {
    }

    // ------------------------------------------------------------------------
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setCopyOf(ServerStatusDTO src) {
        super.setCopyOf(src);
        this.id = src.id;
        this.pid = src.pid;
        this.status = src.status;
    }

    
    // ------------------------------------------------------------------------
    
}
