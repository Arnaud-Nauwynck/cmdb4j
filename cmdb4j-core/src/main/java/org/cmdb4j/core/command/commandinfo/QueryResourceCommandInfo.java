package org.cmdb4j.core.command.commandinfo;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * description of a resource objet Command query,<BR/> 
 * 
 * equivalent to "@QueryResourceCommand" annotation information on method
 */
public class QueryResourceCommandInfo extends ResourceCommandInfo {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * expressions to be true for querying this command
     */
    private final ImmutableList<ResourceExprInfo> conditions; 

    // ------------------------------------------------------------------------

    protected QueryResourceCommandInfo(Builder b) {
        super(b);
        this.conditions = ImmutableList.copyOf(b.conditions);
    }

    public static Builder builder() {
        return new Builder();
    }
    
    // ------------------------------------------------------------------------

    public ImmutableList<ResourceExprInfo> getConditions() {
        return conditions;
    }

    // ------------------------------------------------------------------------
    
    public static class Builder extends ResourceCommandInfo.Builder {
        private List<ResourceExprInfo> conditions = new ArrayList<>(); 
        
        public QueryResourceCommandInfo build() {
            return new QueryResourceCommandInfo(this);
        }
        
        public Builder addCondition(ResourceExprInfo p) {
            this.conditions.add(p);
            return this;
        }
        public Builder addConditions(List<ResourceExprInfo> p) {
            this.conditions.addAll(p);
            return this;
        }

    }
    
}
