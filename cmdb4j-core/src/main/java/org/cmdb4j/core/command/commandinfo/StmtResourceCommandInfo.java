package org.cmdb4j.core.command.commandinfo;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * description of a resource objet Command Statement,<BR/>
 * 
 * equivalent to "@StmtResourceCommand" annotation information on method
 */
public class StmtResourceCommandInfo extends ResourceCommandInfo {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * preCondition expressions to be true before invoking this side-effect command
     */
    private final ImmutableList<ResourceExprInfo> preConditions; 

    /**
     * postCondition expressions to be true after invoking this side-effect command
     */
    private final ImmutableList<ResourceExprInfo> postConditions; 

    /**
     * side-effect diff descriptions when invoking this side-effect command
     */
    private final ImmutableList<ResourceSideEffectInfo> sideEffects; 

    // ------------------------------------------------------------------------

    protected StmtResourceCommandInfo(Builder b) {
        super(b);
        this.preConditions = ImmutableList.copyOf(b.preConditions);
        this.postConditions = ImmutableList.copyOf(b.postConditions);
        this.sideEffects = ImmutableList.copyOf(b.sideEffects);
    }

    public static Builder builder() {
        return new Builder();
    }
    
    // ------------------------------------------------------------------------

    public ImmutableList<ResourceExprInfo> getPreConditions() {
        return preConditions;
    }

    public ImmutableList<ResourceExprInfo> getPostConditions() {
        return postConditions;
    }
    
    public ImmutableList<ResourceSideEffectInfo> getSideEffects() {
        return sideEffects;
    }
    
    // ------------------------------------------------------------------------
    
    public static class Builder extends ResourceCommandInfo.Builder {
        private List<ResourceExprInfo> preConditions = new ArrayList<>(); 
        private List<ResourceExprInfo> postConditions = new ArrayList<>(); 
        private List<ResourceSideEffectInfo> sideEffects = new ArrayList<>(); 

        public StmtResourceCommandInfo build() {
            return new StmtResourceCommandInfo(this);
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

    }
    
}
