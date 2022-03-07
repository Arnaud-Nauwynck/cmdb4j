package org.cmdb4j.core.env;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.cmdb4j.core.dto.env.EnvInfoDTO;
import org.cmdb4j.core.dto.env.LightweightEnvInfoDTO;
import org.cmdb4j.core.dtomapper.env.EnvInfoDTOMapper;
import org.cmdb4j.core.dtomapper.env.LightweightEnvInfoDTOMapper;
import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.EnvsInputDirsConfig;
import org.cmdb4j.core.env.template.EnvTemplateDescr;
import org.cmdb4j.core.env.template.EnvTemplateInstanceParameters;
import org.cmdb4j.core.env.template.EnvTemplateParamDescr;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;
import lombok.val;

public class CmdbEnvRepositoryTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsDir");
    protected EnvsInputDirsConfig envsInputDirsConfig = EnvsInputDirsConfig.createDefaultForDir(baseEnvsDir);
    
    protected CmdbEnvRepository sut = new CmdbEnvRepository(
    		new CmdbInputsSource(baseEnvsDir, envsInputDirsConfig), // 
    		null, // dynStore
    		resourceTypeRepository, FxStdFuncs.stdFuncRegistry(),
    		null); // envValueDecrypter
    
    @Test
    public void testGetter() {
        Assert.assertSame(resourceTypeRepository, sut.getResourceTypeRepository());
        Assert.assertNotNull(sut.getFuncRegistry());
        Assert.assertNotNull(sut.getGlobalResources());
    }
    
    @Test
    public void testPurge() {
        sut.purge();
    }

    @Test
    public void testListEnvs() {
        val res = sut.listEnvs();
        Assert.assertTrue(res.containsAll(ImmutableList.of("INT1", "DEV1", "cloud/instance1", "cloud/instance2")));
    }

    @Test
    public void testGetEnvRepo_DEV1() {
        // Prepare
        // Perform
        val res = sut.getEnv("DEV1");
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(6, resources.size());
        for (Resource e : resources) {
            Assert.assertTrue(e.getId().startsWith(ResourceId.valueOf("DEV1")));
        }
        Resource localhost = resRepo.findById(ResourceId.valueOf("DEV1/localhost"));
        Assert.assertNotNull(localhost);
        Resource locJdk8 = resRepo.findById(ResourceId.valueOf("DEV1/localhost/install-jdk8"));
        Assert.assertNotNull(locJdk8);
        Resource locTrustFile = resRepo.findById(ResourceId.valueOf("DEV1/localhost/install-truststore-ssl"));
        Assert.assertNotNull(locTrustFile);
        Resource locTomcat8Home = resRepo.findById(ResourceId.valueOf("DEV1/localhost/tomcat8-home"));
        Assert.assertNotNull(locTomcat8Home);
        Resource locTomcat1 = resRepo.findById(ResourceId.valueOf("DEV1/localhost/tomcat1"));
        Assert.assertNotNull(locTomcat1);
        Resource tomcat2 = resRepo.findById(ResourceId.valueOf("DEV1/localhost/tomcat2"));
        Assert.assertNotNull(tomcat2);
    }
    
    @Test
    public void testResourceIdToEnvName() {
        Assert.assertEquals("DEV1", sut.resourceIdToEnvName(ResourceId.valueOf("DEV1/localhost")));
        Assert.assertEquals("DEV1", sut.resourceIdToEnvName(ResourceId.valueOf("DEV1/non-existing-resource")));
        Assert.assertNull(sut.resourceIdToEnvName(ResourceId.valueOf("!non-existing-env")));
        Assert.assertEquals("cloud/instance1", sut.resourceIdToEnvName(ResourceId.valueOf("cloud/instance1/non-existing-resouce")));
        Assert.assertNull(sut.resourceIdToEnvName(ResourceId.valueOf("cloud/non-existing-instance")));
        Assert.assertNull(sut.resourceIdToEnvName(ResourceId.valueOf("cloud")));
        Assert.assertNull(sut.resourceIdToEnvName(null));
        Assert.assertNull(sut.resourceIdToEnvName(ResourceId.valueOf()));
        Assert.assertNull(sut.resourceIdToEnvName(ResourceId.valueOf("")));
    }
    
    @Test
    public void testGetResourceById() {
        Assert.assertNotNull(sut.getResourceById(ResourceId.valueOf("DEV1/localhost")));
        Assert.assertNull(sut.getResourceById(ResourceId.valueOf("cloud/non-existing-instance/res")));
    }
    
    @Test
    public void testGetEnvRepo_null() {
        Assert.assertNull(sut.getEnv("Templates"));
        Assert.assertNull(sut.getEnv("cloud"));
        Assert.assertNull(sut.getEnv("Default"));
        Assert.assertNull(sut.getEnv("non-existing-env"));
        Assert.assertNull(sut.getEnv("cloud/non-existing-env"));
    }
    
    @Test
    public void testGetEnvRepo_INT1() {
        val res = sut.getEnv("INT1");
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertTrue(6 <= resources.size());
        for (Resource e : resources) {
            Assert.assertTrue(e.getId().startsWith(ResourceId.valueOf("INT1")) || e.getId().startsWith(ResourceId.valueOf("anotherSharedEnv")));
        }
        Resource host1 = resRepo.findById(ResourceId.valueOf("INT1/host1"));
        Assert.assertNotNull(host1);
        Resource host1Tc1 = resRepo.findById(ResourceId.valueOf("INT1/host1/tomcat1"));
        Assert.assertNotNull(host1Tc1);

        Resource host2 = resRepo.findById(ResourceId.valueOf("INT1/host2"));
        Assert.assertNotNull(host2);
        Resource host2Tc1 = resRepo.findById(ResourceId.valueOf("INT1/host2/tomcat1"));
        Assert.assertNotNull(host2Tc1);
        Resource host2Tc2 = resRepo.findById(ResourceId.valueOf("INT1/host2/tomcat2"));
        Assert.assertNotNull(host2Tc2);

        Resource host3 = resRepo.findById(ResourceId.valueOf("INT1/host3"));
        Assert.assertNotNull(host3);
        Resource host3Tc1 = resRepo.findById(ResourceId.valueOf("INT1/host3/tomcat1"));
        Assert.assertNotNull(host3Tc1);
    }

    @Test
    public void testGetEnvRepo_cloud_instance1() {
        // Prepare
        // Perform
        val res = sut.getEnv("cloud/instance1");
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(150, resources.size()); // 50 * 3(host + tomcat1 + tomcat2)
        
        LightweightEnvInfoDTO resLightweigthDTO = new LightweightEnvInfoDTOMapper().toDTO(res); 
        Assert.assertNotNull(resLightweigthDTO);
        EnvInfoDTO resDTO = new EnvInfoDTOMapper().toDTO(res); 
        Assert.assertNotNull(resDTO);
        Assert.assertEquals(resources.size(), resDTO.getResources().size());
    }

    @Test
    public void testGetEnvRepo_cloud_instance2() {
        // Prepare
        // Perform
        val res = sut.getEnv("cloud/instance2");
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(30, resources.size()); // 10 * 3(host + tomcat1 + tomcat2)
    }

    
    @Test
    public void testGetEnvTemplateDescr() {
        // Prepare
        // Perform
        EnvTemplateDescr res = sut.getEnvTemplateDescr("template1");
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals("template1", res.getName());
        Assert.assertEquals("template 1", res.getDisplayName());
        Assert.assertEquals("a template for test", res.getComment());
         List<EnvTemplateParamDescr> paramDescriptions = res.getParamDescriptions();
        Assert.assertEquals(1, paramDescriptions.size());
        EnvTemplateParamDescr param = res.findParamDescriptionByName("Topology_NumberOfVirtualHost");
        Assert.assertEquals("number of virtual hosts to provision", param.getComment());
        FxNodeCheckUtils.checkIntEquals(2, param.getDefaultValue());
        Assert.assertEquals("number of virtual host", param.getDisplayName());
        Map<String, FxNode> paramExtraProperties = param.getExtraProperties();
        Assert.assertEquals(2, paramExtraProperties.size());
        FxNodeCheckUtils.checkIntEquals(1, paramExtraProperties.get("minValue"));
        FxNodeCheckUtils.checkIntEquals(10, paramExtraProperties.get("maxValue"));
        
        Map<String, FxNode> extraProperties = res.getExtraProperties();
        Assert.assertEquals(4,  extraProperties.size());
        FxNodeCheckUtils.checkTextEquals("a temporary env for test...", extraProperties.get("detailedDescription"));
        FxNodeCheckUtils.checkDoubleEquals(1.2, extraProperties.get("costPerDay"), 1e-6);
        Assert.assertTrue(extraProperties.get("nestYamlProperty").isObject());
    }
    
    @Test
    public void testGetEnvTemplateDescr_null() {
        EnvTemplateDescr res = sut.getEnvTemplateDescr("template-not-existing");
        Assert.assertNull(res);
    }
    
    @Test
    public void testCreateEnvFromTemplate() throws IOException {
        // Prepare
        String envName = "cloud/test-Create1";
        File testEnvDir = new File(baseEnvsDir, envName);
        if (testEnvDir.exists()) {
            FileUtils.deleteDirectory(testEnvDir);
        }
        val templateParameters = new HashMap<String, FxNode>();
        int testNumberOfVirtualHost = 2;
        val doc = FxMemRootDocument.newInMem();
        templateParameters.put("Topology_NumberOfVirtualHost", doc.contentWriter().add(testNumberOfVirtualHost, null));
        val instanceParams = new EnvTemplateInstanceParameters(envName, "template1", templateParameters, null);
        // Perform
        val res = sut.createEnvFromTemplate(instanceParams);
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resourceRepository = res.getResourceRepository();
        List<Resource> resResources = resourceRepository.findAll();
        Assert.assertEquals(testNumberOfVirtualHost*3, resResources.size());
        // cleanup.. FileUtils.deleteDirectory(testEnvDir);
    }

}
