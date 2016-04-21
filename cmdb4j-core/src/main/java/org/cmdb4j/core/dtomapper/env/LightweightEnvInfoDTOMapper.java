package org.cmdb4j.core.dtomapper.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.dto.env.LightweightEnvInfoDTO;
import org.cmdb4j.core.env.EnvResourceRepository;

public class LightweightEnvInfoDTOMapper {

    protected EnvTemplateInstanceParametersDTOMapper templateParamsDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    
    public LightweightEnvInfoDTO toDTO(EnvResourceRepository src) {
        EnvTemplateInstanceParametersDTO templateParams = templateParamsDTOMapper.toDTO(src.getTemplateParams());
        return new LightweightEnvInfoDTO(src.getEnvName(), templateParams);
    }
    
    public List<LightweightEnvInfoDTO> toDTOs(Collection<EnvResourceRepository> src) {
        List<LightweightEnvInfoDTO> res = new ArrayList<>();
        for(EnvResourceRepository e : src) {
            res.add(toDTO(e));
        }
        return res;
    }


}
