package org.cmdb4j.core.env;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.an.fxtree.model.FxNode;

/**
 * Description for a template env
 * 
 * <p> 
 * see also corresponding EnvTemplateDescrDTO
 */
public class EnvTemplateDescr {

    private String name;

    private Map<String,EnvTemplateParamDescr> paramDescriptions = new LinkedHashMap<>();

    private Map<String,FxNode> extraProperties = new LinkedHashMap<>();

    private FxNode rawNode;

    // ------------------------------------------------------------------------
    
    public EnvTemplateDescr(String name, Map<String, EnvTemplateParamDescr> paramDescriptions, Map<String, FxNode> extraProperties, FxNode rawNode) {
        this.name = name;
        this.paramDescriptions = paramDescriptions;
        this.extraProperties = extraProperties;
        this.rawNode = rawNode;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public Map<String, EnvTemplateParamDescr> getParamDescriptions() {
        return paramDescriptions;
    }
    
    public Map<String, FxNode> getExtraProperties() {
        return extraProperties;
    }
    
    public FxNode getRawNode() {
        return rawNode;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "EnvTemplateDescr [" + name + "]";
    }
    
}
