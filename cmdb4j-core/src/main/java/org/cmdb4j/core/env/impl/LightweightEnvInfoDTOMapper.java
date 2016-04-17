package org.cmdb4j.core.env.impl;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.dto.env.LightweightEnvInfoDTO;
import org.cmdb4j.core.env.EnvResourceRepository;

public class LightweightEnvInfoDTOMapper {

    protected EnvTemplateInstanceParametersDTOMapper templateParamsDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    
    public LightweightEnvInfoDTO toDTO(EnvResourceRepository src) {
        EnvTemplateInstanceParametersDTO templateParams = templateParamsDTOMapper.toDTO(src.getTemplateParams());
        return new LightweightEnvInfoDTO(src.getEnvName(), templateParams);
    }
    
}
