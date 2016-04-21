package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.model.reflect.ResourceType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
    private final ImmutableList<ResourceCommandParamInfo> params;
    
    /**
     * indexed <code>params</code> by name
     */
    private final ImmutableMap<String,ResourceCommandParamInfo> paramsByName;
    
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
        Map<String,ResourceCommandParamInfo> tmpParamsByName = new LinkedHashMap<>();
        b.params.forEach(p -> tmpParamsByName.put(p.getName(), p));
        this.paramsByName = ImmutableMap.copyOf(tmpParamsByName);
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

    public ImmutableList<ResourceCommandParamInfo> getParams() {
        return params;
    }
    
    public String getHelpParamNames() {
        StringBuilder sb = new StringBuilder();
        for(ResourceCommandParamInfo p : params) {
            sb.append(p.getName());
            List<String> aliases = p.getAliases();
            if (aliases != null && !aliases.isEmpty()) {
                sb.append("(alias: " + aliases + ")");
            }
            sb.append(" ");
        }
        return sb.toString();
    }
    
    public ImmutableMap<String,ResourceCommandParamInfo> getParamsByName() {
        return paramsByName;
    }

    public ResourceCommandParamInfo getParamAt(int index) {
        return params.get(index);
    }

    public ResourceCommandParamInfo getParam(String name) {
        return paramsByName.get(name);
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
        private List<ResourceCommandParamInfo> params = new ArrayList<>();
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
        public Builder addParam(ResourceCommandParamInfo p) {
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
