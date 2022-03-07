package org.cmdb4j.core.env;

import java.io.File;
import java.util.List;

import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.EnvsInputDirsConfig;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import lombok.val;

public class CmdbEnvTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsDir");
    protected EnvsInputDirsConfig envsInputDirsConfig = EnvsInputDirsConfig.createDefaultForDir(baseEnvsDir);
    
    protected CmdbEnvRepository sut = new CmdbEnvRepository(
    		new CmdbInputsSource(baseEnvsDir, envsInputDirsConfig), // 
    		null, // dynStore
    		resourceTypeRepository, FxStdFuncs.stdFuncRegistry(),
    		null); // envValueDecrypter
          
    @Test
    public void testGetResourceRepository_findById_getRequireResourceIds_transitiveRequireResources() {
        val dev1 = sut.getEnv("DEV1");
    	ResourceRepository res = dev1.getResourceRepository();
        List<Resource> resources = res.findAll();
        Assert.assertNotNull(resources);
        Assert.assertEquals(6, resources.size());
        
        ResourceId jdk8Id = ResourceId.valueOf("localhost/install-jdk8");
        ResourceId truststoreId = ResourceId.valueOf("localhost/install-truststore-ssl");
        ResourceId tomcat8HomeId = ResourceId.valueOf("localhost/tomcat8-home");
        ResourceId tomcat1Id = ResourceId.valueOf("localhost/tomcat1");
        ResourceId tomcat2Id = ResourceId.valueOf("localhost/tomcat2");
        
        // Perform
        Resource jdk8 = resources.get(1);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(), jdk8.getRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat8HomeId, tomcat1Id), jdk8.getInvRequiredFromResources().keySet());

        // Perform
        Resource truststore = res.findById(truststoreId);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(), truststore.getRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat8HomeId, tomcat1Id), truststore.getInvRequiredFromResources().keySet());
        Assert.assertEquals(ImmutableSet.of(), truststore.getTransitiveRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat8HomeId, tomcat1Id, tomcat2Id), truststore.getTransitiveInvRequireFromResources().keySet());
        
        // Perform
        Resource tomcat8Home = res.findById(tomcat8HomeId);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(jdk8Id, truststoreId), tomcat8Home.getRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat2Id), tomcat8Home.getInvRequiredFromResources().keySet());
        Assert.assertEquals(ImmutableSet.of(jdk8Id, truststoreId), tomcat8Home.getTransitiveRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat2Id), tomcat8Home.getTransitiveInvRequireFromResources().keySet());

        // Perform
        Resource tomcat1 = res.findById(tomcat1Id);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(jdk8Id, truststoreId), tomcat1.getRequireResourceIds());
        Assert.assertEquals(ImmutableSet.of(), tomcat1.getInvRequiredFromResources().keySet());
        Assert.assertEquals(ImmutableSet.of(jdk8Id, truststoreId), tomcat1.getTransitiveRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(), tomcat1.getTransitiveInvRequireFromResources().keySet());

        // Perform
        Resource tomcat2 = res.findById(tomcat2Id);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tomcat8HomeId), tomcat2.getRequireResourceIds());
        Assert.assertEquals(ImmutableSet.of(), tomcat2.getInvRequiredFromResources().keySet());
        Assert.assertEquals(ImmutableSet.of(tomcat8HomeId, jdk8Id, truststoreId), tomcat2.getTransitiveRequireResources().keySet());
        Assert.assertEquals(ImmutableSet.of(), tomcat1.getTransitiveInvRequireFromResources().keySet());
        
    }
    
}
