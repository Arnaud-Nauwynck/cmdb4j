package org.cmdb4j.core.env.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdb4j.core.env.EnvTemplateDescr;
import org.cmdb4j.core.env.EnvTemplateParamDescr;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescrDTOMapper {

    public static EnvTemplateDescr fromDTO(EnvTemplateDescrDTO src) {
        Map<String, EnvTemplateParamDescr> paramDescriptions = new LinkedHashMap<>();
        src.getParamDescriptions().forEach((n,p) -> {
            paramDescriptions.put(n, EnvTemplateParamDescr.fromDTO(p));
        });
        Map<String,FxNode> extraProperties = Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties());
        FxNode rawNode = Fx2JacksonUtils.jsonNodeToFxTree(src.getRawNode());
        return new EnvTemplateDescr(src.getName(), paramDescriptions, extraProperties, rawNode);
    }

    public static EnvTemplateDescrDTO toDTO(EnvTemplateDescr src) {
        Map<String, EnvTemplateParamDescrDTO> paramDescriptions = new LinkedHashMap<>();
        src.getParamDescriptions().forEach((n,p) -> {
            paramDescriptions.put(n, EnvTemplateParamDescr.toDTO(p));
        });
        Map<String,JsonNode> extraProperties = Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties());
        JsonNode rawNode = Fx2JacksonUtils.fxTreeToJsonNode(src.getRawNode());
        return new EnvTemplateDescrDTO(src.getName(), paramDescriptions, extraProperties, rawNode);
    }

    /** parse FxNode -> EnvTemplateDescr */
    public static EnvTemplateDescr fromFxTree(FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateDescrDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateDescrDTO.class, src);
        return fromDTO(tmpres);
    }

    /** format EnvTemplateDescr -> FxNode */
    public static FxNode toFxTree(EnvTemplateDescr src) {
        EnvTemplateDescrDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

}
