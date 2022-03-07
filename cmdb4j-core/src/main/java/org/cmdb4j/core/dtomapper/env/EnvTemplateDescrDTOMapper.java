package org.cmdb4j.core.dtomapper.env;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.dto.env.EnvTemplateDescrDTO;
import org.cmdb4j.core.dto.env.EnvTemplateParamDescrDTO;
import org.cmdb4j.core.env.input.ResourceFileContent;
import org.cmdb4j.core.env.template.EnvTemplateDescr;
import org.cmdb4j.core.env.template.EnvTemplateParamDescr;

import com.fasterxml.jackson.databind.JsonNode;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.json.jackson.Fx2JacksonUtils;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescrDTOMapper {

    protected EnvTemplateParamDescrDTOMapper paramDescrDTOMapper = new EnvTemplateParamDescrDTOMapper();
    
    // ------------------------------------------------------------------------

    public EnvTemplateDescrDTOMapper() {
    }

    // ------------------------------------------------------------------------

    public EnvTemplateDescr fromDTO(EnvTemplateDescrDTO src, List<ResourceFileContent> contents) {
        List<EnvTemplateParamDescr> paramDescriptions = new ArrayList<>();
        List<EnvTemplateParamDescrDTO> srcParamDescriptions = src.getParamDescriptions();
        if (srcParamDescriptions != null && !srcParamDescriptions.isEmpty()) {
            for(EnvTemplateParamDescrDTO srcParamDescr : srcParamDescriptions) {
                paramDescriptions.add(paramDescrDTOMapper.fromDTO(srcParamDescr));
            }
        }
        FxSourceLoc srcLoc = new FxSourceLoc("env-template", src.getName());
        Map<String,FxNode> extraProperties = Fx2JacksonUtils.jsonNodesToFxTrees(src.getExtraProperties(), srcLoc);
        return new EnvTemplateDescr(src.getName(), src.getDisplayName(), src.getComment(), 
            paramDescriptions, extraProperties, contents);
    }

    public EnvTemplateDescrDTO toDTO(EnvTemplateDescr src) {
        List<EnvTemplateParamDescrDTO> paramDescriptions = new ArrayList<>();
        List<EnvTemplateParamDescr> srcParamDescriptions = src.getParamDescriptions();
        if (srcParamDescriptions != null && !srcParamDescriptions.isEmpty()) {
            for(EnvTemplateParamDescr srcParamDescr : srcParamDescriptions) {
                paramDescriptions.add(paramDescrDTOMapper.toDTO(srcParamDescr));
            }
        }
        Map<String,JsonNode> extraProperties = Fx2JacksonUtils.fxTreesToJsonNodes(src.getExtraProperties());
        return new EnvTemplateDescrDTO(src.getName(), src.getDisplayName(), src.getComment(), 
            paramDescriptions, extraProperties);
    }

    /** parse FxNode -> EnvTemplateDescr */
    public EnvTemplateDescr fromFxTree(String envName, FxNode src, List<ResourceFileContent> contents) {
        // use FxNode->DTO mapping + DTO->Obj copy (shorter equivalent than hand-parsing FxNode..) 
        EnvTemplateDescrDTO tmpres = FxJsonUtils.treeToValue(EnvTemplateDescrDTO.class, src);
        tmpres.setName(envName);
        return fromDTO(tmpres, contents);
    }

    /** format EnvTemplateDescr -> FxNode */
    public FxNode toFxTree(EnvTemplateDescr src) {
        EnvTemplateDescrDTO tmpres = toDTO(src);
        return FxJsonUtils.valueToTree(tmpres);
    }

}
