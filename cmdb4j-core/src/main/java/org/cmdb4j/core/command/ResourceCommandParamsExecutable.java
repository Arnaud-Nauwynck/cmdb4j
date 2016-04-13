package org.cmdb4j.core.command;

import java.util.Arrays;

import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.model.Resource;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxEvalContext;

/**
 * executable command for [resource,command,rawParamNodes]:<BR/> 
 * ready to call <code>execute(CommandExecutionCtx)</code>, <BR/>
 * by first pre-processing evaluation of FxNode params,
 * then convert to Object values, then invoke resourceCommand 
 * 
 */
public class ResourceCommandParamsExecutable {

    protected ResourceCommand resourceCommand;
    
    protected Resource resource;
    
    protected FxNode[] rawParamNodes;

    // ------------------------------------------------------------------------
    
    public ResourceCommandParamsExecutable(ResourceCommand resourceCommand, Resource resource, FxNode[] rawParamNodes) {
        this.resourceCommand = resourceCommand;
        this.resource = resource;
        this.rawParamNodes = rawParamNodes;
    }
    
    // ------------------------------------------------------------------------

    public ResourceCommand getResourceCommand() {
        return resourceCommand;
    }

    public Resource getResource() {
        return resource;
    }

    public FxNode[] getRawParamNodes() {
        return rawParamNodes;
    }
    
    public Object execute(CommandExecutionCtx ctx) {
        // eval "{ "@fx-eval"="phase0:..." }"  then convert result tree->Object using treeToValue()
        CommandCtx commandCtx = ctx.getContext(); // used for funcRegistry + lookupVariables
        ResourceCommandInfo commandInfo = resourceCommand.getCommandInfo();

        final Object[] paramValues = evalRawParamNodesToValues(commandCtx, commandInfo, rawParamNodes);

        // *** The biggy : execute command ***
        Object res = resourceCommand.execute(ctx, resource, paramValues);
        
        return res;
    }

    public Object[] evalRawParamNodesToValues(CommandCtx commandContext, final ResourceCommandInfo commandInfo, final FxNode[] rawParamNodes) {
        final ImmutableList<ParamInfo> params = commandInfo.getParams();
        final int paramLen = params.size();

        // preprocess: eval rawParamNodes -> paramNodes
        final FxNode[] paramNodes = new FxNode[paramLen];
        final String phase = "phase0";
        final FxEvalContext argsEvalCtx = new FxEvalContext(commandContext.getFxEvalContext(), null);
        for(int i = 0; i < paramLen; i++) {
            ParamInfo param = params.get(i);
            try {
                paramNodes[i] = FxPhaseRecursiveEvalFunc.evalPhase(phase, argsEvalCtx, rawParamNodes[i], null);
            } catch(Exception ex) {
                throw new IllegalArgumentException("Failed to eval param '" + param.getName() + "' json value to " + param.getType(), ex);
            }
        }

        // convert json paramNodes -> Object paramValues
        final Object[] paramValues = new Object[paramLen];
        for(int i = 0; i < paramLen; i++) {
            FxNode paramNode = paramNodes[i];
            ParamInfo param = params.get(i);
            // replace variables with ctx Object variables
            Object convertValue = null;
            if (paramNode != null && paramNode.isTextual()) {
                String paramText = paramNode.textValue();
                if (paramText.length() > 1 && paramText.charAt(0) == '$') {
                    String variableName = paramText.substring(1);
                    convertValue = commandContext.lookupVariable(variableName);
                    paramValues[i] = convertValue;
                    continue;
                }
            }
            if (convertValue == null) {
                Class<?> paramType = param.getType();
                if (paramType.isInstance(paramNode)) {
                    // expecting FxNode (or sub-class FxIntNode, ...) => no need to convert treeToValue
                    paramValues[i] = paramNode;
                } else {
                    try {
                        paramValues[i] = FxJsonUtils.treeToValue(paramType, paramNode);
                    } catch(RuntimeException ex) {
                        throw new IllegalArgumentException("Failed to convert param '" + param.getName() + "' json value to " + paramType, ex);
                    }
                }
            }
        }
        return paramValues;
    }
    
    
    @Override
    public String toString() {
        return "ResourceCommandParamsExecutable [" 
                + resource.getId() + " " + resourceCommand.getCommandName()
                + ((rawParamNodes != null && rawParamNodes.length != 0)? ", args=" + Arrays.toString(rawParamNodes) : "")
                + "]";
    }
   
}
