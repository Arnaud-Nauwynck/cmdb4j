package org.cmdb4j.core.command.impl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.CommandProvider;
import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.util.CopyOnWriteUtils;

import com.google.common.collect.ImmutableMap;

/**
 * registry of CommandProvider for Resource objects<BR/>
 * => factory of Command
 */
public class ResourceCommandManager {

    private Object lock = new Object();
    
    /**
     * Thread safety: copy-on-write, protected by <code>lock</code>
     */
    private Map<String,CommandProvider> commandProviders = ImmutableMap.of();
    
    // ------------------------------------------------------------------------

    public ResourceCommandManager() {
    }

    // ------------------------------------------------------------------------

    public void addCommandProvider(CommandProvider p) {
        synchronized(lock) {
            CommandInfo c = p.getCommandInfo();
            this.commandProviders = CopyOnWriteUtils.immutableCopyWithPut(commandProviders, c.getName(), p);
        }
    }

    public void removeCommandProvider(CommandProvider p) {
        synchronized(lock) {
            CommandInfo c = p.getCommandInfo();
            this.commandProviders = CopyOnWriteUtils.immutableCopyWithRemove(commandProviders, c.getName());
        }
    }

    public CommandProvider get(String name) {
        return commandProviders.get(name);
    }
    
    public CommandProvider getOrThrow(String name) {
        CommandProvider res = commandProviders.get(name);
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }
    
    public List<CommandProvider> findAllByPrefix(String prefix) {
        List<CommandProvider> res = commandProviders.values().stream()
                .filter(x -> x.getCommandName().startsWith(prefix))
                .collect(Collectors.toList());
        return res;
    }

}
