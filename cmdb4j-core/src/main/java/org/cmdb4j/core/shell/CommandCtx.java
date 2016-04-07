package org.cmdb4j.core.shell;

public class CommandCtx {

    private final String name;

    // ------------------------------------------------------------------------
    
    public CommandCtx(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CommandCtx[" + name + "]";
    }
    
}