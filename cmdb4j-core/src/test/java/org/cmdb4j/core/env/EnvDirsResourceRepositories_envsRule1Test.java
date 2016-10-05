package org.cmdb4j.core.env;

import java.io.File;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import fr.an.fxtree.format.yaml.FxYamlUtils;
import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.FxNode;

public class EnvDirsResourceRepositories_envsRule1Test {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsRule1Dir");
    protected EnvDirsResourceRepositories sut = new EnvDirsResourceRepositories(baseEnvsDir, resourceTypeRepository, FxStdFuncs.stdFuncRegistry());


    @Test
    public void testGetEnvRepo_test1() {
        // Prepare
        // Perform
        EnvResourceRepository res = sut.getEnvRepo("test1");
        // Post-check
        ResourceRepository resRepo = res.getResourceRepository();
        Map<ResourceId,Resource> resources = resRepo.findAllAsMap();
        // Assert.assertEquals(, resources.size());
        
        // compare with expected implicit resources from rules..
        FxNode expectedNodes = FxYamlUtils.readTree(new File(baseEnvsDir, res.getEnvName() + "/env.implicit-expected.txt"));
        FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(expectedNodes, (expectedId, expectedTypeName, expectedObjData) -> {
			ResourceId expectedResourceId = ResourceId.valueOf(expectedId);
			ResourceType expectedType = resourceTypeRepository.getOrCreateType(expectedTypeName);
			
			Resource resource = resources.get(expectedResourceId);
			Assert.assertNotNull(resource);
			Assert.assertEquals(expectedType, resource.getType());
			// TODO compare content
			// FxNodeValueUtils.
		});
        
    }
    

}

