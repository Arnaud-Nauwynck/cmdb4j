package org.cmdb4j.core.repo.json.reflect;

import java.io.File;
import java.util.Collection;

import org.cmdb4j.core.model.reflect.ResourceFieldDef;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class ResourceTypeContributionTreeParserTest {

    ResourceTypeRepository target = new ResourceTypeRepository();
    ResourceTypeContributionTreeParser sut = new ResourceTypeContributionTreeParser(target, true);
    
    @Test
    public void testAddParseContributions() {
        // Prepare
        File file = new File("src/test/data/test-ResourceTypeContributions.json");
        // Perform
        sut.addParseContributions(file);
        // Post-check
        ResourceType tomcatType = target.getOrNull("Tomcat");
        Assert.assertNotNull(tomcatType);
        ResourceType tomcatSuperType = tomcatType.getSuperType();
        Assert.assertNotNull(tomcatSuperType);
        Assert.assertEquals("Webserver", tomcatSuperType.getName());
        ImmutableList<ResourceType> tomcatSuperInterfaces = tomcatType.getSuperInterfaces();
        Assert.assertNotNull(tomcatSuperInterfaces);
        Assert.assertEquals(2, tomcatSuperInterfaces.size());
        Assert.assertEquals("TomcatWebAppManager", tomcatSuperInterfaces.get(0).getName());
        Assert.assertEquals("SSLKeyStoreSupport", tomcatSuperInterfaces.get(1).getName());
        Collection<ResourceFieldDef> fields = tomcatType.fields();
        Assert.assertEquals(3, fields.size());
    }
    
}
