package org.cmdb4j.core.dtomapper.env;

import java.util.List;

import org.cmdb4j.core.dto.env.EnvInfoDTO;
import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.dto.model.ResourceDTO;
import org.cmdb4j.core.dtomapper.model.ResourceDTOMapper;
import org.cmdb4j.core.env.CmdbEnv;
import org.cmdb4j.core.model.Resource;

public class EnvInfoDTOMapper {

    protected EnvTemplateInstanceParametersDTOMapper templateParamsDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    protected ResourceDTOMapper resourceDTOMapper = new ResourceDTOMapper();
    
    // ------------------------------------------------------------------------

    public EnvInfoDTOMapper() {
    }

    // ------------------------------------------------------------------------

    public EnvInfoDTO toDTO(CmdbEnv src) {
    	String envName = src.getEnvName();
        EnvTemplateInstanceParametersDTO templateParams = templateParamsDTOMapper.toDTO(src.getTemplateParams());
        List<Resource> resources = src.findAll();
        List<ResourceDTO> resourceDTOs = resourceDTOMapper.toDTOs(resources);
		return new EnvInfoDTO(envName, templateParams, resourceDTOs);
    }

}
