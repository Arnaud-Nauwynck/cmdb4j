package org.cmdb4j.core.env.dto;

import java.io.File;

import org.cmdb4j.core.env.EnvTemplateInstanceParameters;
import org.junit.Test;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateInstanceParametersDTOMapperTest {

    @Test
    public void testFromDTO_toDto_fromFxTree_toFxTree() {
        // Prepare
        FxNode fxNode = FxFileUtils.readTree(new File("src/test/java/org/cmdb4j/core/env/dto/template-param1.json"));
        // Perform
        EnvTemplateInstanceParametersDTO dto = FxJsonUtils.treeToValue(EnvTemplateInstanceParametersDTO.class, fxNode);
        EnvTemplateInstanceParameters inst = EnvTemplateInstanceParametersDTOMapper.fromDTO(dto); 
        EnvTemplateInstanceParametersDTO dto2 = EnvTemplateInstanceParametersDTOMapper.toDTO(inst);
        FxNode fxNode2 = FxJsonUtils.valueToTree(dto2);
        
        EnvTemplateInstanceParameters inst3 = EnvTemplateInstanceParametersDTOMapper.fromFxTree(fxNode);
        FxNode fxNode3 = EnvTemplateInstanceParametersDTOMapper.toFxTree(inst3);
        
        // Post-check
        FxNodeCheckUtils.checkEquals(fxNode, fxNode2);
        FxNodeCheckUtils.checkEquals(fxNode, fxNode3);
    }
    
}
