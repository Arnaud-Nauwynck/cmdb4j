package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;

/**
 * description of a resource objet expression, equivalent to "@ResourceExpr" annotation information on method
 */
public class ResourceExprInfo implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;


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
