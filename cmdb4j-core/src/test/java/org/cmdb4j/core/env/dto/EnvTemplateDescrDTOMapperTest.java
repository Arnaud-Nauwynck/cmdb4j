package org.cmdb4j.core.env.dto;

import java.io.File;

import org.cmdb4j.core.env.EnvTemplateDescr;
import org.junit.Test;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescrDTOMapperTest {

    @Test
    public void testFromDTO_toDto_fromFxTree_toFxTree() {
        // Prepare
        FxNode fxNode = FxFileUtils.readTree(new File("src/test/java/org/cmdb4j/core/env/dto/template-descr1.json"));
        // Perform
        EnvTemplateDescrDTO dto = FxJsonUtils.treeToValue(EnvTemplateDescrDTO.class, fxNode);
        EnvTemplateDescr descr = EnvTemplateDescrDTOMapper.fromDTO(dto); 
        EnvTemplateDescrDTO dto2 = EnvTemplateDescrDTOMapper.toDTO(descr);
        FxNode fxNode2 = FxJsonUtils.valueToTree(dto2);
        
        EnvTemplateDescr descr3 = EnvTemplateDescrDTOMapper.fromFxTree(fxNode);
        FxNode fxNode3 = EnvTemplateDescrDTOMapper.toFxTree(descr3);
        
        // Post-check
        FxNodeCheckUtils.checkEquals(fxNode, fxNode2);
        FxNodeCheckUtils.checkEquals(fxNode, fxNode3);
    }
}
