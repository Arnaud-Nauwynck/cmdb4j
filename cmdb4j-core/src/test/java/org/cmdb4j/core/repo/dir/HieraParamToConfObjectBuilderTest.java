package org.cmdb4j.core.repo.dir;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.objs.ConfClass;
import org.cmdb4j.core.objs.ConfClassRegistry;
import org.cmdb4j.core.objs.ConfObject;
import org.cmdb4j.core.objs.ConfObjectRegistry;
import org.cmdb4j.core.objs.ConfProp;
import org.cmdb4j.core.objs.ConfProp.ReadonlyValueConfProp;
import org.cmdb4j.core.objs.ConfProp.ResolvedValueConfProp;
import org.cmdb4j.core.objs.ConfPropDef;
import org.cmdb4j.core.repo.dir.propfiles.HieraParamsPropertiesFileDirParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class HieraParamToConfObjectBuilderTest {

    HieraParamToConfObjectBuilder sut;
    
    ConfClassRegistry classRegistry;
    ConfObjectRegistry objRegistry;
    HieraParams objHieraParams;
    
    ConfClass containerType1;
    ConfPropDef<Integer> fieldInt1;
    ConfPropDef<String> fieldString1;
    
    @Before
    public void setup() {
        File rootDir = new File("src/test/dirs/hiera-obj-dir1");
        HieraParamsPropertiesFileDirParser objHieraParser = HieraParamsPropertiesFileDirParser.defaultObjPropParser();
        this.objHieraParams = objHieraParser.loadHieraFromDir(rootDir);
    
        classRegistry = new ConfClassRegistry();
        
        // cf type in src/test/dirs/hiera-obj-dir1/DEV1/obj.properties
        containerType1 = classRegistry.getOrCreateConfClass("containerType1");
        fieldInt1 = containerType1.registerPropertyDef("container_fieldInt1", 
            new ConfPropDef.Builder<Integer>(Integer.class).withReadOnlyProp(true));
        fieldString1 = containerType1.registerPropertyDef("container_fieldString1", 
            new ConfPropDef.Builder<String>(String.class));
            
        
        objRegistry = new ConfObjectRegistry();
        sut = new HieraParamToConfObjectBuilder(classRegistry, objRegistry);
        
    }
    
    
    @Test
    public void testbuild() {
        // Prepare
        // Perform
        sut.build(objHieraParams, null);
        // Post-check
        List<ConfObject> builtObjects = sut.getBuiltObjects();
        Assert.assertNotNull(builtObjects);
        Assert.assertEquals(1, builtObjects.size());
        ConfObject obj1 = builtObjects.get(0);
        Map<ConfPropDef<?>, ConfProp<?>> objProps = obj1.getProperties();
        Assert.assertEquals(2, objProps.size());
        ConfProp<Integer> propInt1 = obj1.getProperty(fieldInt1);
        ConfProp<String> propString1 = obj1.getProperty(fieldString1);

        Assert.assertNotNull(propInt1);
        Assert.assertEquals(Integer.valueOf(123), propInt1.getValue());
        Assert.assertNotNull(propInt1 instanceof ReadonlyValueConfProp);
        
        Assert.assertNotNull(propString1);
        Assert.assertEquals("value2", propString1.getValue());
        Assert.assertNotNull(propString1 instanceof ResolvedValueConfProp);
        Assert.assertNotNull("value2", ((ResolvedValueConfProp<String>)propString1).getValueParamExpr());
    }
}
