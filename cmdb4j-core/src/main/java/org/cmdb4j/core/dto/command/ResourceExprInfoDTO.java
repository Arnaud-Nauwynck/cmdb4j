package org.cmdb4j.core.dto.command;

import java.io.Serializable;

/**
 * description of a resource objet expression, equivalent to "@ResourceExpr" annotation information on method
 */
public class ResourceExprInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private String exprText;

    // ------------------------------------------------------------------------
    
    public ResourceExprInfoDTO() {
    }
    
    public ResourceExprInfoDTO(String exprText) {
        this.exprText = exprText;
    }

    // ------------------------------------------------------------------------
    
    public String getExprText() {
        return exprText;
    }
    
    public void setExprText(String exprText) {
        this.exprText = exprText;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "ResourceExprDTO[" + exprText + "]";
    }
    
}
