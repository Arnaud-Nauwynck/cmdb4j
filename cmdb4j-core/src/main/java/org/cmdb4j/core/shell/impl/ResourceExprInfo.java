package org.cmdb4j.core.shell.impl;

/**
 * 
 */
public class ResourceExprInfo {

    private final String exprText;

    // ------------------------------------------------------------------------
    
    public ResourceExprInfo(String exprText) {
        this.exprText = exprText;
    }

    // ------------------------------------------------------------------------
    
    public String getExprText() {
        return exprText;
    }

    @Override
    public String toString() {
        return "ResourceExpr[" + exprText + "]";
    }
    
}
