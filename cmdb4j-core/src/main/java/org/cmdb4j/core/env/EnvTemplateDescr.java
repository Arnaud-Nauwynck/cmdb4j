package org.cmdb4j.core.env;

import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescr /*implements Serializable*/ {

    private String name;

    private FxNode descrNode;
    
    private FxNode rawNode;

    // ------------------------------------------------------------------------
    
    public EnvTemplateDescr(String name, FxNode descrNode, FxNode rawNode) {
        this.name = name;
        this.descrNode = descrNode;
        this.rawNode = rawNode;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }

    public FxNode getDescrNode() {
        return descrNode;
    }

    public FxNode getRawNode() {
        return rawNode;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "EnvTemplateDescr [" + name + ", " + descrNode + "]";
    }
    
}
