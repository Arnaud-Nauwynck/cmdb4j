package org.cmdb4j.core.dto.command;

import java.io.Serializable;

/**
 * description of a resource objet side-effect description, equivalent to "@ResourceSideEffect" annotation information on method
 */
public class ResourceSideEffectInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private String sideEffectText;

    // ------------------------------------------------------------------------

    public ResourceSideEffectInfoDTO() {
    }
    
    public ResourceSideEffectInfoDTO(String p) {
        this.sideEffectText = p;
    }

    // ------------------------------------------------------------------------
    
    public String getSideEffectText() {
        return sideEffectText;
    }
    
    public void setSideEffectText(String sideEffectText) {
        this.sideEffectText = sideEffectText;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "ResourceSideEffect[" + sideEffectText + "]";
    }
    
}
