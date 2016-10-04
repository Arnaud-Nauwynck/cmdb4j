package org.cmdb4j.core.ext.process.server;

import java.io.Serializable;

public abstract class AbstractServerCommandDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;
	
	protected String serverId;
    
    // ------------------------------------------------------------------------

    public AbstractServerCommandDTO() {
    }

    // ------------------------------------------------------------------------

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }



    
}