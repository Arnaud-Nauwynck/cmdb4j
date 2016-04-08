package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;

/**
 * description of a resource objet side-effect description, equivalent to "@ResourceSideEffect" annotation information on method
 */
public class ResourceSideEffectInfo implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;


    private final String sideEffectText;

    // ------------------------------------------------------------------------
    
    public ResourceSideEffectInfo(String p) {
        this.sideEffectText = p;
    }

    // ------------------------------------------------------------------------
    
    public String getSideEffectText() {
        return sideEffectText;
    }

    @Override
    public String toString() {
        return "ResourceSideEffect[" + sideEffectText + "]";
    }
    
}
