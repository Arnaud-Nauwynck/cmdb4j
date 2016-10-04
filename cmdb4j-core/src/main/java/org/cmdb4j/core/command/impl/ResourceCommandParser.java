package org.cmdb4j.core.command.impl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Map;
import java.util.function.Supplier;

import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.ResourceCommandParamsExecutable;
import org.cmdb4j.core.command.ResourceCommandRegistry;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.env.EnvDirsResourceRepositories;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.reflect.ResourceType;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.util.FxReaderUtils;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.model.FxNode;

/**
 * command line parser for text as "<<resourceId>> <<commandName>> param1=json1 param2=json2 .." <BR/>
 * 
 * first resolve "<<resourceId>>" by delegating to EnvDirsResourceTreeRepository <BR/>
 * then resolve "<<commandName>>" by delegating to ResourceTypeToCommandAliasRegistry<BR/>
 * then parse "param123=json123" by delegating to NameJsonValueParser
 */
public class ResourceCommandParser {

    protected EnvDirsResourceRepositories envsResourceTreeRepository;
    
    protected ResourceCommandRegistry resourceTypeToCommandRegistry;
    
    protected NameJsonValueParser nameJsonValueParser = new NameJsonValueParser();
    
    // ------------------------------------------------------------------------
    
    public ResourceCommandParser(
            EnvDirsResourceRepositories envsResourceTreeRepository,
            ResourceCommandRegistry resourceTypeToCommandRegistry
            ) {
        this.envsResourceTreeRepository = envsResourceTreeRepository;
        this.resourceTypeToCommandRegistry = resourceTypeToCommandRegistry;
    }

    // ------------------------------------------------------------------------
    
    public ResourceCommandParamsExecutable parse(String cmdLine) {
        // Step 1: parse text and extract evaluable expressions for resourceId, commandName, commandArgs
        FxNode resourceIdExprNode;
        FxNode commandNameExprNode;
        Map<String, FxNode> rawNameValues;
        
        PushbackReader pushBackReader = new PushbackReader(new StringReader(cmdLine + " ")); // append 1 space to please Jackson parser when finishing parsing numbers ...
        try {
            resourceIdExprNode = FxReaderUtils.readNameOrPathExpr(pushBackReader, true);
            FxReaderUtils.skipWs(pushBackReader);
            
            commandNameExprNode = FxReaderUtils.readNameExpr(pushBackReader);
            FxReaderUtils.skipWs(pushBackReader);
            
            // parse json for "param1=json1 param2=json2 .."
            Supplier<FxNode> cmdArgsPartialParser = FxJsonUtils.createPartialParser(pushBackReader);
            rawNameValues = nameJsonValueParser.parseNamedValues(pushBackReader, cmdArgsPartialParser);
        } catch (IOException ex) {
            throw new RuntimeException("Should not occur", ex);
        }
        
        // Step 2: resolve Resource, ResourceType, Command for parsed text
        
        // TODO... expression not supported yet, only name, path or quoted strings ...
        String resourceId = FxNodeValueUtils.nodeToString(resourceIdExprNode);
        String commandName = FxNodeValueUtils.nodeToString(commandNameExprNode);
        
        // resolve String resourceId -> Resource
        Resource resource = envsResourceTreeRepository.getResourceById(ResourceId.valueOf(resourceId));
        
        // resolve String commandName -> ResourceCommand
        ResourceType resourceType = resource.getType();
        ResourceCommand resourceCommand = resourceTypeToCommandRegistry.get(resourceType, commandName);
        
        // resolve indexed params by name, and use default values for missing args 
        final ResourceCommandInfo commandInfo = resourceCommand.getCommandInfo();
        FxNode[] rawParamValues = nameJsonValueParser.namedParamsToIndexedArgs(commandInfo, rawNameValues);

        return new ResourceCommandParamsExecutable(resourceCommand, resource, rawParamValues);
    }

}
