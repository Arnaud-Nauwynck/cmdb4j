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
     * optionnal extra properties (for comment, creationDate, user, retention, description, deployedVersionSet, ... )  
     */
    private final Map<String, FxNode> extraProperties;
    
    // ------------------------------------------------------------------------
    
    public EnvTemplateInstanceParameters(String sourceTemplateName, Map<String, FxNode> templateParameters, Map<String, FxNode> extraProperties) {
        this.sourceTemplateName = sourceTemplateName;
        this.parameters = ImmutableMap.copyOf(templateParameters);
        this.extraProperties = ImmutableMap.copyOf(extraProperties);
    }

    // ------------------------------------------------------------------------
    
    public String getSourceTemplateName() {
        return sourceTemplateName;
    }

    public Map<String, FxNode> getParameters() {
        return parameters;
    }

    public Map<String, FxNode> getExtraProperties() {
        return extraProperties;
    }

    // ------------------------------------------------------------------------
    
    /**
     * Builder design pattern for immutable class
     */
    public static class Builder {
        private String sourceTemplateName;
        private final Map<String, FxNode> parameters = new LinkedHashMap<>();
        private final Map<String, FxNode> extraProperties = new LinkedHashMap<>();
        
        public EnvTemplateInstanceParameters build() {
            return new EnvTemplateInstanceParameters(sourceTemplateName, parameters, extraProperties);
        }
        
        public Builder sourceTemplateName(String p) {
            sourceTemplateName = p;
            return this;
        }
        public Builder putParameter(String name, FxNode value) {
            parameters.put(name, value);
            return this;
        }
        public Builder putAllParameters(Map<String, FxNode> p) {
            parameters.putAll(p);
            return this;
        }
        public Builder putExtraProperty(Map<String, FxNode> p) {
            extraProperties.putAll(p);
            return this;
        }
        public Builder putAllExtraPropreties(Map<String, FxNode> p) {
            extraProperties.putAll(p);
            return this;
        }
    }
    
}
