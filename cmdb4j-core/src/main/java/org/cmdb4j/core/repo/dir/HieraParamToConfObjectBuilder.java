package org.cmdb4j.core.repo.dir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.hieraparams.HieraParams;
import org.cmdb4j.core.hieraparams.HieraParams.HieraPathParamsVisitor;
import org.cmdb4j.core.hieraparams.HieraPath;
import org.cmdb4j.core.objs.ConfClass;
import org.cmdb4j.core.objs.ConfClassRegistry;
import org.cmdb4j.core.objs.ConfObject;
import org.cmdb4j.core.objs.ConfObjectRegistry;
import org.cmdb4j.core.objs.ConfProp;
import org.cmdb4j.core.objs.ConfProp.ReadonlyValueConfProp;
import org.cmdb4j.core.objs.ConfProp.ResolvedValueConfProp;
import org.cmdb4j.core.objs.ConfPropDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * factory class to build ConfObject using HieraParams
 * 
 * Internally, it scans paths containing property "type"
 * resolve the ConfClass and initialize all class properties using class propertyDefs
 */
public class HieraParamToConfObjectBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(HieraParamToConfObjectBuilder.class);
    
    private String propTypeName = "type";

    private ConfClassRegistry classRegistry;
    private ConfObjectRegistry objRegistry;
    
    private List<ConfObject> builtObjects = new ArrayList<ConfObject>();
    
    // ------------------------------------------------------------------------

    public HieraParamToConfObjectBuilder(ConfClassRegistry classRegistry, ConfObjectRegistry objRegistry) {
        this.classRegistry = classRegistry;
        this.objRegistry = objRegistry;
    }
    
    // ------------------------------------------------------------------------
    
    public List<ConfObject> getBuiltObjects() {
        return builtObjects;
    }
    
    
    public void build(final HieraParams hieraParams, HieraPath rootScanPath) {
        HieraPathParamsVisitor visitor = new HieraPathParamsVisitor() {
            public void visit(HieraPath path, Map<String, String> resolvedParams) {
                buildConObject(hieraParams, path, resolvedParams);
            }
        }; 
        hieraParams.scanPathWithResolveParams(visitor, rootScanPath);
    }
    

    public void buildConObject(HieraParams hieraParams, HieraPath path, Map<String, String> resolvedParams) {
        String confTypeName = hieraParams.getOverride(path, propTypeName); // no override for type
        if (confTypeName == null) {
            return;
        }
        ConfClass confClass = classRegistry.getConfClassOrNull(confTypeName);
        if (confClass == null) {
            // ignore object or create empty object?
        }
        ConfObject confObject = objRegistry.getObjectOrNull(path);

        Map<ConfPropDef<?>,ConfProp<?>> props;
        if (confObject == null) {
            props = new HashMap<ConfPropDef<?>,ConfProp<?>>();
        } else {
            // when object already exists .. => merge override properties
            props = confObject.getProperties();
        }
        
        // attach all resolved properties, as registered by class 
        boolean validObj = true;
        for (ConfPropDef<Object> propDef : confClass.getPropertyDefValues()) {
            validObj &= buildConfObjProp(props, propDef, resolvedParams);
        }
        
        if (confObject == null && validObj) {
            confObject = new ConfObject(confClass, path, props);
            objRegistry.put(path, confObject);
            builtObjects.add(confObject);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean buildConfObjProp(// ConfObject confObject, 
            Map<ConfPropDef<?>,ConfProp<?>> props,
            ConfPropDef<Object> propDef, 
            Map<String, String> resolvedParams) {
        boolean validProp = true;
        String propName = propDef.getPropertyName();
        String propParamValueExpr = resolvedParams.get(propName);
        String propParamValue = propParamValueExpr; 
        if (propParamValue == null) {
            if (propDef.getDefaultValueExpr() != null) {
                propParamValue = propDef.getDefaultValueExpr();
                // TODO default expr may contains itself {{}} placeholder => resolve more... 
            }
        }
        Object parsedValue = null;
        try {
            parsedValue = propDef.getValueParser().parse(propParamValue);
        } catch(Exception ex) {
            LOG.error("Failed to parse prop " + propDef + " value '" + propParamValue + "' using " + propDef.getValueParser() + " ex=" + ex.getMessage());
            parsedValue = null;
        }
        ConfProp<Object> objProp = (ConfProp<Object>) props.get(propDef);

        if (objProp == null) {
            // init property
            if (propParamValue == null && ! propDef.isAllowNull()) {
                // ERROR !! property not set ! => object is invalid, should not instanciate object ?!!
                validProp = false;
            } else {
                if (propDef.isReadOnlyProp()) {
                    objProp = new ReadonlyValueConfProp<Object>(propDef, parsedValue);
                } else {
                    objProp = new ResolvedValueConfProp<Object>(propDef, propParamValueExpr, parsedValue);
                }
                props.put(propDef, objProp);
            }
        } else {
            // property already defined! ... merge override?
            if (objProp instanceof ResolvedValueConfProp) {
                ResolvedValueConfProp<Object> resolvedProp = (ResolvedValueConfProp<Object>) objProp;
                resolvedProp.setResolvedValue(propParamValueExpr, parsedValue);
            } // else do not modify readonly prop!
        }
        return validProp;
    }
    
}
