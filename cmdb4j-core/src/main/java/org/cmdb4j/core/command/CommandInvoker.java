package org.cmdb4j.core.command;

public interface CommandInvoker {

    public Object execute(CommandExecutionCtx ctx);
    
}
