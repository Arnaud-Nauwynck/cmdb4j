package org.cmdb4j.core.command;

import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.model.Resource;

/**
 * Provider of Command (metadata + invoker) for Resource objects
 */
public interface CommandProvider {

    default public String getCommandName() { 
        return getCommandInfo().getName();
    }

    public CommandInfo getCommandInfo();
    
    public CommandInvoker getCommandInvoker(CommandCtx ctx, Resource resource);
    
}
