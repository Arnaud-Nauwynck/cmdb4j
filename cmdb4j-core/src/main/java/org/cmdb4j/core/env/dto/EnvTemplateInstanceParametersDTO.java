package org.cmdb4j.core.env.dto;

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
     * optionnal meta-parameters for comment (creationDate, user, retention, description, deployedVersionSet, ... )  
     */
    private Map<String, JsonNode> metaParameters = new LinkedHashMap<>();
    
    // ------------------------------------------------------------------------
    
    public EnvTemplateInstanceParametersDTO() {
    }

    public EnvTemplateInstanceParametersDTO(String sourceTemplateName, Map<String, JsonNode> parameters, Map<String, JsonNode> metaParameters) {
        this();
        this.sourceTemplateName = sourceTemplateName;
        this.parameters = parameters;
        this.metaParameters = metaParameters;
    }


    // ------------------------------------------------------------------------
    
    public String getSourceTemplateName() {
        return sourceTemplateName;
    }

    public void setTemplateSourceName(String p) {
        this.sourceTemplateName = p;
    }

    public Map<String, JsonNode> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, JsonNode> p) {
        this.parameters = p;
    }

    public Map<String, JsonNode> getMetaParameters() {
        return metaParameters;
    }

    public void setMetaParameters(Map<String, JsonNode> p) {
        this.metaParameters = p;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "EnvTemplateInstanceParametersDTO [sourceTemplateName=" + sourceTemplateName 
                    + ", templateParameters:" + parameters
                    + "]";
    }
    
}
