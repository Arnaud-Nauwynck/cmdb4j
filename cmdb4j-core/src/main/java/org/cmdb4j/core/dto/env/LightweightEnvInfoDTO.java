package org.cmdb4j.core.dto.env;

import java.io.Serializable;

/**
 * Ligtweight DTO for information on a EnvResourceRepository<BR/>
 * (does not contains full Resources info)
 */
public class LightweightEnvInfoDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private String envName;
    
    private EnvTemplateInstanceParametersDTO templateParams;

    // ------------------------------------------------------------------------
    
    public LightweightEnvInfoDTO() {
    }
    
    public LightweightEnvInfoDTO(String envName, EnvTemplateInstanceParametersDTO templateParams) {
        this.envName = envName;
        this.templateParams = templateParams;
    }
    
    // ------------------------------------------------------------------------

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public EnvTemplateInstanceParametersDTO getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(EnvTemplateInstanceParametersDTO templateParams) {
        this.templateParams = templateParams;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        if (templateParams == null) {
            return envName;
        }
        return envName + " (from '" + templateParams.getSourceTemplateName() + "' with params: " + templateParams.getParameters() + ")";
    }

}
