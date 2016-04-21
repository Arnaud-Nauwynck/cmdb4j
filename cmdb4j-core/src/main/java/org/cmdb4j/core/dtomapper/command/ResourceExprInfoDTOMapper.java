package org.cmdb4j.core.dtomapper.command;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cmdb4j.core.command.commandinfo.ResourceExprInfo;
import org.cmdb4j.core.dto.command.ResourceExprInfoDTO;

/**
 * DTO Mapper for ResourceExprInfoDTO
 */
public class ResourceExprInfoDTOMapper {

    public ResourceExprInfoDTO toDTO(ResourceExprInfo src) {
        return new ResourceExprInfoDTO(src.getExprText());
    }

    public List<ResourceExprInfoDTO> toDTOs(Collection<ResourceExprInfo> src) {
        return src.stream().map(x -> toDTO(x)).collect(Collectors.toList());
    }
    
}
