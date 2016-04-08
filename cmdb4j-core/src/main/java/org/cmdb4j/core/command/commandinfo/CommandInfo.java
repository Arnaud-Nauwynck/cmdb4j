package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.cmdb4j.core.model.reflect.ResourceType;

import com.google.common.collect.ImmutableList;

/**
 * description of a resource objet Command, equivalent to "@Command" annotation information on method
 */
public class CommandInfo implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private final ResourceType targetResourceType;
    
    /**
     * text for the command 
     */
    private final String name;

    private final ParamInfo[] params;
    
//    /**
//     * text aliases for the command 
//     */
//    private final List<String> aliases;

    /**
     *
     */
    private final String category;

    /**
     * 
     */
    private final ResourceExprInfo[] preConditions; 

    /**
     * 
     */
    private final ResourceExprInfo[] postConditions; 

    /**
     *
     */
    private final ResourceSideEffectInfo[] sideEffects; 

    /**
     * help message for this command
     */
    private final String help;
    
    // ------------------------------------------------------------------------

    public CommandInfo(Builder b) {
        this.targetResourceType = b.targetResourceType;
        this.name = b.name;
        this.params = b.params.toArray(new ParamInfo[b.params.size()]);
        this.category = b.category;
        this.preConditions = b.preConditions.toArray(new ResourceExprInfo[b.preConditions.size()]); 
        this.postConditions = b.postConditions.toArray(new ResourceExprInfo[b.postConditions.size()]);
        this.sideEffects = b.sideEffects.toArray(new ResourceSideEffectInfo[b.sideEffects.size()]); 
        this.help = b.help;
    }

    // ------------------------------------------------------------------------

    public ResourceType getTargetResourceType() {
        return targetResourceType;
    }
    
    public String getName() {
        return name;
    }
    
    public List<ParamInfo> getParams() {
        return ImmutableList.copyOf(params);
    }
    
    public String getCategory() {
        return category;
    }

    public List<ResourceExprInfo> getPreConditions() {
        return ImmutableList.copyOf(preConditions);
    }

    public List<ResourceExprInfo> getPostConditions() {
        return ImmutableList.copyOf(postConditions);
    }
    
    public List<ResourceSideEffectInfo> getSideEffects() {
        return ImmutableList.copyOf(sideEffects);
    }
    
    public String getHelp() {
        return help;
    }
    
    // ------------------------------------------------------------------------
    
    public static class Builder {
        private ResourceType targetResourceType;
        private String name;
        private List<ParamInfo> params = new ArrayList<>();
        private String category;
        private List<ResourceExprInfo> preConditions = new ArrayList<>(); 
        private List<ResourceExprInfo> postConditions = new ArrayList<>(); 
        private List<ResourceSideEffectInfo> sideEffects = new ArrayList<>(); 
        private String help;
        
        public Builder targetResourceType(ResourceType p) {
            this.targetResourceType = p;
            return this;
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder addParam(ParamInfo p) {
            this.params.add(p);
            return this;
        }
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        public Builder addPreCondition(ResourceExprInfo p) {
            this.preConditions.add(p);
            return this;
        }
        public Builder addPreConditions(List<ResourceExprInfo> p) {
            this.preConditions.addAll(p);
            return this;
        }
        public Builder addPostCondition(ResourceExprInfo p) {
            this.postConditions.add(p);
            return this;
        }
        public Builder addPostConditions(List<ResourceExprInfo> p) {
            this.postConditions.addAll(p);
            return this;
        }
        public Builder addSideEffect(ResourceSideEffectInfo p) {
            this.sideEffects.add(p);
            return this;
        }
        public Builder addSideEffects(List<ResourceSideEffectInfo> p) {
            this.sideEffects.addAll(p);
            return this;
        }
        public Builder help(String help) {
            this.help = help;
            return this;
        }

    }
    
}
