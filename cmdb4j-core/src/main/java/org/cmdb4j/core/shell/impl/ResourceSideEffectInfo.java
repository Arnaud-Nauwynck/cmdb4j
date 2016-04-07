package org.cmdb4j.core.shell.impl;

public class ResourceSideEffectInfo {

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
