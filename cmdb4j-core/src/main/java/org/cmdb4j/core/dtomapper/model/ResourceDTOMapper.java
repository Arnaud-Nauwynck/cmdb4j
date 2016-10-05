package org.cmdb4j.core.dtomapper.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdb4j.core.dto.model.ResourceDTO;
import org.cmdb4j.core.model.Resource;

import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;

public class ResourceDTOMapper {

    public ResourceDTO toDTO(Resource src) {
        ResourceDTO res = new ResourceDTO();
        res.setId(src.getIdAsString());
        res.setType(src.getTypeName());
        res.setObjData(Fx2JacksonUtils.fxTreeToJsonNode(src.getObjData()));
        
        res.setRequireResources(Resource.toIdAsStringSet(src.getRequireResources().values()));
        res.setSubscribeResources(Resource.toIdAsStringSet(src.getSubscribeResources().values()));
        res.setInvRequiredFromResources(Resource.toIdAsStringSet(src.getInvRequiredFromResources().values()));
        res.setInvSubscribedFromResources(Resource.toIdAsStringSet(src.getInvSubscribedFromResources().values()));
        
        res.setAllTags(res.getTags());        
        return res;
    }

    public List<ResourceDTO> toDTOs(Collection<Resource> src) {
        List<ResourceDTO> res = new ArrayList<>();
        for(Resource e : src) {
            res.add(toDTO(e));
        }
        return res;
    }

}
