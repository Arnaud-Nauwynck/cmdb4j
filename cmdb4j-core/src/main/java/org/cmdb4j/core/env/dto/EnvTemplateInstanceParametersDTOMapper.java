package org.cmdb4j.core.env.dto;

import java.util.Map;

import org.cmdb4j.core.env.EnvTemplateInstanceParameters;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateInstanceParametersDTOMapper {

    public static EnvTemplateInstanceParameters fromDTO(EnvTemplateInstanceParametersDTO src) {
        Map<String,FxNode> templateParameters = Fx2JacksonUtils.jsonNodesToFxTrees(src.getParameters());
        Map<String,FxNode> metaParameters = Fx2JacksonUtils.jsonNodesToFxTrees(src.getMetaParameters());
        return new EnvTemplateInstanceParameters(src.getSourceTemplateName(), templateParameters, metaParameters);
    }

    public static EnvTemplateInstanceParametersDTO toDTO(EnvTemplateInstanceParameters src) {
        Map<String,JsonNode> templateParameters = Fx2JacksonUtils.fxTreesToJsonNodes(src.getParameters());
        Map<String,JsonNode> metaParameters = Fx2JacksonUtils.fxTreesToJsonNodes(src.getMetaParameters());
        return new EnvTemplateInstanceParametersDTO(src.getSourceTemplateName(), templateParameters, metaParameters);
    }

    /** parse FxNode -> EnvTemplateInstanceParameters */
    public static EnvTemplateInstanceParameters fromFxTree(FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateInstanceParametersDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateInstanceParametersDTO.class, src);
        return fromDTO(tmpres);
    }

    /** format EnvTemplateInstanceParameters -> FxNode */
    public static FxNode toFxTree(EnvTemplateInstanceParameters src) {
        EnvTemplateInstanceParametersDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

}
