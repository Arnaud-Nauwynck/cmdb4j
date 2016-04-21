package org.cmdb4j.core.dtomapper.command;

import java.util.ArrayList;

import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.dto.command.ResourceCommandInfoDTO;
import org.cmdb4j.core.dtomapper.model.ResourceTypeDTOMapper;

/**
 * DTO Mapper for ResourceCommandInfo<->ResourceCommandInfoDTO
 */
public class ResourceCommandInfoDTOMapper {

    protected ResourceCommandParamInfoDTOMapper paramInfoMapper = new ResourceCommandParamInfoDTOMapper();
    
    // ------------------------------------------------------------------------

    public ResourceCommandInfoDTOMapper() {
    }

    // ------------------------------------------------------------------------

    public ResourceCommandInfoDTO toDTO(ResourceCommandInfo src) {
        ResourceCommandInfoDTO res = new ResourceCommandInfoDTO();
        toDTO(src, res);
        return res;
    }
    
    public void toDTO(ResourceCommandInfo src, ResourceCommandInfoDTO res) {
        res.setTargetResourceType(ResourceTypeDTOMapper.toName(src.getTargetResourceType()));
        res.setName(src.getName());
        res.setAliases((res.getAliases() != null)? new ArrayList<>(res.getAliases()): new ArrayList<>());
        res.setParams(paramInfoMapper.toDTO(src.getParams()));
        res.setCategory(src.getCategory());
        res.setHelp(src.getHelp());
    }

}
