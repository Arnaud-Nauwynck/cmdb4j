package org.cmdb4j.core.command;

import java.util.List;

import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.tst.TomcatJvmProcessResourceTypesMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResourceCommandRegistryTest {

    ResourceTypeRepository resourceTypeRepository;
    TomcatJvmProcessResourceTypesMock tcTypesMock;
    
    ResourceCommandRegistry sut;
    
    @Before
    public void setup() {
        resourceTypeRepository = new ResourceTypeRepository();
        tcTypesMock = new TomcatJvmProcessResourceTypesMock(resourceTypeRepository);
        sut = new ResourceCommandRegistry(resourceTypeRepository);
        ResourceCommandsMock.scanRegisterMockMethods(sut, resourceTypeRepository);
    }
    
    @Test
    public void testFindByTypeHierarchy() {
        // Prepare
        // Perform
        List<ResourceCommand> res = sut.findByTypeHierarchy(tcTypesMock.tomcatType);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(7, res.size());
        Assert.assertEquals("Tomcat.start", res.get(0).toString());
        Assert.assertEquals("Tomcat.start_sleep", res.get(1).toString());
        Assert.assertEquals("Tomcat.start_sleep_default", res.get(2).toString());
        Assert.assertEquals("Tomcat.stop", res.get(3).toString());
        Assert.assertEquals("JvmProcess.threadsDump", res.get(4).toString());
        Assert.assertEquals("Process.kill9Process", res.get(5).toString());
        Assert.assertEquals("Process.killProcess", res.get(6).toString());
    }
}
