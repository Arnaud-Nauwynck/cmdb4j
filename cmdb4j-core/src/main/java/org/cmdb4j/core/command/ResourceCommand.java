package org.cmdb4j.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.model.Resource;

/**
 * Command (metadata + invoke) for Resource objects
 */
public interface ResourceCommand {

    default public String getCommandName() { 
        return getCommandInfo().getName();
    }

    public CommandInfo getCommandInfo();
    
    public Object execute(CommandExecutionCtx ctx, Resource resource, Object[] args);
    
    
    // ------------------------------------------------------------------------
    
    public static abstract class AbstractResourceCommand implements ResourceCommand {
        protected final CommandInfo commandInfo;
        
        protected AbstractResourceCommand(CommandInfo commandInfo) {
            this.commandInfo = commandInfo;
        }

        @Override
        public CommandInfo getCommandInfo() {
            return commandInfo;
        }
        
    }

    // ------------------------------------------------------------------------

    public static class MethodResourceCommand extends AbstractResourceCommand {
        protected final Object targetObject;
        protected final Method method;
        
        public MethodResourceCommand(CommandInfo commandInfo, Object targetObject, Method method) {
            super(commandInfo);
            this.targetObject = targetObject;
            this.method = method;
        }
        
        @Override
        public Object execute(CommandExecutionCtx ctx, Resource resource, Object[] args) {
            Object res;
            Object[] ctxArgs = new Object[args.length + 2];
            ctxArgs[0] = ctx;
            ctxArgs[1] = resource;
            System.arraycopy(args, 0, ctxArgs, 2, args.length);
            try {
                res = method.invoke(targetObject, ctxArgs);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Failed to invoke command method '" + method + "'", ex);
            }
            return res;
        }

    }
    
}
