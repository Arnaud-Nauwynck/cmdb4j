package org.cmdb4j.core.shell.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Immutable description of Command, equivalent to "@Command" annotation information on method
 */
public class CommandInfo {

    /**
     * text for the command 
     */
    private final String text;

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
        this.text = b.text;
        this.category = b.category;
        this.preConditions = b.preConditions.toArray(new ResourceExprInfo[b.preConditions.size()]); 
        this.postConditions = b.postConditions.toArray(new ResourceExprInfo[b.postConditions.size()]);
        this.sideEffects = b.sideEffects.toArray(new ResourceSideEffectInfo[b.sideEffects.size()]); 
        this.help = b.help;
    }

    // ------------------------------------------------------------------------

    public String getText() {
        return text;
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
        
        private String text;
        private String category;
        private List<ResourceExprInfo> preConditions = new ArrayList<>(); 
        private List<ResourceExprInfo> postConditions = new ArrayList<>(); 
        private List<ResourceSideEffectInfo> sideEffects = new ArrayList<>(); 
        private String help;
        
        public Builder text(String text) {
            this.text = text;
            return this;
        }
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        public Builder addPreConditions(List<ResourceExprInfo> p) {
            this.preConditions.addAll(p);
            return this;
        }
        public Builder addPostConditions(List<ResourceExprInfo> p) {
            this.postConditions.addAll(p);
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
