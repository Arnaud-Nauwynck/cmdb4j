package org.cmdb4j.core.command.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceType;

/**
 * registry of CommandProvider for Resource objects<BR/>
 * => factory of Command
 */
public class ResourceCommandManager {

    private Object lock = new Object();
    
    /**
     * Thread safety: protected by <code>lock</code>
     */
    private ResourceTypeToNameToCommand type2name2commands = new ResourceTypeToNameToCommand();
    
    // ------------------------------------------------------------------------

    public ResourceCommandManager() {
    }

    // ------------------------------------------------------------------------

    public void addCommandProvider(ResourceCommand p) {
        synchronized(lock) {
            ResourceCommandInfo c = p.getCommandInfo();
            ResourceType resourceType = c.getTargetResourceType();
            String cmdName = c.getName();
            type2name2commands.put(resourceType, cmdName, p);
        }
    }

    public void removeCommandProvider(ResourceCommand p) {
        synchronized(lock) {
            ResourceCommandInfo ci = p.getCommandInfo();
            ResourceType resourceType = ci.getTargetResourceType();
            String cmdName = ci.getName();
            type2name2commands.remove(resourceType, cmdName, p);
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

    public Object executeCommand(String name, CommandExecutionCtx ctx, Resource resource, Object[] args) {
        ResourceType resourceType = resource.getType();
        ResourceCommand resourceCmd = getOrThrow(resourceType, name);
        return resourceCmd.execute(ctx, resource, args);
    }
    
}
