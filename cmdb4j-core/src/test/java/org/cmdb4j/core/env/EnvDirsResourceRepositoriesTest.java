package org.cmdb4j.core.env;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.IntNode;
import com.google.common.collect.ImmutableList;

import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;

public class EnvDirsResourceRepositoriesTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsDir");
    protected EnvDirsResourceRepositories sut = new EnvDirsResourceRepositories(baseEnvsDir, resourceTypeRepository, FxStdFuncs.stdFuncRegistry());

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
        List<String> res = sut.listEnvs();
        Assert.assertEquals(ImmutableList.of("INT1", "DEV1", "cloud/instance1", "cloud/instance2"), res);
    }

    @Test
    public void testGetEnvRepo_DEV1() {
        // Prepare
        // Perform
        EnvResourceRepository res = sut.getEnvRepo("DEV1");
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
        Assert.assertNull(sut.getEnvRepo(null));
        Assert.assertNull(sut.getEnvRepo(""));
        Assert.assertNull(sut.getEnvRepo("Templates"));
        Assert.assertNull(sut.getEnvRepo("cloud"));
        Assert.assertNull(sut.getEnvRepo("Default"));
        Assert.assertNull(sut.getEnvRepo("non-existing-env"));
        Assert.assertNull(sut.getEnvRepo("clout/non-existing-env"));
    }
    
    @Test
    public void testGetEnvRepo_INT1() {
        EnvResourceRepository res = sut.getEnvRepo("INT1");
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
        EnvResourceRepository res = sut.getEnvRepo("cloud/instance1");
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(150, resources.size()); // 50 * 3(host + tomcat1 + tomcat2)
    }

    @Test
    public void testGetEnvRepo_cloud_instance2() {
        // Prepare
        // Perform
        EnvResourceRepository res = sut.getEnvRepo("cloud/instance2");
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(30, resources.size()); // 10 * 3(host + tomcat1 + tomcat2)
    }

    
    @Test
    public void testListEnvTemplates() {
        List<String> res = sut.listEnvTemplates();
        Assert.assertEquals(ImmutableList.of("template1"), res);
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
        String envName = "test-Create1";
        File testEnvDir = new File(baseEnvsDir, "cloud/" + envName);
        if (testEnvDir.exists()) {
            FileUtils.deleteDirectory(testEnvDir);
        }
        EnvTemplateInstanceParametersDTO instanceParams = new EnvTemplateInstanceParametersDTO();
        instanceParams.setSourceTemplateName("template1");
        int testNumberOfVirtualHost = 2;
        instanceParams.putParameter("Topology_NumberOfVirtualHost", new IntNode(testNumberOfVirtualHost));
        // Perform
        EnvResourceRepository res = sut.createEnvFromTemplate(envName, instanceParams);
        // Post-check
        Assert.assertNotNull(res);
        ResourceRepository resourceRepository = res.getResourceRepository();
        List<Resource> resResources = resourceRepository.findAll();
        Assert.assertEquals(testNumberOfVirtualHost*3, resResources.size());
        // cleanup.. FileUtils.deleteDirectory(testEnvDir);
    }

}

