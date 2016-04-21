package org.cmdb4j.core.dto.command;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for StmtResourceCommandInfo
 */
public class StmtResourceCommandInfoDTO extends ResourceCommandInfoDTO {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    /**
     * preCondition expressions to be true before invoking this side-effect command
     */
    private List<ResourceExprInfoDTO> preConditions = new ArrayList<>(); 

    /**
     * postCondition expressions to be true after invoking this side-effect command
     */
    private List<ResourceExprInfoDTO> postConditions = new ArrayList<>(); 

    /**
     * side-effect diff descriptions when invoking this side-effect command
     */
    private List<ResourceSideEffectInfoDTO> sideEffects = new ArrayList<>();

    // ------------------------------------------------------------------------

    public StmtResourceCommandInfoDTO() {
    }

    // ------------------------------------------------------------------------

    public List<ResourceExprInfoDTO> getPreConditions() {
        return preConditions;
    }

    public void setPreConditions(List<ResourceExprInfoDTO> preConditions) {
        this.preConditions = preConditions;
    }

    public List<ResourceExprInfoDTO> getPostConditions() {
        return postConditions;
    }

    public void setPostConditions(List<ResourceExprInfoDTO> postConditions) {
        this.postConditions = postConditions;
    }

    public List<ResourceSideEffectInfoDTO> getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(List<ResourceSideEffectInfoDTO> sideEffects) {
        this.sideEffects = sideEffects;
    }

}
