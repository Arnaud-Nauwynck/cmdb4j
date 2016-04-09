package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdb4j.core.model.reflect.ResourceType;

import com.google.common.collect.ImmutableList;

/**
 * description of a resource objet Command,<BR/> 
 * equivalent to "@QueryResourceCommand" / "@StmtResourceCommand" annotation information on method
 */
public class ResourceCommandInfo implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private final ResourceType targetResourceType;
    
    /**
     * text for the command 
     */
    private final String name;

    /**
     * text aliases for the command 
     */
    private final ImmutableList<String> aliases;
    
    /**
     * description of command parameters
     */
    private final ImmutableList<ParamInfo> params;
    
    /**
     *
     */
    private final String category;

    /**
     * help message for this command
     */
    private final String help;
    
    // ------------------------------------------------------------------------

    protected ResourceCommandInfo(Builder b) {
        this.targetResourceType = b.targetResourceType;
        this.name = b.name;
        this.aliases = ImmutableList.copyOf(b.aliases);
        this.params = ImmutableList.copyOf(b.params);
        this.category = b.category;
        this.help = b.help;
    }

    // ------------------------------------------------------------------------

    public ResourceType getTargetResourceType() {
        return targetResourceType;
    }
    
    public String getName() {
        return name;
    }
    
    public ImmutableList<String> getAliases() {
        return aliases;
    }

    public ImmutableList<ParamInfo> getParams() {
        return params;
    }
    
    public String getCategory() {
        return category;
    }

    public String getHelp() {
        return help;
    }
    
    // ------------------------------------------------------------------------
    
    public static class Builder {
        private ResourceType targetResourceType;
        private String name;
        private List<String> aliases = new ArrayList<>();
        private List<ParamInfo> params = new ArrayList<>();
        private String category;
        private String help;
        
        public Builder targetResourceType(ResourceType p) {
            this.targetResourceType = p;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder addAlias(String p) {
            this.aliases.add(p);
            return this;
        }
        public Builder addAllAlias(Collection<String> p) {
            this.aliases.addAll(p);
            return this;
        }
        public Builder addParam(ParamInfo p) {
            this.params.add(p);
            return this;
        }
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        public Builder help(String help) {
            this.help = help;
            return this;
        }

    }
    
}
