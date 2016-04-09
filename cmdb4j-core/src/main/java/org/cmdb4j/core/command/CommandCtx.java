package org.cmdb4j.core.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * context for evaluating commands on Resource objects
 */
public class CommandCtx {

    private final String name;

    private Map<String,Object> variables = Collections.synchronizedMap(new LinkedHashMap<>());
    
    // ------------------------------------------------------------------------
    
    public CommandCtx(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }

    public Map<String,Object> getVariables() {
        return variables;
    }

    public Object putVariable(String name, Object value) {
        return variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "CommandCtx[" + name + "]";
    }
    
}
