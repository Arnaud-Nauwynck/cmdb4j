package org.cmdb4j.core.dto.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Resource Command Parameter Description
 */
public class ResourceCommandParamInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private int index;
    
    /**
     * name of the parameter
     */
    private String name;

    /**
     * type of the parameter
     */
    private String type;
    
    /**
     * 
     */
    private List<String> aliases = new ArrayList<>();

    /**
     * description of this parameter
     */
    private String description;

    /**
     * Whether this parameter is required.
     */
    private boolean required;

    /**
     * 
     */
    private String defaultValue;

    // ------------------------------------------------------------------------

    public ResourceCommandParamInfoDTO() {
    }

    // ------------------------------------------------------------------------
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

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

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    // ------------------------------------------------------------------------
    
    
    
}
