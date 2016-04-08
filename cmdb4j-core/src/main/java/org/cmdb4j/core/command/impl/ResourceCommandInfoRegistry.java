package org.cmdb4j.core.command.impl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.util.CopyOnWriteUtils;

import com.google.common.collect.ImmutableMap;

/**
 * registry of CommandInfo for Resource objects
 */
public class ResourceCommandInfoRegistry {

    private Object lock = new Object();
    
    /**
     * Thread safety: copy-on-write, protected by <code>lock</code>
     */
    private Map<String,CommandInfo> commands = ImmutableMap.of();
    
    // ------------------------------------------------------------------------

    public ResourceCommandInfoRegistry() {
    }

    // ------------------------------------------------------------------------

    public void addCommandInfo(CommandInfo p) {
        synchronized(lock) {
            this.commands = CopyOnWriteUtils.immutableCopyWithPut(commands, p.getName(), p);
        }
    }

    public void removeCommandInfo(CommandInfo p) {
        synchronized(lock) {
            this.commands = CopyOnWriteUtils.immutableCopyWithRemove(commands, p.getName());
        }
    }

    public CommandInfo get(String name) {
        return commands.get(name);
    }
    
    public CommandInfo getOrThrow(String name) {
        CommandInfo res = commands.get(name);
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }
    
    public List<CommandInfo> findAllByPrefix(String prefix) {
        List<CommandInfo> res = commands.values().stream()
                .filter(x -> x.getName().startsWith(prefix))
                .collect(Collectors.toList());
        return res;
    }

}
