package org.cmdb4j.core.env.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.dto.env.EnvTemplateDescrDTO;
import org.cmdb4j.core.dto.env.EnvTemplateParamDescrDTO;
import org.cmdb4j.core.env.EnvTemplateDescr;
import org.cmdb4j.core.env.EnvTemplateParamDescr;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescrDTOMapper {

    protected EnvTemplateParamDescrDTOMapper paramDescrDTOMapper = new EnvTemplateParamDescrDTOMapper();
    
    // ------------------------------------------------------------------------

    public EnvTemplateDescrDTOMapper() {
    }

    // ------------------------------------------------------------------------

    public EnvTemplateDescr fromDTO(EnvTemplateDescrDTO src) {
        List<EnvTemplateParamDescr> paramDescriptions = new ArrayList<>();
        src.getParamDescriptions().forEach(p -> {
            paramDescriptions.add(paramDescrDTOMapper.fromDTO(p));
        });
        Map<String,FxNode> extraProperties = Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties());
        FxNode rawNode = Fx2JacksonUtils.jsonNodeToFxTree(src.getRawNode());
        return new EnvTemplateDescr(src.getName(), src.getDisplayName(), src.getComment(), 
            paramDescriptions, extraProperties, rawNode);
    }

    public EnvTemplateDescrDTO toDTO(EnvTemplateDescr src) {
        List<EnvTemplateParamDescrDTO> paramDescriptions = new ArrayList<>();
        src.getParamDescriptions().forEach(p -> {
            paramDescriptions.add(paramDescrDTOMapper.toDTO(p));
        });
        Map<String,JsonNode> extraProperties = Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties());
        JsonNode rawNode = Fx2JacksonUtils.fxTreeToJsonNode(src.getRawNode());
        return new EnvTemplateDescrDTO(src.getName(), src.getDisplayName(), src.getComment(), 
            paramDescriptions, extraProperties, rawNode);
    }

    /** parse FxNode -> EnvTemplateDescr */
    public EnvTemplateDescr fromFxTree(String envName, FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateDescrDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateDescrDTO.class, src);
        tmpres.setName(envName);
        return fromDTO(tmpres);
    }

    /** format EnvTemplateDescr -> FxNode */
    public FxNode toFxTree(EnvTemplateDescr src) {
        EnvTemplateDescrDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

}
