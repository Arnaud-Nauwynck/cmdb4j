package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * description of a resource objet command Param, equivalent to "@Param" annotation information on method parameter
 */
public class ParamInfo implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * name of the parameter... when not set (as of jdk8... the java code method name is used)
     */
    private final String name;

    /**
     * type of the parameter
     */
    private Class<?> type;
    
    /**
     * 
     */
    private final String[] aliases;

    /**
     * description of this parameter
     */
    private final String description;

    /**
     * Whether this parameter is required.
     */
    private final boolean required;

    /**
     * 
     */
    private final String defaultValue;

    // ------------------------------------------------------------------------
    
    public ParamInfo(Builder b) {
        super();
        this.name = b.name;
        this.type = b.type;
        this.aliases = b.aliases.toArray(new String[b.aliases.size()]);
        this.description = b.description;
        this.required = b.required;
        this.defaultValue = b.defaultValue;
    }

    
    // ------------------------------------------------------------------------

    public String getName() {
        return name;
    }
    
    public Class<?> getType() {
        return type;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "ParamInfo[" + name + "]";
    }

    // ------------------------------------------------------------------------

    public static class Builder {
        private String name;
        private Class<?> type;
        private List<String> aliases = new ArrayList<>();
        private String description;
        private boolean required;
        private String defaultValue;
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder type(Class<?> type) {
            this.type = type;
            return this;
        }
        public Builder addAlias(String alias) {
            this.aliases.add(alias);
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder required(boolean required) {
            this.required = required;
            return this;
        }
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
    }
    
}
