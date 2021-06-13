package org.cmdb4j.core.dtomapper.env;

import java.io.File;

import org.cmdb4j.core.dto.env.EnvTemplateDescrDTO;
import org.cmdb4j.core.env.EnvTemplateDescr;
import org.junit.Test;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;

public class EnvTemplateDescrDTOMapperTest {

    protected EnvTemplateDescrDTOMapper sut = new EnvTemplateDescrDTOMapper();
    
    @Test
    public void testFromDTO_toDto_fromFxTree_toFxTree() {
        // Prepare
    	String filePath = "src/test/java/org/cmdb4j/core/env/impl/template-descr1.json";
    	File file = new File(filePath);
    	FxSourceLoc source = new FxSourceLoc("", filePath);
		FxNode fxNode = FxFileUtils.readTree(file, source);
        // Perform
        EnvTemplateDescrDTO dto = FxJsonUtils.treeToValue(EnvTemplateDescrDTO.class, fxNode);
        EnvTemplateDescr descr = sut.fromDTO(dto); 
        EnvTemplateDescrDTO dto2 = sut.toDTO(descr);
        FxNode fxNode2 = FxJsonUtils.valueToTree(dto2);
        
        EnvTemplateDescr descr3 = sut.fromFxTree("test", fxNode);
        FxNode fxNode3 = sut.toFxTree(descr3);
        
        // Post-check
        FxNodeCheckUtils.checkEquals(fxNode, fxNode2);
        FxNodeCheckUtils.checkEquals(fxNode, fxNode3);
    }
}
