package org.cmdb4j.core.env;

import java.io.File;
import java.util.List;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class EnvDirsResourceTreeRepositoryTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsDir");
    protected EnvDirsResourceTreeRepository sut = new EnvDirsResourceTreeRepository(baseEnvsDir, resourceTypeRepository);
    
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
    public void testGetEnvTreeRepo_DEV1() {
        EnvResourceTreeRepository res = sut.getEnvTreeRepo("DEV1");
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertEquals(3, resources.size());
        for(Resource e : resources) {
            Assert.assertTrue(e.getId().startsWith(ResourceId.valueOf("DEV1")));
        }
    }

    @Test
    public void testGetEnvTreeRepo_INT1() {
        EnvResourceTreeRepository res = sut.getEnvTreeRepo("INT1");
        Assert.assertNotNull(res);
        ResourceRepository resRepo = res.getResourceRepository();
        List<Resource> resources = resRepo.findAll();
        Assert.assertTrue(6 <= resources.size());
        for(Resource e : resources) {
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
    public void testGetEnvTreeRepo_cloud1() {
        EnvResourceTreeRepository res = sut.getEnvTreeRepo("cloud/instance1");
        Assert.assertNotNull(res);
    }

}
