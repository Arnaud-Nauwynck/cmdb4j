package org.cmdb4j.core.command;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class CommandSession {

    private Map<String,CommandCtx> contexts = new HashMap<>();
    
    private CommandCtx currentContext;
    
    // ------------------------------------------------------------------------

    public CommandSession() {
    }

    // ------------------------------------------------------------------------

    public void putNewContext(String name) {
        CommandCtx ctx = new CommandCtx(name);
        contexts.put(name, ctx);
    }
    
    public void removeContext(String name) {
        contexts.remove(name);
        if (currentContext != null && currentContext.getName().equals(name)) {
            currentContext = null;
        }
    }
    
    public CommandCtx getContext(String name) {
        return contexts.get(name);
    }
    
    public void setCurrentContext(String name) {
        currentContext = contexts.get(name);
    }
    
    public CommandCtx getCurrentContext() {
        return currentContext;
    }
}
