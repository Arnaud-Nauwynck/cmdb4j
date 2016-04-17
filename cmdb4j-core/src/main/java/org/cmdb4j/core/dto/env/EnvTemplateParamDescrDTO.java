package org.cmdb4j.core.dto.env;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Description of an env template parameter
 */
public class EnvTemplateParamDescrDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private String name;

    private String type;

    private String displayName;
    
    private String comment;
    
    private JsonNode defaultValue;
    
    private Map<String,JsonNode> extraProperties = new LinkedHashMap<>();
    
    // ------------------------------------------------------------------------

    public EnvTemplateParamDescrDTO() {
    }
    
    public EnvTemplateParamDescrDTO(String name, String type, String displayName, String comment, JsonNode defaultValue,
            Map<String, JsonNode> extraProperties) {
        this();
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.extraProperties = extraProperties;
    }


    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public JsonNode getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(JsonNode defaultValue) {
        this.defaultValue = defaultValue;
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

    public void removeExtraProperty(String key) {
        if (this.extraProperties == null) {
            return;
        }
        this.extraProperties.remove(key);
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "EnvTemplateParamDescrDTO [" + name + "]";
    }
    
}
