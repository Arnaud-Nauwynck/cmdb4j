package org.cmdb4j.core.env.dto;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO for EnvTemplateDescr
 *
 */
public class EnvTemplateDescrDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private String name;

    private String displayName;

    private String comment;

    private Map<String,EnvTemplateParamDescrDTO> paramDescriptions = new LinkedHashMap<>();

    private Map<String,JsonNode> extraProperties = new LinkedHashMap<>();

    private JsonNode rawNode;

    // ------------------------------------------------------------------------
    
    public EnvTemplateDescrDTO() {
    }
    
    public EnvTemplateDescrDTO(String name, String displayName, String comment,
            Map<String, EnvTemplateParamDescrDTO> paramDescriptions, 
            Map<String, JsonNode> extraProperties, JsonNode rawNode) {
        this();
        this.name = name;
        this.displayName = displayName;
        this.comment = comment;
        this.paramDescriptions = paramDescriptions;
        this.extraProperties = extraProperties;
        this.rawNode = rawNode;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, EnvTemplateParamDescrDTO> getParamDescriptions() {
        return paramDescriptions;
    }
    
    public void setParamDescriptions(Map<String, EnvTemplateParamDescrDTO> paramDescriptions) {
        this.paramDescriptions = paramDescriptions;
    }

    public void addParamDescription(EnvTemplateParamDescrDTO p) {
        if (this.paramDescriptions == null) {
            this.paramDescriptions = new LinkedHashMap<>();
        }
        this.paramDescriptions.put(p.getName(), p);
    }

    public Map<String, JsonNode> getExtraProperties() {
        return extraProperties;
    }
    
    public void setExtraProperties(Map<String, JsonNode> extraProperties) {
        this.extraProperties = extraProperties;
    }
    
    public void putExtraProperty(String key, JsonNode value) {
        if (this.extraProperties == null) {
            this.extraProperties = new LinkedHashMap<>();
        }
        this.extraProperties.put(key, value);
    }
    
    public JsonNode getRawNode() {
        return rawNode;
    }
    
    public void setRawNode(JsonNode rawNode) {
        this.rawNode = rawNode;
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "EnvTemplateDescrDTO[" + name + "]";
    }
    
}
