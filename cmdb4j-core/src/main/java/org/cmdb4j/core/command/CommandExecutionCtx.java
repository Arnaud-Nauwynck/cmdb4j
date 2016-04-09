package org.cmdb4j.core.command;

/**
 * context of a running execution command
 */
public class CommandExecutionCtx {

    private CommandCtx context;
    
    // ------------------------------------------------------------------------

    public CommandExecutionCtx(CommandCtx context) {
        this.context = context;
    }

    // ------------------------------------------------------------------------
    
    public CommandCtx getContext() {
        return context;
    }
    

}
