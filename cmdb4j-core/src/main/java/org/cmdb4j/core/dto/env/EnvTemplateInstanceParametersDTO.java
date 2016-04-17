package org.cmdb4j.core.dto.env;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO for EnvTemplateInstanceParameters
 */
public class EnvTemplateInstanceParametersDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    
    private String sourceTemplateName;
    
    /**
     * parameter values for instanciating the template
     */
    private Map<String, JsonNode> parameters = new LinkedHashMap<>();

    /**
     * optionnal extra properties (for creationDate, user, retention, description, deployedVersionSet, ... )  
     */
    private Map<String, JsonNode> extraProperties = new LinkedHashMap<>();
    
    // ------------------------------------------------------------------------
    
    public EnvTemplateInstanceParametersDTO() {
    }

    public EnvTemplateInstanceParametersDTO(String sourceTemplateName, Map<String, JsonNode> parameters, Map<String, JsonNode> metaParameters) {
        this();
        this.sourceTemplateName = sourceTemplateName;
        this.parameters = parameters;
        this.extraProperties = metaParameters;
    }


    // ------------------------------------------------------------------------
    
    public String getSourceTemplateName() {
        return sourceTemplateName;
    }

    public void setSourceTemplateName(String p) {
        this.sourceTemplateName = p;
    }

    public Map<String, JsonNode> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, JsonNode> p) {
        this.parameters = p;
    }

    public void putParameter(String name, JsonNode value) {
        if (parameters == null) {
            parameters = new LinkedHashMap<>();
        }
        this.parameters.put(name, value);
    }
    
    public Map<String, JsonNode> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, JsonNode> p) {
        this.extraProperties = p;
    }

    public void putExtraProperty(String name, JsonNode value) {
        if (extraProperties == null) {
            extraProperties = new LinkedHashMap<>();
        }
        this.extraProperties.put(name, value);
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "EnvTemplateInstanceParametersDTO [sourceTemplateName=" + sourceTemplateName 
                    + ", parameters:" + parameters
                    + "]";
    }
    
}
