package org.cmdb4j.core.command;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.command.commandinfo.ResourceCommandParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceExprInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.util.CmdbAssertUtils;

public class CommandValidationMessages {

    public static class CommandParamValidationMessage {
        private ResourceCommandParamInfo param;
        private String message;
        
        public CommandParamValidationMessage(ResourceCommandParamInfo param, String message) {
            CmdbAssertUtils.checkNotNull(param);
            CmdbAssertUtils.checkNotNull(message);
            this.param = param;
            this.message = message;
        }

        public ResourceCommandParamInfo getParam() {
            return param;
        }

        public void setParam(ResourceCommandParamInfo param) {
            this.param = param;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return param.getName() + ": " + message;
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
        
        @Override
        public String toString() {
            return resourceExpr.getExprText() + ": " + message;
        }
        
    }

    
    private Resource resource;
    
    private ResourceCommandInfo command;
    
    private Map<String,CommandParamValidationMessage> paramMessages = new LinkedHashMap<>();
    
    private Map<String,PreConditionValidationMessage> preConditionMessages = new LinkedHashMap<>();

    // ------------------------------------------------------------------------
    
    public CommandValidationMessages(Resource resource, ResourceCommandInfo command, 
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

    public ResourceCommandInfo getCommand() {
        return command;
    }

    public void setCommand(ResourceCommandInfo command) {
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
