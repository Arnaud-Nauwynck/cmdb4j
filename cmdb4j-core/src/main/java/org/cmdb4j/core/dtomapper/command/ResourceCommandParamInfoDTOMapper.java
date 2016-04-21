package org.cmdb4j.core.dtomapper.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.commandinfo.ResourceCommandParamInfo;
import org.cmdb4j.core.dto.command.ResourceCommandParamInfoDTO;

import com.google.common.collect.ImmutableList;

/**
 * DTO Mapper for ResourceCommandParamInfo<->ResourceCommandParamInfoDTO
 */
public class ResourceCommandParamInfoDTOMapper {

    public List<ResourceCommandParamInfoDTO> toDTO(ImmutableList<ResourceCommandParamInfo> params) {
        return params.stream().map(x -> toDTO(x)).collect(Collectors.toList());
    }
    
    public ResourceCommandParamInfoDTO toDTO(ResourceCommandParamInfo src) {
        ResourceCommandParamInfoDTO res = new ResourceCommandParamInfoDTO();
        res.setIndex(src.getIndex());
        res.setName(src.getName());
        res.setType(src.getType() != null? src.getType().getName() : null);
        res.setAliases(new ArrayList<>(src.getAliases()));
        res.setDescription(src.getDescription());
        res.setRequired(src.isRequired());
        res.setDefaultValue(src.getDefaultValue());
        return res;
    }
    
    
}
