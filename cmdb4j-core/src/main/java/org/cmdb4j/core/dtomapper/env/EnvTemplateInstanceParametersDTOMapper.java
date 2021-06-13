package org.cmdb4j.core.dtomapper.env;

import java.util.Map;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.env.EnvTemplateInstanceParameters;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

public class EnvTemplateInstanceParametersDTOMapper {

    private static final String PROP_sourceTemplateName = "sourceTemplateName";
    private static final String PROP_parameters = "parameters";
    private static final String PROP_extraProperties = "extraProperties";
    
    public EnvTemplateInstanceParameters fromDTO(EnvTemplateInstanceParametersDTO src) {
        if (src == null) {
            return null;
        }
    	FxSourceLoc source = new FxSourceLoc("env-instance", "");
        Map<String,FxNode> parameters = Fx2JacksonUtils.jsonNodesToFxTrees(src.getParameters(), source);
        Map<String,FxNode> extraProperties = Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties(), source);
        return new EnvTemplateInstanceParameters(src.getEnvName(), src.getSourceTemplateName(), parameters, extraProperties);
    }

    public EnvTemplateInstanceParametersDTO toDTO(EnvTemplateInstanceParameters src) {
        if (src == null) {
            return null;
        }
        Map<String,JsonNode> parameters = Fx2JacksonUtils.fxTreesToJsonNodes(src.getParameters());
        Map<String,JsonNode> extraProperties = Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties());
        return new EnvTemplateInstanceParametersDTO(src.getEnvName(), src.getSourceTemplateName(), parameters, extraProperties);
    }

    /** parse FxNode -> EnvTemplateInstanceParameters */
    public EnvTemplateInstanceParameters fromFxTree(FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateInstanceParametersDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateInstanceParametersDTO.class, src);
        return fromDTO(tmpres);
    }

    /** format EnvTemplateInstanceParameters -> FxNode */
    public FxNode toFxTree(EnvTemplateInstanceParameters src) {
        EnvTemplateInstanceParametersDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

    public void parseMergeNode(EnvTemplateInstanceParameters.Builder res, FxObjNode src) {
        String templateSourceEnvName = FxNodeValueUtils.getString(src, PROP_sourceTemplateName);
        if (templateSourceEnvName != null) {
            res.sourceTemplateName(templateSourceEnvName);
        }
        
        // extract "params" and "metaparams", concatenate to result
        FxObjNode paramsNode = FxNodeValueUtils.getObjOrThrow(src, PROP_parameters);
        res.putAllParameters(paramsNode.fieldsMap());

        FxObjNode metaParamsNode = FxNodeValueUtils.getObjOrNull(src, PROP_extraProperties);
        if (metaParamsNode != null) {
            res.putAllExtraPropreties(metaParamsNode.fieldsMap());
        }
    }

    public FxObjNode formatNode(EnvTemplateInstanceParameters src) {
    	FxSourceLoc source = new FxSourceLoc("env-instance", "");
        FxObjNode res = new FxMemRootDocument(source).setContentObj(source);
        res.put(PROP_sourceTemplateName, src.getSourceTemplateName(), source);
        
        FxObjNode paramsNode = res.putObj(PROP_parameters, source);
        FxNodeCopyVisitor.copyChildMapTo(paramsNode, src.getParameters());
        
        FxObjNode metaParamsNode = res.putObj(PROP_extraProperties, source);
        FxNodeCopyVisitor.copyChildMapTo(metaParamsNode, src.getExtraProperties());
        
        return res;
    }

}
