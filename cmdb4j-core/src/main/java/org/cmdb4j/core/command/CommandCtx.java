package org.cmdb4j.core.command;

import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.func.FxEvalContext;

/**
 * context for evaluating commands on Resource objects
 */
public class CommandCtx {

    private final String name;

    private FxEvalContext fxEvalContext = new FxEvalContext(null, FxStdFuncs.stdFuncRegistry()); 
    
    // ------------------------------------------------------------------------
    
    public CommandCtx(String name) {
        this.name = name;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public FxEvalContext getFxEvalContext() {
        return fxEvalContext;
    }
    
    public void setFxEvalContext(FxEvalContext fxEvalContext) {
        this.fxEvalContext = fxEvalContext;
    }

    /** helper for getFxEvalContext().putVariable() */
    public void putVariable(Object key, Object value) {
        fxEvalContext.putVariable(key, value);
    }

    /** helper for getFxEvalContext().lookupVariable() */
    public Object lookupVariable(Object key) {
        return fxEvalContext.lookupVariable(key);
    }
    
    // ------------------------------------------------------------------------


    @Override
    public String toString() {
        return "CommandCtx[" + name + "]";
    }
    
}
