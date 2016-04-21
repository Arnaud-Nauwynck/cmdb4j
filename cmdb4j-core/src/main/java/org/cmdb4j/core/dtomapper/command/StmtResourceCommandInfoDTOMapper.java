package org.cmdb4j.core.dtomapper.command;

import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.command.commandinfo.StmtResourceCommandInfo;
import org.cmdb4j.core.dto.command.ResourceCommandInfoDTO;
import org.cmdb4j.core.dto.command.StmtResourceCommandInfoDTO;

/**
 * DTO Mapper for StmtResourceCommandInfo
 */
public class StmtResourceCommandInfoDTOMapper extends ResourceCommandInfoDTOMapper {

    protected ResourceExprInfoDTOMapper resourceExprDTOMapper = new ResourceExprInfoDTOMapper();
    protected ResourceSideEffectInfoDTOMapper sideEffectDTOMapper = new ResourceSideEffectInfoDTOMapper();
    
    // ------------------------------------------------------------------------

    public StmtResourceCommandInfoDTOMapper() {
    }

    // ------------------------------------------------------------------------

    @Override
    public StmtResourceCommandInfoDTO toDTO(ResourceCommandInfo src) {
        StmtResourceCommandInfoDTO res = new StmtResourceCommandInfoDTO();
        toDTO(src, res);
        return res;
    }
    
    @Override
    public void toDTO(ResourceCommandInfo src, ResourceCommandInfoDTO res) {
        toDTO((StmtResourceCommandInfo) src, (StmtResourceCommandInfoDTO) res);
    }

    public void toDTO(StmtResourceCommandInfo src, StmtResourceCommandInfoDTO res) {
        super.toDTO(src, res);
        res.setPreConditions(resourceExprDTOMapper.toDTOs(src.getPreConditions()));
        res.setPostConditions(resourceExprDTOMapper.toDTOs(src.getPostConditions()));
        res.setSideEffects(sideEffectDTOMapper.toDTOs(src.getSideEffects()));
    }

}
