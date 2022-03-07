package org.cmdb4j.core.env.template;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.dtomapper.env.EnvTemplateInstanceParametersDTOMapper;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

public class EnvTemplateInstanceParametersMapper {

    private static final String PROP_sourceTemplateName = "sourceTemplateName";
    private static final String PROP_parameters = "parameters";
    private static final String PROP_extraProperties = "extraProperties";

    EnvTemplateInstanceParametersDTOMapper dtoMapper = new EnvTemplateInstanceParametersDTOMapper();
    
    /** parse FxNode -> EnvTemplateInstanceParameters */
    public EnvTemplateInstanceParameters fromFxTree(FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateInstanceParametersDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateInstanceParametersDTO.class, src);
        return dtoMapper.fromDTO(tmpres);
    }

    /** format EnvTemplateInstanceParameters -> FxNode */
    public FxNode toFxTree(EnvTemplateInstanceParameters src) {
        EnvTemplateInstanceParametersDTO tmpres = dtoMapper.toDTO(src);
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
        FxSourceLoc loc = new FxSourceLoc("env-instance", src.getEnvName());
        FxObjNode res = new FxMemRootDocument(loc).setContentObj(loc);
        res.put(PROP_sourceTemplateName, src.getSourceTemplateName(), loc);
        
        FxObjNode paramsNode = res.putObj(PROP_parameters, loc);
        FxNodeCopyVisitor.copyChildMapTo(paramsNode, src.getParameters());
        
        FxObjNode metaParamsNode = res.putObj(PROP_extraProperties, loc);
        FxNodeCopyVisitor.copyChildMapTo(metaParamsNode, src.getExtraProperties());
        
        return res;
    }

}
