package org.cmdb4j.core.model.reflect;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.cmdb4j.core.model.reflect.ResourceFieldDef;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeContributio
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import fr.an.dynadapter.alt.IAdapterAlternativeFactory;
import fr.an.dynadapter.alt.IAdapterAlternativesManager;
import fr.an.dynadapter.alt.ItfId;

public class ResourceTypeContributionTreeParserTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypeContributionTreeParserTest.class);
    
    protected static interface ITstTomcatStopSupport {
        public void stop();
    }
    protected static class DummyTstTomcatStopAdapter implements ITstTomcatStopSupport {
        protected Object adaptableObject;
        public DummyTstTomcatStopAdapter(Object adaptableObject) {
            this.adaptableObject = adaptableObject;
        }
        public void stop() {
            LOG.debug("dummy stop() ..do nothing");
        }
    }
    
    protected IAdapterAlternativeFactory dummyTomcatStopSupportAdapterFactory = new IAdapterAlternativeFactory() {
        @Override
        public Object getAdapter(Object adaptableObject, ItfId<?> interfaceId) {
            return new DummyTstTomcatStopAdapter(adaptableObject);
        }
        @Override
        public ItfId<?>[] getInterfaceIds() {
            return new ItfId[] { ItfId.of(ITstTomcatStopSupport.class) };
        }
        @Override
        public String getAlternativeName() {
            return "";
        }
    };

    public static class DummyTstTomcatStopSupportAdapterFactory implements IAdapterAlternativeFactory {
        /* called by introspection! */
        public DummyTstTomcatStopSupportAdapterFactory() {
        }
        @Override
        public Object getAdapter(Object adaptableObject, ItfId<?> interfaceId) {
            return new DummyTstTomcatStopAdapter(adaptableObject);
        }
        @Override
        public ItfId<?>[] getInterfaceIds() {
            return new ItfId[] { ItfId.of(ITstTomcatStopSupport.class) };
        }
        @Override
        public String getAlternativeName() {
            return "impl2";
        }
    };
    
    
    protected Function<String,IAdapterAlternativeFactory> afLookup = (x) -> {
        if ("dummyTomcatStopSupportAdapterFactory".equals(x)) {
            return dummyTomcatStopSupportAdapterFactory;
        }
        return null;
    };
    
    protected ResourceTypeRepository target = new ResourceTypeRepository();
    protected ResourceTypeContributionTreeParser sut = new ResourceTypeContributionTreeParser(target, true, afLookup);
    
    protected ResourceTypeRepository targetNotStrict = new ResourceTypeRepository();
    protected ResourceTypeContributionTreeParser sutNotStrict = new ResourceTypeContributionTreeParser(targetNotStrict, false, afLookup);
    
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
        
        IAdapterAlternativesManager<ResourceType> am = target.getAdapterManager();
        ItfId<?> tomcatStopSupportItfId = ItfId.of(ITstTomcatStopSupport.class);
        Set<String> stopAlternatives = am.getAdapterAlternatives(tomcatType, tomcatStopSupportItfId);
        Assert.assertEquals(2, stopAlternatives.size());
    }
    
    @Test
    public void testAddParseContributions_err() {
        // Prepare
        File file = new File("src/test/data/test-ResourceTypeContributions-errClassNotFound.json");
        // Perform
        LOG.info("testing errors with not-strict parser => WARN on next log line");
        sutNotStrict.addParseContributions(file);
        try {
            sut.addParseContributions(file);
            Assert.fail();
        } catch(IllegalArgumentException ex) {
            String err = ex.getMessage();
            Assert.assertTrue(err.startsWith("Failed to parse decl 'adapterFactoryDecl'"));
        }
        // Post-check
    }
}
