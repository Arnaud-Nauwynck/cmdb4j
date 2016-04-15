package org.cmdb4j.core.env.dto;

import java.util.Map;

import org.cmdb4j.core.env.EnvTemplateInstanceParameters;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

public class EnvTemplateInstanceParametersDTOMapper {

    private static final String PROP_sourceTemplateName = "sourceTemplateName";
    private static final String PROP_parameters = "parameters";
    private static final String PROP_metaParameters = "metaParameters";
    
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

    public static void parseMergeNode(EnvTemplateInstanceParameters.Builder res, FxObjNode src) {
        String templateSourceEnvName = FxNodeValueUtils.getString(src, PROP_sourceTemplateName);
        if (templateSourceEnvName != null) {
            res.sourceTemplateName(templateSourceEnvName);
        }
        
        // extract "params" and "metaparams", concatenate to result
        FxObjNode paramsNode = FxNodeValueUtils.getObjOrThrow(src, PROP_parameters);
        res.putAllTemplateParameters(paramsNode.fieldsHashMapCopy());

        FxObjNode metaParamsNode = FxNodeValueUtils.getObjOrNull(src, PROP_metaParameters);
        if (metaParamsNode != null) {
            res.putAllMetaParameters(metaParamsNode.fieldsHashMapCopy());
        }
    }

    public static FxObjNode formatNode(EnvTemplateInstanceParameters src) {
        FxObjNode res = new FxMemRootDocument().setContentObj();
        res.put(PROP_sourceTemplateName, src.getSourceTemplateName());
        
        FxObjNode paramsNode = res.putObj(PROP_parameters);
        FxNodeCopyVisitor.copyChildMapTo(paramsNode, src.getParameters());
        
        FxObjNode metaParamsNode = res.putObj(PROP_metaParameters);
        FxNodeCopyVisitor.copyChildMapTo(metaParamsNode, src.getMetaParameters());
        
        return res;
    }

}
