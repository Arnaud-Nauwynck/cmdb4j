package org.cmdb4j.core.util;

public class CmdbObjectNotFoundException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    public CmdbObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CmdbObjectNotFoundException(String message) {
        super(message);
    }

    
}
