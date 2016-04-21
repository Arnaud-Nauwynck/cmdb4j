package org.cmdb4j.core.dtomapper.command;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.commandinfo.ResourceSideEffectInfo;
import org.cmdb4j.core.dto.command.ResourceSideEffectInfoDTO;

/**
 * DTO Mapper for ResourceSideEffectInfoDTO
 */
public class ResourceSideEffectInfoDTOMapper {

    public ResourceSideEffectInfoDTO toDTO(ResourceSideEffectInfo src) {
        return new ResourceSideEffectInfoDTO(src.getSideEffectText());
    }

    public List<ResourceSideEffectInfoDTO> toDTOs(Collection<ResourceSideEffectInfo> src) {
        return src.stream().map(x -> toDTO(x)).collect(Collectors.toList());
    }
    
}
