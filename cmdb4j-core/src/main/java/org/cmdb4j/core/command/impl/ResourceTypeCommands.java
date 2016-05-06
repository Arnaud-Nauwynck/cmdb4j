package org.cmdb4j.core.command.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.model.reflect.ResourceType;

/**
 * List of ResourceCommands for a given ResourceType
 */
public class ResourceTypeCommands {

    private final ResourceType resourceType;
    
    private List<ResourceCommand> cmds = new ArrayList<>();
    
    // indexed by name or by alias
    private Map<String,ResourceCommand> byNameOnly = new TreeMap<>();
    private Map<String,ResourceCommand> byNameOrAlias = new TreeMap<>();
    
    // ------------------------------------------------------------------------

    public ResourceTypeCommands(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    // ------------------------------------------------------------------------

    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void clear() {
        this.cmds.clear();
        this.byNameOnly.clear();
        this.byNameOrAlias.clear();
    }

    public void add(ResourceCommand cmd) {
        this.cmds.add(cmd);
        this.byNameOnly.put(cmd.getCommandName(), cmd);
        this.byNameOrAlias.put(cmd.getCommandName(), cmd);
        Collection<String> aliases = cmd.getCommandInfo().getAliases();
        if (aliases != null && !aliases.isEmpty()) {
            for(String alias : aliases) {
                this.byNameOrAlias.put(alias, cmd);
            }
        }
    }

    public void remove(ResourceCommand cmd) {
        this.cmds.remove(cmd);
        this.byNameOnly.remove(cmd.getCommandName());
        this.byNameOrAlias.remove(cmd.getCommandName());
        Collection<String> aliases = cmd.getCommandInfo().getAliases();
        if (aliases != null && !aliases.isEmpty()) {
            for(String alias : aliases) {
                this.byNameOrAlias.remove(alias);
            }
        }
    }
    
    public ResourceCommand get(String name) {
        ResourceCommand res = findByName(name);
        if (res == null) {
            throw new NoSuchElementException("Command name '" + name + "' not found on type " + resourceType);
        }
        return res;
    }

    public ResourceCommand findByName(String name) {
        return byNameOrAlias.get(name);
    }
    
    public List<ResourceCommand> findAllByPrefix(String prefix) {
        List<ResourceCommand> res = new ArrayList<>();
        for(Map.Entry<String,ResourceCommand> e : byNameOrAlias.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                res.add(e.getValue());
            }
        }
        return res;
    }

    public List<ResourceCommand> listAll() {
        return new ArrayList<>(byNameOnly.values());
    }
    
}
