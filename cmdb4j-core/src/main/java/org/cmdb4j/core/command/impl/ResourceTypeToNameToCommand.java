package org.cmdb4j.core.command.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.model.reflect.ResourceType;

public class ResourceTypeToNameToCommand {

    private Map<ResourceType,Map<String,ResourceCommand>> type2name2cmd = new HashMap<>();
    
    // ------------------------------------------------------------------------

    public ResourceTypeToNameToCommand() {
    }

    // ------------------------------------------------------------------------

    public void put(ResourceType type, String name, ResourceCommand cmd) {
        Map<String, ResourceCommand> name2cmd = getOrCreateName2Cmd(type);
        name2cmd.put(name, cmd);
    }

    public void remove(ResourceType type, String name, ResourceCommand cmd) {
        Map<String, ResourceCommand> name2cmd = type2name2cmd.get(type);
        if (name2cmd != null) {
            name2cmd.put(name, cmd);
        }
    }
    
    public ResourceCommand get(ResourceType type, String name) {
        Map<String, ResourceCommand> name2cmd = getOrCreateName2Cmd(type);
        return name2cmd.get(name);
    }

    public Map<String, ResourceCommand> getOrNullName2Cmd(ResourceType type) {
        return type2name2cmd.get(type);
    }
    
    protected Map<String, ResourceCommand> getOrCreateName2Cmd(ResourceType type) {
        Map<String, ResourceCommand> name2cmd = type2name2cmd.get(type);
        if (name2cmd == null) {
            name2cmd = new HashMap<>();
            type2name2cmd.put(type, name2cmd);
        }
        return name2cmd;
    }

    public Collection<ResourceCommand> findAllByPrefix(ResourceType type, String prefix) {
        Collection<ResourceCommand> res = new ArrayList<>();
        Map<String, ResourceCommand> name2cmd = type2name2cmd.get(type);
        if (name2cmd != null) {
            for(Map.Entry<String,ResourceCommand> e : name2cmd.entrySet()) {
                if (e.getKey().startsWith(prefix)) {
                    res.add(e.getValue());
                }
            }
        }
        return res;
        
    }
}
