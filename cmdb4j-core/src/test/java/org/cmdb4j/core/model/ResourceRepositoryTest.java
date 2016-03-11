package org.cmdb4j.core.model;

import java.util.List;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.tst.TstResourceTypes1;
import org.cmdb4j.core.tst.TstResourceTypes2;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ResourceRepositoryTest {

    protected ResourceTypeRepository typeRepo = new ResourceTypeRepository(); 

    protected TstResourceTypes1 types1 = new TstResourceTypes1(typeRepo);
    protected ResourceType serverType = types1.serverType;
    protected ResourceType tomcatType = types1.tomcatType;
    protected ResourceType webserverType = types1.webserverType;
    protected ResourceType dirServerType = types1.dirFileServerType;
    protected ResourceId tc1Id = ResourceId.valueOf("DEV1/host1/tc1");
    protected Resource tc1 = new Resource(tc1Id, tomcatType, null);
    protected Resource tc2 = new Resource(ResourceId.valueOf("DEV1/host1/tc2"), tomcatType, null);
    protected Resource dirServer1 = new Resource(ResourceId.valueOf("DEV1/host1/dir1"), dirServerType, null);
    
    // protected TstResourceTypes2 types2 = new TstResourceTypes2(typeRepo);
    protected ResourceType jettyType = typeRepo.getOrCreateType("jetty"); // does not extends webserver yet... cf new Types2
    protected Resource jetty1 = new Resource(ResourceId.valueOf("DEV1/host1/jetty1"), jettyType, null);
    protected Resource tc3 = new Resource(ResourceId.valueOf("DEV1/host1/tc3"), tomcatType, null);
    
    protected ResourceRepository sut = new ResourceRepository(typeRepo);
    
    @Before
    public void setup() {
        addTstResources1();
    }
    
    @Test
    public void testFindById() {
        Assert.assertSame(tc1, sut.findById(tc1Id));
        sut.remove(tc1);
        Assert.assertNull(sut.findById(tc1Id));
    }
     
    @Test
    public void testGetById() {
        Assert.assertSame(tc1, sut.getById(tc1Id));
        sut.remove(tc1);
        try {
            sut.getById(tc1Id);
            Assert.fail();
        } catch(CmdbObjectNotFoundException ex) {
            // ok
        }
    }
    
    @Test
    public void testRemove() {
        sut.remove(tc1);
        sut.remove(tc2.getId());
        sut.remove(ResourceId.valueOf("not-found-id"));
        sut.remove(tc2); // already removed
        // Post-check
        Assert.assertNull(sut.findById(tc1Id));
        Assert.assertNull(sut.findById(tc2.getId()));
    }
    
    @Test
    public void testFindAll() {
        List<Resource> res = sut.findAll();
        Assert.assertEquals(ImmutableList.of(tc1, tc2, dirServer1), res);

        // dynamic add
        addTstResources2();
        res = sut.findAll();
        Assert.assertEquals(ImmutableList.of(tc1, tc2, dirServer1, jetty1, tc3), res);
        
        // dynamic remove
        sut.remove(tc2);
        res = sut.findAll();
        Assert.assertEquals(ImmutableList.of(tc1, dirServer1, jetty1, tc3), res);        
    }

    @Test
    public void testFindAllIds() {
        List<ResourceId> res = sut.findAllIds();
        Assert.assertEquals(3, res.size());
    }
    
    @Test
    public void testFindByCrit() {
        List<Resource> res = sut.findByCrit(x -> x.getId().toString().endsWith("tc2"));
        Assert.assertEquals(ImmutableList.of(tc2), res);
    }
    
    @Test
    public void testFindFirstByCrit() {
        Resource res = sut.findFirstByCrit(x -> x.getId().toString().startsWith("DEV1/host1/tc"));
        Assert.assertSame(tc1, res);
    }
    
    @Test
    public void testFindByExactTypeAndCrit() {
        List<Resource> res = sut.findByExactTypeAndCrit(tomcatType, x -> x.getId().toString().startsWith("DEV1/host1/"));
        Assert.assertEquals(ImmutableList.of(tc1, tc2), res);
    }
    
    @Test
    public void testFindFirstByExactTypeAndCrit() {
        Resource res = sut.findFirstByExactTypeAndCrit(tomcatType, x -> x.getId().toString().endsWith("2"));
        Assert.assertSame(tc2, res);
    }
    
    
    @Test
    public void testFindByExactType() {
        Assert.assertTrue(sut.findByExactType(jettyType).isEmpty());
        List<Resource> res = sut.findByExactType(tomcatType);
        Assert.assertEquals(ImmutableList.of(tc1, tc2), res);

        // dynamic add
        addTstResources2();
        res = sut.findByExactType(tomcatType);
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3), res);
        Assert.assertEquals(ImmutableList.of(jetty1), sut.findByExactType(jettyType));
        
        // dynamic remove
        sut.remove(tc2);
        res = sut.findByExactType(tomcatType);
        Assert.assertEquals(ImmutableList.of(tc1, tc3), res);        
    }
    
    @Test
    public void testFindBySubType() {
        Assert.assertTrue(sut.findBySubType(jettyType).isEmpty());
        Assert.assertEquals(ImmutableList.of(tc1, tc2), sut.findBySubType(tomcatType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2), sut.findBySubType(webserverType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2, dirServer1), sut.findBySubType(serverType));

        // dynamic add   ("jetty" type does not extends "webserver" yet)
        addTstResources2();
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3), sut.findBySubType(tomcatType));
        Assert.assertEquals(ImmutableList.of(jetty1), sut.findBySubType(jettyType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3), sut.findBySubType(webserverType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3, dirServer1), sut.findBySubType(serverType));

        // now add extends
        new TstResourceTypes2(typeRepo);
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3), sut.findBySubType(tomcatType));
        Assert.assertEquals(ImmutableList.of(jetty1), sut.findBySubType(jettyType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3, jetty1), sut.findBySubType(webserverType));
        Assert.assertEquals(ImmutableList.of(tc1, tc2, tc3, jetty1, dirServer1), sut.findBySubType(serverType));

        // dynamic remove
        sut.remove(tc2);
        Assert.assertEquals(ImmutableList.of(tc1, tc3), sut.findBySubType(tomcatType));
        Assert.assertEquals(ImmutableList.of(jetty1), sut.findBySubType(jettyType));
        Assert.assertEquals(ImmutableList.of(tc1, tc3, jetty1), sut.findBySubType(webserverType));
        Assert.assertEquals(ImmutableList.of(tc1, tc3, jetty1, dirServer1), sut.findBySubType(serverType));
    }    
    
    @Test
    public void testFindBySubTypeAndCrit() {
        List<Resource> res = sut.findBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("1"));
        Assert.assertEquals(ImmutableList.of(tc1), res);
        addTstResources2();
        res = sut.findBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("1"));
        Assert.assertEquals(ImmutableList.of(tc1), res);
        
        // add extends jetty->webserver
        new TstResourceTypes2(typeRepo);
        res = sut.findBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("1"));
        Assert.assertEquals(ImmutableList.of(tc1, jetty1), res);
    }

    @Test
    public void testFindFirstBySubTypeAndCrit() {
        Resource res = sut.findFirstBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("1"));
        Assert.assertSame(tc1, res);
        addTstResources2();
        res = sut.findFirstBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("2"));
        Assert.assertSame(tc2, res);
        res = sut.findFirstBySubTypeAndCrit(webserverType, x -> x.getId().toString().contains("jetty"));
        Assert.assertNull(res);
        
        // add extends jetty->webserver
        new TstResourceTypes2(typeRepo);
        res = sut.findFirstBySubTypeAndCrit(webserverType, x -> x.getId().toString().endsWith("1"));
        Assert.assertSame(tc1, res);
        res = sut.findFirstBySubTypeAndCrit(webserverType, x -> x.getId().toString().contains("jetty"));
        Assert.assertSame(jetty1, res);
    }
    
        
    private void addTstResources1() {
        sut.add(tc1);
        sut.add(tc2);
        sut.add(dirServer1);
    }
    
    private void addTstResources2() {
        sut.add(jetty1);
        sut.add(tc3);
    }

    @Test
    public void testClose() {
        sut.close();
        Assert.assertTrue(sut.findAll().isEmpty());
    }
    
    
}
