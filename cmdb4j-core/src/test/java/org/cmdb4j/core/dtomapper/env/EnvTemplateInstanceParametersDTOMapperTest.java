package org.cmdb4j.core.dtomapper.env;

import java.io.File;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.env.EnvTemplateInstanceParameters;
import org.junit.Test;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateInstanceParametersDTOMapperTest {

    protected EnvTemplateInstanceParametersDTOMapper sut = new EnvTemplateInstanceParametersDTOMapper();
    
    @Test
    public void testFromDTO_toDto_fromFxTree_toFxTree() {
        // Prepare
        String filePath = "src/test/java/org/cmdb4j/core/env/impl/template-param1.json";
    	FxSourceLoc source = new FxSourceLoc("", filePath);
		FxNode fxNode = FxFileUtils.readTree(new File(filePath), source);
        // Perform
        EnvTemplateInstanceParametersDTO dto = FxJsonUtils.treeToValue(EnvTemplateInstanceParametersDTO.class, fxNode);
        EnvTemplateInstanceParameters inst = sut.fromDTO(dto); 
        EnvTemplateInstanceParametersDTO dto2 = sut.toDTO(inst);
        FxNode fxNode2 = FxJsonUtils.valueToTree(dto2);
        
        EnvTemplateInstanceParameters inst3 = sut.fromFxTree(fxNode);
        FxNode fxNode3 = sut.toFxTree(inst3);
        
        // Post-check
        FxNodeCheckUtils.checkEquals(fxNode, fxNode2);
        FxNodeCheckUtils.checkEquals(fxNode, fxNode3);
    }
    
}
