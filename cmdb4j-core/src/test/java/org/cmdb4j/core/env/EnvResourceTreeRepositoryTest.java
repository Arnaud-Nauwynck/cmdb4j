package org.cmdb4j.core.env;

import java.io.File;
import java.util.List;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;

public class EnvResourceTreeRepositoryTest {

    protected EnvResourceTreeRepository sut;
    
    @Before
    public void setup() {
        File envsDir = new File("src/test/envsDir");
        File targetEnvDir = new File("target/test/DEV1");
        if (! targetEnvDir.exists()) {
            targetEnvDir.mkdirs();
        }
        File srcEnvDir = new File(envsDir, "DEV1");   
        FxNode rawTemplateRootNode = FxFileUtils.readTree(new File(srcEnvDir, "env.yaml"));
        FxNodeFuncRegistry funcRegistry = FxStdFuncs.stdFuncRegistry();
        ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository(); 
        sut = new EnvResourceTreeRepository("DEV1", targetEnvDir, null, rawTemplateRootNode, 
            funcRegistry, resourceTypeRepository);
        sut.init();
    }
    
    @Test
    public void testGetResourceRepository() {
        ResourceRepository res = sut.getResourceRepository();
        List<Resource> resources = res.findAll();
        Assert.assertNotNull(resources);
        Assert.assertEquals(3, resources.size());
    }
    
}
