package org.cmdb4j.core.env;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdb4j.core.dto.env.EnvTemplateParamDescrDTO;

import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.model.FxNode;

/**
 * Description of an env template parameter
 * 
 * see corresponding EnvTemplateParamDescrDTO
 */
public class EnvTemplateParamDescr {

    private String name;

    private String type;

    private String displayName;
    
    private String comment;
    
    private FxNode defaultValue;
    
    private Map<String,FxNode> extraProperties = new LinkedHashMap<>();
    
    // ------------------------------------------------------------------------

    public EnvTemplateParamDescr() {
    }
    
    public EnvTemplateParamDescr(String name, String type, String displayName, String comment, FxNode defaultValue,
            Map<String, FxNode> extraProperties) {
        super();
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.extraProperties = extraProperties;
    }


    // ------------------------------------------------------------------------

    public static EnvTemplateParamDescrDTO toDTO(EnvTemplateParamDescr src) {
        return new EnvTemplateParamDescrDTO(src.getName(), src.getType(), src.getDisplayName(), src.getComment(),
            Fx2JacksonUtils.fxTreeToJsonNode(src.getDefaultValue()), 
            Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties()));
    }
    
    public static EnvTemplateParamDescr fromDTO(EnvTemplateParamDescrDTO src) {
        return new EnvTemplateParamDescr(src.getName(), src.getType(), src.getDisplayName(), src.getComment(),
            Fx2JacksonUtils.jsonNodeToFxTree(src.getDefaultValue()), 
            Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties()));
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

    public FxNode getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(FxNode defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public Map<String, FxNode> getExtraProperties() {
        return extraProperties;
    }
    
    public void setExtraProperties(Map<String, FxNode> extraProperties) {
        this.extraProperties = extraProperties;
    }

    public void putExtraProperty(String key, FxNode value) {
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
        return "EnvTemplateParamDescr[" + name + "]";
    }
    
}
