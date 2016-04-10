package org.cmdb4j.core.command.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.cmdb4j.core.command.CommandCtx;
import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceType;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.util.FxReaderUtils;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxEvalContext;

/**
 * registry of CommandProvider for Resource objects<BR/>
 * => factory of Command
 */
public class ResourceCommandManager {

    private Object lock = new Object();
    
    /**
     * Thread safety: protected by <code>lock</code>
     */
    private ResourceTypeToNameToCommand type2name2commands = new ResourceTypeToNameToCommand();
    
    // ------------------------------------------------------------------------

    public ResourceCommandManager() {
    }

    // ------------------------------------------------------------------------

    public void addCommandProvider(ResourceCommand p) {
        synchronized(lock) {
            ResourceCommandInfo c = p.getCommandInfo();
            ResourceType resourceType = c.getTargetResourceType();
            String cmdName = c.getName();
            type2name2commands.put(resourceType, cmdName, p);
        }
    }

    public void removeCommandProvider(ResourceCommand p) {
        synchronized(lock) {
            ResourceCommandInfo ci = p.getCommandInfo();
            ResourceType resourceType = ci.getTargetResourceType();
            String cmdName = ci.getName();
            type2name2commands.remove(resourceType, cmdName, p);
        }
    }

    public ResourceCommand get(ResourceType resourceType, String name) {
        synchronized(lock) {
            return type2name2commands.get(resourceType, name);
        }
    }
    
    public ResourceCommand getOrThrow(ResourceType resourceType, String name) {
        ResourceCommand res;
        synchronized(lock) {
            res = type2name2commands.get(resourceType, name);
        }
        if (res == null) {
            throw new NoSuchElementException();
        }
        return res;
    }
    
    public List<ResourceCommand> findAllByPrefix(ResourceType resourceType, String prefix) {
        List<ResourceCommand> res = new ArrayList<>();
        synchronized(lock) {
            res.addAll(type2name2commands.findAllByPrefix(resourceType, prefix));
        }
        return res;
    }

    public Object executeCommand(CommandExecutionCtx ctx, Resource resource, String resourceCmdName, Object[] args) {
        ResourceType resourceType = resource.getType();
        ResourceCommand resourceCmd = getOrThrow(resourceType, resourceCmdName);
        return resourceCmd.execute(ctx, resource, args);
    }

    /**
     * parse cmd line and complete missing params with default values, then evaluate params, and execute method
     * @param ctx
     * @param resource
     * @param resourceCmdName
     * @param cmdArgsLine command line argumanet with format: "arg1={json1..} arg2={json2} ..."
     * @return
     */
    public Object executeCommand(CommandExecutionCtx ctx, Resource resource, String resourceCmdName, String cmdArgsLine) {
        final ResourceType resourceType = resource.getType();
        final ResourceCommand resourceCmd = getOrThrow(resourceType, resourceCmdName);
        final ResourceCommandInfo commandInfo = resourceCmd.getCommandInfo();
        
        // parse cmd line "arg1={json1..} arg2={json2} ..." and complete missing params with default values
        final FxNode[] rawParamNodes = parseCommandParamRawNodes(commandInfo, cmdArgsLine);
        
        // eval "{ "@fx-eval"="phase0:..." }"  then convert result tree->Object using treeToValue()
        CommandCtx commandCtx = ctx.getContext(); // used for funcRegistry + lookupVariables
        final Object[] paramValues = evalRawParamNodesToValues(commandCtx, commandInfo, rawParamNodes);

        // *** the biggy ***
        Object res = resourceCmd.execute(ctx, resource, paramValues);
        
        return res;
    }

    /**
     * parse command line with format "arg1={json1..} arg2={json2} ..." and complete missing params with default values
     * 
     * sample:
     * with following command declaration, 
     * <PRE>
     * @QueryResourceCommand(name="cmd", resourceType="Type1")
     * public static void cmd_bool_int_long_float_double_text_tree(CommandExecutionCtx ctx, Resource target, 
     *           @Param(name="boolParam") boolean boolParam, 
     *           @Param(name="intParam") int intParam, 
     *           @Param(name="longParam") long longParam, 
     *           @Param(name="floatParam") float floatParam, 
     *           @Param(name="doubleParam") double doubleParam, 
     *           @Param(name="textParam") String textParam,
     *           @Param(name="treeParam") FxNode treeParam
     *           ) { }
     * </PRE>
     * The following command lines are accepted (independently of order) 
     * <PRE>
     * "boolParam=true intParam=123 longParam=234 floatParam=345.6 doubleParam=456.7 textParam=\"text\" treeParam={\"id\": 1 }"
     * "treeParam={\"id\": 1 } boolParam=true longParam=234 floatParam=345.6 doubleParam=456.7 textParam=\"text\" intParam=123"
     * </PRE>
     * 
     * <p>
     * Using default values:
     * <PRE>
     * @QueryResourceCommand(name="cmd", resourceType="Type1")
     * public static void cmd_bool_int_long_float_double_text_tree(CommandExecutionCtx ctx, Resource target, 
     *           @Param(name="boolParam", defaultValue="true") boolean boolParam, 
     *           @Param(name="intParam", defaultValue="12") int intParam, 
     *           @Param(name="longParam", defaultValue="23") long longParam, 
     *           @Param(name="floatParam", defaultValue="1.2") float floatParam, 
     *           @Param(name="doubleParam", defaultValue="2.3") double doubleParam, 
     *           @Param(name="textParam", defaultValue="\"text\"") String textParam,
     *           @Param(name="treeParam", defaultValue="{ \"id\": 34 }") String treeParam
     *           ) { .. }
     * </PRE>
     * The following command lines are accepted (independently of order) 
     * <PRE>
     * "intParam=1111"
     * "boolParam=true"
     * "treeParam={\"id\": 1 } boolParam=true longParam=234 floatParam=345.6 doubleParam=456.7 textParam=\"text\" intParam=123"
     * </PRE>
     * 
     * @param commandInfo
     * @param cmdArgsLine
     * @return
     */
    public FxNode[] parseCommandParamRawNodes(ResourceCommandInfo commandInfo, String cmdArgsLine) {
        ImmutableList<ParamInfo> params = commandInfo.getParams();
        final int paramLen = params.size();
        FxNode[] rawParamValues = new FxNode[paramLen];
        
        try {
            cmdArgsLine += " "; // append 1 space to please Jackson parser when finishing parsing on numbers ...
            // otherwise may have 
            // java.lang.RuntimeException: .. 
            // Caused by: com.fasterxml.jackson.core.JsonParseException: Unexpected character (' ' (code 65535 / 0xffff)): Expected space separating root-level values
            Reader cmdArgsReader = new StringReader(cmdArgsLine); 
            Supplier<FxNode> cmdArgsPartialParser = FxJsonUtils.createPartialParser(cmdArgsReader);
            while(cmdArgsReader.ready()) {
                String argAssign = FxReaderUtils.readUntil(cmdArgsReader, "=");
                if (argAssign == null || !argAssign.endsWith("=")) {
                    break; 
                }
                String paramName = argAssign.substring(0, argAssign.length()-1).trim();
                ParamInfo param = commandInfo.getParam(paramName);
                if (param == null) {
                    throw new IllegalArgumentException("param not found '" + paramName + "' ... expecting " + commandInfo.getHelpParamNames());
                }
                FxNode rawParamValue = cmdArgsPartialParser.get();
                rawParamValues[param.getIndex()-2] = rawParamValue; 
            }
        } catch(IOException ex) {
            throw new RuntimeException("should not occur!", ex);
        }
        
        for(int i = 0; i < paramLen; i++) {
            if (rawParamValues[i] == null) {
                ParamInfo param = params.get(i);
                // use default if not set
                String defaultValue = param.getDefaultValue();
                if (defaultValue != null && !defaultValue.isEmpty()) {
                    rawParamValues[i] = FxJsonUtils.jsonTextToTree(defaultValue);
                    continue;
                }
                if (param.isRequired()) {
                    throw new IllegalArgumentException("param '" + param.getName() + "' is required");
                }
            }
        }
        
        return rawParamValues;
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

}
