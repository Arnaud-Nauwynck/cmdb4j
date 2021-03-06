package org.cmdb4j.core.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.commandinfo.ResourceCommandId;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.command.impl.ResourceTypeCommands;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

/**
 * registry of ResourceCommand by Resource types, by names/aliases<BR/>
 * 
 */
public class ResourceCommandRegistry {

    private Object lock = new Object();
    
    /**
     * 
     */
    private ResourceTypeRepository resourceTypeRepository;
    
    /**
     * Thread safety: protected by <code>lock</code>
     */
    protected List<ResourceCommand> commands = new ArrayList<>();
    
    /**
     * indexed commands, by type -> nameOrAlias -> ResourceCommand
     * Thread safety: protected by <code>lock</code>
     */
    private Map<ResourceType,ResourceTypeCommands> type2commands = new HashMap<>();
    
    // ------------------------------------------------------------------------

    public ResourceCommandRegistry(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
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
            type2commands.clear();
            for (ResourceCommand cmd : commands) {
                doAddIndexedCommand(cmd);
            }
        }
    }
    
    private void doAddIndexedCommand(ResourceCommand p) {
        ResourceCommandInfo c = p.getCommandInfo();
        ResourceType resourceType = c.getTargetResourceType();
        ResourceTypeCommands typeCmds = getOrCreateTypeCommands(resourceType);
        typeCmds.add(p);
    }

    private ResourceTypeCommands getOrCreateTypeCommands(ResourceType resourceType) {
        ResourceTypeCommands res = type2commands.get(resourceType);
        if (res == null) {
            res = new ResourceTypeCommands(resourceType);
            type2commands.put(resourceType, res);
        }
        return res;
    }
    
    public ResourceCommand get(ResourceType resourceType, String name) {
        synchronized(lock) {
            ResourceTypeCommands typeCmds = getOrCreateTypeCommands(resourceType);
            return typeCmds.get(name);
        }
    }

    public List<ResourceCommandInfo> lisCommandInfos() {
        synchronized(lock) {
            return cmdsToInfos(commands);
        }
    }
    
    public static List<ResourceCommandInfo> cmdsToInfos(Collection<ResourceCommand> src) {
        return src.stream().map(x -> x.getCommandInfo()).collect(Collectors.toList());
    }
    
    public List<ResourceCommand> findByTypeHierarchyAndPrefix(ResourceType resourceType, String prefix) {
        Set<ResourceType> superTypes = resourceTypeRepository.superTypeHierarchyOf(resourceType);
        synchronized(lock) {
            List<ResourceCommand> res = new ArrayList<>();
            for(ResourceType superType : superTypes) {
                ResourceTypeCommands typeCmds = getOrCreateTypeCommands(superType);
                res.addAll(typeCmds.findAllByPrefix(prefix));
            }
            return res;
        }
    }

    public List<ResourceCommand> findByTypeHierarchy(ResourceType resourceType) {
        Set<ResourceType> superTypes = resourceTypeRepository.superTypeHierarchyOf(resourceType);
        synchronized(lock) {
            List<ResourceCommand> res = new ArrayList<>();
            for(ResourceType superType : superTypes) {
                ResourceTypeCommands typeCmds = getOrCreateTypeCommands(superType);
                res.addAll(typeCmds.listAll());
            }
            return res;
        }
    }

    public ResourceCommand get(ResourceCommandId id) {
        ResourceType resourceType = resourceTypeRepository.get(id.getResourceTypeId());
        return get(resourceType, id.getCommandName());
    }
    
}
