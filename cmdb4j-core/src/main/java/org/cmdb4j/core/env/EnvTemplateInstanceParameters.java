package org.cmdb4j.core.env;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import fr.an.fxtree.model.FxNode;

/**
 * parameters for "cloud" environments instanciated from "template"
 */
public class EnvTemplateInstanceParameters {

    private final String templateSourceEnvName;
    
    /**
     * parameter substitution for instanciating from the template
     * 
     */
    private final Map<String, FxNode> templateParameters;

    /**
     * optionnal parameters for comment (creationDate, user, retention, description, deployedVersionSet, ... )  
     */
    private final Map<String, FxNode> metaParameters;
    
    private final FxNode templateRootNode;

    // ------------------------------------------------------------------------
    
    public EnvTemplateInstanceParameters(String templateSourceEnvName, Map<String, FxNode> templateParameters, Map<String, FxNode> metaParameters, FxNode templateRootNode) {
        this.templateSourceEnvName = templateSourceEnvName;
        this.templateParameters = ImmutableMap.copyOf(templateParameters);
        this.metaParameters = ImmutableMap.copyOf(metaParameters);
        this.templateRootNode = templateRootNode;
    }

    // ------------------------------------------------------------------------
    
    public String getTemplateSourceEnvName() {
        return templateSourceEnvName;
    }

    public Map<String, FxNode> getTemplateParameters() {
        return templateParameters;
    }

    public Map<String, FxNode> getMetaParameters() {
        return metaParameters;
    }

    public FxNode getTemplateRootNode() {
        return templateRootNode;
    }

    // ------------------------------------------------------------------------
    
    /**
     * Builder design pattern for immutable class
     */
    public static class Builder {
        private String templateSourceEnvName;
        private final Map<String, FxNode> templateParameters = new LinkedHashMap<>();
        private final Map<String, FxNode> metaParameters = new LinkedHashMap<>();
        private FxNode templateRootNode;
        
        public EnvTemplateInstanceParameters build() {
            return new EnvTemplateInstanceParameters(templateSourceEnvName, templateParameters, metaParameters, templateRootNode);
        }
        
        public Builder templateSourceEnvName(String p) {
            templateSourceEnvName = p;
            return this;
        }
        public Builder putAllTemplateParameters(Map<String, FxNode> p) {
            templateParameters.putAll(p);
            return this;
        }
        public Builder putAllMetaParameters(Map<String, FxNode> p) {
            metaParameters.putAll(p);
            return this;
        }
        public Builder templateRootNode(FxNode p) {
            templateRootNode = p;
            return this;
        }
    }
}
