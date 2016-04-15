package org.cmdb4j.core.env;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import fr.an.fxtree.model.FxNode;

/**
 * parameters for "cloud" (="ephemeral", "provisionned") environments instanciated from "template"
 */
public class EnvTemplateInstanceParameters {

    private final String sourceTemplateName;
    
    /**
     * parameter substitution for instanciating from the template
     * 
     */
    private final Map<String, FxNode> parameters;

    /**
     * optionnal parameters for comment (creationDate, user, retention, description, deployedVersionSet, ... )  
     */
    private final Map<String, FxNode> metaParameters;
    
    // ------------------------------------------------------------------------
    
    public EnvTemplateInstanceParameters(String sourceTemplateName, Map<String, FxNode> templateParameters, Map<String, FxNode> metaParameters) {
        this.sourceTemplateName = sourceTemplateName;
        this.parameters = ImmutableMap.copyOf(templateParameters);
        this.metaParameters = ImmutableMap.copyOf(metaParameters);
    }

    // ------------------------------------------------------------------------
    
    public String getSourceTemplateName() {
        return sourceTemplateName;
    }

    public Map<String, FxNode> getParameters() {
        return parameters;
    }

    public Map<String, FxNode> getMetaParameters() {
        return metaParameters;
    }

    // ------------------------------------------------------------------------
    
    /**
     * Builder design pattern for immutable class
     */
    public static class Builder {
        private String sourceTemplateName;
        private final Map<String, FxNode> templateParameters = new LinkedHashMap<>();
        private final Map<String, FxNode> metaParameters = new LinkedHashMap<>();
        
        public EnvTemplateInstanceParameters build() {
            return new EnvTemplateInstanceParameters(sourceTemplateName, templateParameters, metaParameters);
        }
        
        public Builder sourceTemplateName(String p) {
            sourceTemplateName = p;
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
    }
    
}
