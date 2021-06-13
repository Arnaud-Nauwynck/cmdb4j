package org.cmdb4j.core.dtomapper.env;

import java.util.Map;

import org.cmdb4j.core.dto.env.EnvTemplateParamDescrDTO;
import org.cmdb4j.core.env.EnvTemplateParamDescr;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateParamDescrDTOMapper {

    public EnvTemplateParamDescr fromDTO(EnvTemplateParamDescrDTO src) {
    	FxSourceLoc source = new FxSourceLoc("env-instance", "");
        FxNode defaultValue = Fx2JacksonUtils.jsonNodeToFxTree(src.getDefaultValue(), source);
        Map<String,FxNode> extraProperties = Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties(), source);
        return new EnvTemplateParamDescr(src.getName(), src.getType(), src.getDisplayName(), src.getComment(), defaultValue, extraProperties);
    }

    public EnvTemplateParamDescrDTO toDTO(EnvTemplateParamDescr src) {
        JsonNode defaultValue = Fx2JacksonUtils.fxTreeToJsonNode(src.getDefaultValue());
        Map<String,JsonNode> extraProperties = Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties());
        return new EnvTemplateParamDescrDTO(src.getName(), src.getType(), src.getDisplayName(), src.getComment(), defaultValue, extraProperties);
    }

    /** parse FxNode -> EnvTemplateParamDescr */
    public EnvTemplateParamDescr fromFxTree(FxNode src) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateParamDescrDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateParamDescrDTO.class, src);
        return fromDTO(tmpres);
    }

    /** format EnvTemplateParamDescr -> FxNode */
    public FxNode toFxTree(EnvTemplateParamDescr src) {
        EnvTemplateParamDescrDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

}
