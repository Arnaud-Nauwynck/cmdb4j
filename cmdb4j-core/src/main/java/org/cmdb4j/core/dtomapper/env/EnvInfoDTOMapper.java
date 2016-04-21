package org.cmdb4j.core.dtomapper.env;

import java.util.List;

import org.cmdb4j.core.dto.env.EnvInfoDTO;
import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.dto.model.ResourceDTO;
import org.cmdb4j.core.dtomapper.model.ResourceDTOMapper;
import org.cmdb4j.core.env.EnvResourceRepository;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceRepository;

public class EnvInfoDTOMapper {

    protected EnvTemplateInstanceParametersDTOMapper templateParamsDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    protected ResourceDTOMapper resourceDTOMapper = new ResourceDTOMapper();
    
    // ------------------------------------------------------------------------

    public EnvInfoDTOMapper() {
    }

    // ------------------------------------------------------------------------

    public EnvInfoDTO toDTO(EnvResourceRepository src) {
        EnvTemplateInstanceParametersDTO templateParams = templateParamsDTOMapper.toDTO(src.getTemplateParams());
        ResourceRepository resourceRepository = src.getResourceRepository();
        List<Resource> resources = resourceRepository.findAll();
        List<ResourceDTO> resourceDTOs = resourceDTOMapper.toDTOs(resources);

        return new EnvInfoDTO(src.getEnvName(), templateParams, resourceDTOs);
    }

}
