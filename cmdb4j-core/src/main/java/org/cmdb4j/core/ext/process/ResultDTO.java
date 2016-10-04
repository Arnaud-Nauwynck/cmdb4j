package org.cmdb4j.core.ext.process;

import java.io.Serializable;
import java.util.Date;

public class ResultDTO implements Serializable {

    /** */
	private static final long serialVersionUID = 1L;
	
	private Date execStartDate;
    private long execTimeMillis;
    private String execException;

    // ------------------------------------------------------------------------

    public ResultDTO() {
    }

    // ------------------------------------------------------------------------
    
    public void setExecEnd(Date end) {
        setExecTimeMillis(end.getTime() - execStartDate.getTime());
        this.execException = null;
    }

    public void setExecEndException(Date end, Exception ex) {
        setExecTimeMillis(end.getTime() - execStartDate.getTime());
        this.execException = (ex != null)? ex.getMessage() : null;
    }


    public boolean isExecExpired(Date date, long expiryMillis) {
        if (execStartDate == null) {
            return true;
        }
        long millis = date.getTime() - execStartDate.getTime();
        return millis > expiryMillis;
    }
    
    public void setCopyOf(ResultDTO src) {
        this.execStartDate = src.execStartDate;
        this.execTimeMillis = src.execTimeMillis;
        this.execException = src.execException;
    }
    
    // ------------------------------------------------------------------------
    
    public Date getExecStartDate() {
        return execStartDate;
    }

    public void setExecStartDate(Date p) {
        this.execStartDate = p;
    }
    
    public long getExecTimeMillis() {
        return execTimeMillis;
    }

    public void setExecTimeMillis(long execTimeMillis) {
        this.execTimeMillis = execTimeMillis;
    }

    public String getExecException() {
        return execException;
    }

    public void setExecException(String p) {
        this.execException = p;
    }

    // ------------------------------------------------------------------------

    
}
