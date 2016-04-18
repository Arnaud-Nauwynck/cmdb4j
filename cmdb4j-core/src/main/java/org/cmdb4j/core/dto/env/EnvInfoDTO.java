package org.cmdb4j.core.dto.env;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.cmdb4j.core.dto.model.ResourceDTO;

/**
 * DTO for information on a EnvResourceRepository<BR/>
 */
public class EnvInfoDTO extends LightweightEnvInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    protected List<ResourceDTO> resources = new ArrayList<ResourceDTO>();
    
    // ------------------------------------------------------------------------
    
    public EnvInfoDTO() {
    }
    
    public EnvInfoDTO(String envName, EnvTemplateInstanceParametersDTO templateParams, List<ResourceDTO> resources) {
        super(envName, templateParams);
        this.resources = resources;
    }
    
    // ------------------------------------------------------------------------

    public List<ResourceDTO> getResources() {
        return resources;
    }

    public void setResources(List<ResourceDTO> resources) {
        this.resources = resources;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        String res = super.toString();
        if (resources != null && !resources.isEmpty()) {
            res += " resources count:" + resources.size();
        }
        return res;
    }

}
