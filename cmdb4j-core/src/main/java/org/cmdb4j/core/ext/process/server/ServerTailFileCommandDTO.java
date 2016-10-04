package org.cmdb4j.core.ext.process.server;

public class ServerTailFileCommandDTO extends AbstractServerCommandDTO {

    /** */
	private static final long serialVersionUID = 1L;

    protected String fileName;
    protected int tailLineCount;
    
    // ------------------------------------------------------------------------

    public ServerTailFileCommandDTO() {
    }

    // ------------------------------------------------------------------------

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTailLineCount() {
        return tailLineCount;
    }

    public void setTailLineCount(int tailLineCount) {
        this.tailLineCount = tailLineCount;
    }


    
}