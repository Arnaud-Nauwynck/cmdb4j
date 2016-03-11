package org.cmdb4j.core.model.reflect;

import java.util.Set;

import org.cmdb4j.core.tst.TstResourceTypes1;
import org.cmdb4j.core.tst.TstResourceTypes2;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class ResourceTypeRepositoryTest {

    protected ResourceTypeRepository sut = new ResourceTypeRepository();
    
    @Test
    public void testSubTypeHierarchyOf() {
        // Prepare
        TstResourceTypes1 tstTypes = new TstResourceTypes1(sut);
        // Perform
        Set<ResourceType> res = sut.subTypeHierarchyOf(tstTypes.serverType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.serverType, 
            tstTypes.webserverType, tstTypes.tomcatType, 
            tstTypes.dirFileServerType), res);

        // Perform
        res = sut.subTypeHierarchyOf(tstTypes.webserverType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.webserverType, tstTypes.tomcatType), res);

        // Perform
        res = sut.subTypeHierarchyOf(tstTypes.tomcatType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.tomcatType, tstTypes.tomcatType), res);
        
        // now dynamically add jetty, apache (extends webserver)
        // Prepare
        TstResourceTypes2 tstTypes2 = new TstResourceTypes2(sut);
        
        // Perform
        res = sut.subTypeHierarchyOf(tstTypes.serverType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.serverType, 
            tstTypes.webserverType, tstTypes.tomcatType, 
            tstTypes2.jettyType, tstTypes2.apacheType, // <= now added 
            tstTypes.dirFileServerType), res);

        // Perform
        res = sut.subTypeHierarchyOf(tstTypes.webserverType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.webserverType, tstTypes.tomcatType, 
            tstTypes2.jettyType, tstTypes2.apacheType // <= now added
            ), res);

        // Perform
        res = sut.subTypeHierarchyOf(tstTypes.tomcatType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.tomcatType, tstTypes.tomcatType), res);
    }

    @Test
    public void testSuperTypeHierarchyOf() {
        // Prepare
        TstResourceTypes1 tstTypes = new TstResourceTypes1(sut);
        // Perform
        Set<ResourceType> res = sut.superTypeHierarchyOf(tstTypes.tomcatType);
        // Post-check
        Assert.assertEquals(ImmutableSet.of(tstTypes.tomcatType, tstTypes.webserverType, tstTypes.serverType), res);
        
        // create jetty that does no inherit from "webserver" ... then add superType link
        ResourceType jettyType = sut.getOrCreateType("jetty");
        
        res = sut.superTypeHierarchyOf(jettyType);
        Assert.assertEquals(ImmutableSet.of(jettyType), res);
        
        // now dynamically add jetty->webserver + apache (extends webserver)
        // Prepare
        TstResourceTypes2 tstTypes2 = new TstResourceTypes2(sut);
        Assert.assertSame(jettyType, tstTypes2.jettyType);
        
        res = sut.superTypeHierarchyOf(jettyType);
        Assert.assertEquals(ImmutableSet.of(jettyType, tstTypes.webserverType, tstTypes.serverType), res);
    }
    
}
