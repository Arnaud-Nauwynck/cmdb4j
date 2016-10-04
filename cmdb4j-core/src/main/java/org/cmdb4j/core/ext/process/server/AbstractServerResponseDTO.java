package org.cmdb4j.core.ext.process.server;

import java.io.Serializable;

import org.cmdb4j.core.ext.process.ResultDTO;

public class AbstractServerResponseDTO extends ResultDTO implements Serializable {
    
    /** */
	private static final long serialVersionUID = 1L;

    protected String serverId;

    // ------------------------------------------------------------------------

    public AbstractServerResponseDTO() {
    }

    // ------------------------------------------------------------------------

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
    
}