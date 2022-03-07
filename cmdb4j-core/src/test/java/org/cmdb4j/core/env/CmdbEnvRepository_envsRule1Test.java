package org.cmdb4j.core.env;

import java.io.File;
import java.util.Map;

import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.EnvsInputDirsConfig;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import fr.an.fxtree.format.yaml.FxYamlUtils;
import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.FxNode;
import lombok.val;

public class CmdbEnvRepository_envsRule1Test {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsRule1Dir");
    protected EnvsInputDirsConfig envsInputDirsConfig = EnvsInputDirsConfig.createDefaultForDir(baseEnvsDir);
    
    protected CmdbEnvRepository sut = new CmdbEnvRepository(
    		new CmdbInputsSource(baseEnvsDir, envsInputDirsConfig), // 
    		null, // dynStore
    		resourceTypeRepository, FxStdFuncs.stdFuncRegistry(),
    		null); // envValueDecrypter

    @Test
    public void testGetEnvRepo_test1() {
        // Prepare
        // Perform
    	String envName = "test1";
        val res = sut.getEnv(envName);
        // Post-check
        ResourceRepository resRepo = res.getResourceRepository();
        final Map<ResourceId,Resource> resources = resRepo.findAllAsMap();
        // Assert.assertEquals(, resources.size());
        
        // compare with expected implicit resources from rules..
        String filePath = envName + "/env.implicit-expected.txt";
		FxNode expectedNodes = FxYamlUtils.readTree(new File(baseEnvsDir, filePath), new FxSourceLoc("", filePath));
        FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(expectedNodes, (expectedId, expectedTypeName, expectedObjData, loc) -> {
			ResourceId expectedResourceId = ResourceId.valueOf(envName + "/" + expectedId);
			ResourceType expectedType = resourceTypeRepository.getOrCreateType(expectedTypeName);
			
			Resource resource = resources.get(expectedResourceId);
			Assert.assertNotNull(resource);
			Assert.assertEquals(expectedType, resource.getType());
			// TODO compare content
			// FxNodeValueUtils.
		});
        
    }
    

}

