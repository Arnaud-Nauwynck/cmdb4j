package org.cmdb4j.core.command.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.model.reflect.ResourceType;

import com.google.common.collect.ImmutableList;

/**
 * registry of ResourceCommand for Resource types, by names and by aliases<BR/>
 * 
 */
public class ResourceTypeToCommandAliasRegistry {

    private Object lock = new Object();
    
    
    /**
     * Thread safety: protected by <code>lock</code>
     */
    protected List<ResourceCommand> commands = new ArrayList<>();
    
    /**
     * indexed commands, by type -> nameOrAlias -> ResourceCommand
     * Thread safety: protected by <code>lock</code>
     */
    private ResourceTypeToNameToCommand type2name2commands = new ResourceTypeToNameToCommand();
    
    // ------------------------------------------------------------------------

    public ResourceTypeToCommandAliasRegistry() {
    }

    // ------------------------------------------------------------------------

    public void addCommands(Collection<ResourceCommand> cmds) {
        synchronized(lock) {
            for(ResourceCommand cmd : cmds) {
                addCommand(cmd);
            }
        }
    }
    
    public void addCommand(ResourceCommand p) {
        synchronized(lock) {
            commands.add(p);
            doAddIndexedCommand(p);
        }
    }


    public void removeCommand(ResourceCommand p) {
        synchronized(lock) {
            commands.remove(p);
            // reeval indexed (clear and reeval in order for overriden name/aliases!)
            type2name2commands.clear();
            for (ResourceCommand cmd : commands) {
                doAddIndexedCommand(cmd);
            }
        }
    }
    
    private void doAddIndexedCommand(ResourceCommand p) {
        ResourceCommandInfo c = p.getCommandInfo();
        ResourceType resourceType = c.getTargetResourceType();
        String cmdName = c.getName();
        type2name2commands.put(resourceType, cmdName, p);
        ImmutableList<String> aliases = c.getAliases();
        if (aliases != null && !aliases.isEmpty()) {
            for(String alias : aliases) {
                type2name2commands.put(resourceType, alias, p);
            }
        }
    }

    public ResourceCommand get(ResourceType resourceType, String name) {
        synchronized(lock) {
            return type2name2commands.get(resourceType, name);
        }
    }
    
    public ResourceCommand getOrThrow(ResourceType resourceType, String name) {
        ResourceCommand res;
        synchronized(lock) {
            res = type2name2commands.get(resourceType, name);
        }
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }
    
    public List<ResourceCommand> findAllByPrefix(ResourceType resourceType, String prefix) {
        List<ResourceCommand> res = new ArrayList<>();
        synchronized(lock) {
            res.addAll(type2name2commands.findAllByPrefix(resourceType, prefix));
        }
        return res;
    }


}
