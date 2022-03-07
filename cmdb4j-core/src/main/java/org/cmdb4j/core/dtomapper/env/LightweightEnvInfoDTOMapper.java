package org.cmdb4j.core.dtomapper.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdb4j.core.dto.env.LightweightEnvInfoDTO;
import org.cmdb4j.core.env.CmdbEnv;

import lombok.val;

public class LightweightEnvInfoDTOMapper {

    protected EnvTemplateInstanceParametersDTOMapper templateParamsDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    
    public LightweightEnvInfoDTO toDTO(CmdbEnv src) {
    	String envName = src.getEnvName();
        val templateParams = templateParamsDTOMapper.toDTO(src.getTemplateParams());
		return new LightweightEnvInfoDTO(envName, templateParams);
    }
    
    public List<LightweightEnvInfoDTO> toDTOs(Collection<CmdbEnv> src) {
        val res = new ArrayList<LightweightEnvInfoDTO>();
        for(val e : src) {
            res.add(toDTO(e));
        }
        return res;
    }


}
