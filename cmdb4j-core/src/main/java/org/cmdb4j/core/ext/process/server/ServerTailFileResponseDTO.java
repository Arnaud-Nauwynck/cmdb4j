package org.cmdb4j.core.ext.process.server;

import java.io.Serializable;

public class ServerTailFileResponseDTO extends AbstractServerResponseDTO implements Serializable {
    
    /** */
	private static final long serialVersionUID = 1L;

    protected String fileName;

    protected String content;
    
    // ------------------------------------------------------------------------

    public ServerTailFileResponseDTO() {
    }

    // ------------------------------------------------------------------------

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}