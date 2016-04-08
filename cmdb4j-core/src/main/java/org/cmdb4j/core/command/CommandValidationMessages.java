package org.cmdb4j.core.command;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceExprInfo;
import org.cmdb4j.core.model.Resource;

public class CommandValidationMessages {

    public static class CommandParamValidationMessage {
        private ParamInfo param;
        private String message;
        
        public CommandParamValidationMessage(ParamInfo param, String message) {
            this.param = param;
            this.message = message;
        }

        public ParamInfo getParam() {
            return param;
        }

        public void setParam(ParamInfo param) {
            this.param = param;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        
    }

    public static class PreConditionValidationMessage {
        private ResourceExprInfo resourceExpr;
        private String message;
        
        public PreConditionValidationMessage(ResourceExprInfo resourceExpr, String message) {
            this.resourceExpr = resourceExpr;
            this.message = message;
        }

        public ResourceExprInfo getResourceExpr() {
            return resourceExpr;
        }

        public void setResourceExpr(ResourceExprInfo resourceExpr) {
            this.resourceExpr = resourceExpr;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
        
        
    }

    
    private Resource resource;
    private CommandInfo command;
    private Map<String,CommandParamValidationMessage> paramMessages = new LinkedHashMap<>();
    private Map<String,PreConditionValidationMessage> preConditionMessages = new LinkedHashMap<>();

    // ------------------------------------------------------------------------
    
    public CommandValidationMessages(Resource resource, CommandInfo command, 
            Map<String, CommandParamValidationMessage> paramMessages,
            Map<String, PreConditionValidationMessage> preConditionMessages) {
        this.resource = resource;
        this.command = command;
        if (paramMessages != null) {
            this.paramMessages.putAll(paramMessages);
        }
        if (preConditionMessages != null) {
            this.preConditionMessages.putAll(preConditionMessages);
        }
    }

    // ------------------------------------------------------------------------
    
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public CommandInfo getCommand() {
        return command;
    }

    public void setCommand(CommandInfo command) {
        this.command = command;
    }

    public Map<String, CommandParamValidationMessage> getParamMessages() {
        return paramMessages;
    }

    public void setParamMessages(Map<String, CommandParamValidationMessage> paramMessages) {
        this.paramMessages = paramMessages;
    }

    public Map<String, PreConditionValidationMessage> getPreConditionMessages() {
        return preConditionMessages;
    }

    public void setPreConditionMessages(Map<String, PreConditionValidationMessage> preConditionMessages) {
        this.preConditionMessages = preConditionMessages;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "CommandValidationMessages [" 
                + ((resource != null)? resource.getId() : "null")
                + " "
                + ((command != null)? command.getName() : "null")
                + "]";
    }


    
}
