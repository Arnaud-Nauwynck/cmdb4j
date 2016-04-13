package org.cmdb4j.core.command.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.util.FxReaderUtils;
import fr.an.fxtree.model.FxNode;


public class NameJsonValueParser {

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
        Map<String, FxNode> rawNameValues = parseNamedValues(cmdArgsLine);

        FxNode[] rawParamValues = namedParamsToIndexedArgs(commandInfo, rawNameValues);
        
        return rawParamValues;
    }

    public Map<String, FxNode> parseNamedValues(String cmdArgsLine) {
        cmdArgsLine += " "; 
        // append 1 space to please Jackson parser when finishing parsing numbers ...
        // otherwise may have 
        // java.lang.RuntimeException: .. 
        // Caused by: com.fasterxml.jackson.core.JsonParseException: Unexpected character (' ' (code 65535 / 0xffff)): Expected space separating root-level values
        Reader cmdArgsReader = new StringReader(cmdArgsLine);
        Supplier<FxNode> cmdArgsPartialParser = FxJsonUtils.createPartialParser(cmdArgsReader);
        return parseNamedValues(cmdArgsReader, cmdArgsPartialParser);
    }
    
    public Map<String, FxNode> parseNamedValues(Reader cmdArgsReader, Supplier<FxNode> cmdArgsPartialParser) {
        Map<String,FxNode> rawNameValues = new LinkedHashMap<>(); 
        try {
            while(cmdArgsReader.ready()) {
                String paramName = FxReaderUtils.readUntil(cmdArgsReader, '=', false);
                if (paramName == null) {
                    break; 
                }
                FxNode rawParamValue = cmdArgsPartialParser.get();
                rawNameValues.put(paramName, rawParamValue); 
            }
        } catch(IOException ex) {
            throw new RuntimeException("should not occur!", ex);
        }
        return rawNameValues;
    }

    
    public FxNode[] namedParamsToIndexedArgs(ResourceCommandInfo commandInfo, Map<String, FxNode> rawNameValues) {
        FxNode[] rawParamValues;
        // resolve param index by name, convert Map<String,FxNode> -> FxNode[]
        ImmutableList<ParamInfo> params = commandInfo.getParams();
        final int paramLen = params.size();
        rawParamValues = new FxNode[paramLen];
        
        for(Map.Entry<String, FxNode> e : rawNameValues.entrySet()) {
            String paramName = e.getKey();
            ParamInfo param = commandInfo.getParam(paramName);
            if (param == null) {
                throw new IllegalArgumentException("param not found '" + paramName + "' ... expecting " + commandInfo.getHelpParamNames());
            }            
            rawParamValues[param.getIndex()-2] = e.getValue();  
        }
        // fill default values when missing param
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

}
