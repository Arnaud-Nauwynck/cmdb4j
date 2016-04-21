package org.cmdb4j.core.dto.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for ResourceCommandInfo
 */
public class ResourceCommandInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String targetResourceType;
    
    /**
     * text for the command 
     */
    private String name;

    /**
     * text aliases for the command 
     */
    private List<String> aliases = new ArrayList<>();
    
    /**
     * description of command parameters
     */
    private List<ResourceCommandParamInfoDTO> params = new ArrayList<>();
    
    /**
     *
     */
    private String category;

    /**
     * help message for this command
     */
    private String help;
    
    // ------------------------------------------------------------------------

    public ResourceCommandInfoDTO() {
    }

    // ------------------------------------------------------------------------

    public String getTargetResourceType() {
        return targetResourceType;
    }

    public void setTargetResourceType(String targetResourceType) {
        this.targetResourceType = targetResourceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    public List<ResourceCommandParamInfoDTO> getParams() {
        return params;
    }

    public void setParams(List<ResourceCommandParamInfoDTO> params) {
        this.params = params;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "ResourceCommandInfoDTO [name=" + name + "]";
    }

}
