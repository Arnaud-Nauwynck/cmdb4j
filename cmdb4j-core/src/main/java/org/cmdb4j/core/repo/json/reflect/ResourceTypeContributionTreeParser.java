package org.cmdb4j.core.repo.json.reflect;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.cmdb4j.core.model.reflect.ResourceFieldDef;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.dynadapter.alt.IAdapterAlternativeFactory;
import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.helper.FxObjNodeWithTypeTreeScanner;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

/**
 * helper class to parse a fx-tree (Json / Yaml / *) as a list of ResourceType contributions, 
 * to register into <code>ResourceTypeRepository target</code>.
 *
 * <p>
 * sample recognised format:  (notice several type declaration contribution for the same type, and partial unspecified types)
 * <PRE>
 * [
 *  {
 *   type: "resourceTypeDecl",
 *   resourceTypeName: "Webserver"
 *  },
 *  
 *  {
 *   type: "resourceTypeDecl",
 *   resourceTypeName: "Tomcat",
 *   superTypeName: "Webserver",
 *   superInterfaceNames: [ "TomcatWebAppManager" ],
 *   fields: {
 *      port: {
 *        description: "http port number of tomcat server (default 8080)",
 *        defaultValue: 8080 
 *      },
 *      httpsPort: {
 *        description: "https port number of tomcat server (default 8443)",
 *        defaultValue: 8443 
 *      }
 *   }
 *  },
 *  
 *  {
 *   type: "resourceTypeDecl",
 *   resourceTypeName: "Tomcat",
 *   superInterfaceNames: [ "SSLKeyStoreSupport" ],
 *   fields: {
 *      sslKeystoreFile: {
 *        description: "SSL keystore file"
 *      }
 *   }
 *  },
 *  
 *  {
 *    type: "adapterFactoryDecl",
 *    resourceTypeName: "Tomcat",
 *    adapterFactoryClassName: "com.foo.plugins.tomcat.TomcatStartSupportAdapterFactory"
 *  }
 * ]
 * </PRE>
 */
public class ResourceTypeContributionTreeParser {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypeContributionTreeParser.class);
    
    protected ResourceTypeRepository target;
    protected boolean strict;
    protected Function<String,IAdapterAlternativeFactory> adapterFactoryLookup;
    
    // ------------------------------------------------------------------------
    
    public ResourceTypeContributionTreeParser(ResourceTypeRepository target, boolean strict, 
            Function<String,IAdapterAlternativeFactory> adapterFactoryLookup) {
        this.target = target;
        this.strict = strict;
        this.adapterFactoryLookup = adapterFactoryLookup;
    }

    // ------------------------------------------------------------------------

    public void addParseContributions(File file) {
        FxNode content = FxFileUtils.readTree(file);
        addParseContributions(content);
    }

    public void addParseContributions(FxNode node) {
        List<FxObjNode> objs = FxObjNodeWithTypeTreeScanner.scanFxObjNodesWithType(node);
        for(FxObjNode obj : objs) {
            String declType = obj.get("type").textValue();
            try {
                switch(declType) {
                case "resourceTypeDecl":
                    addParseResourceTypeDecl(obj);
                    break;
                case "adapterFactoryDecl":
                    addParseAdapterFactoryDecl(obj);
                    break;
                default:
                    if (strict) {
                        throw new IllegalArgumentException("unrecognised type '" + declType + "', expecting resourceTypeDecl");
                    }
                    // ignore unrecognised type?
                    break;
                }
            } catch(Exception ex) {
                String err = "Failed to parse decl '" + declType + "', ex:" + ex.getMessage() + ", data:" + obj;
                if (strict) {
                    throw new IllegalArgumentException(err, ex);
                } else {
                    LOG.warn(err + " ...ignore, no rethrow! ex:" + ex.getMessage());
                }
            }
        };
    }

    public void addParseResourceTypeDecl(FxObjNode obj) {
        String typeName = FxNodeValueUtils.getStringOrThrow(obj, "resourceTypeName");
        ResourceType resourceType = target.getOrCreateType(typeName);
        
        FxObjNode fieldsNode = FxNodeValueUtils.getObjOrNull(obj, "fields");
        if (fieldsNode != null) {
            fieldsNode.forEachFields((name,node) -> {
                if (node instanceof FxObjNode) {
                    addParseFieldDecl(resourceType, name, (FxObjNode) node);
                }
            });
        }
        
        String superTypename = FxNodeValueUtils.getOrDefault(obj, "superTypeName", null);
        if (superTypename != null) {
            ResourceType superType = target.getOrCreateType(superTypename);
            resourceType.registerSuperType(superType);
        }
        
        String[] superInterfaceNames = FxNodeValueUtils.getStringArrayOrNull(obj, "superInterfaceNames", false);
        if (superInterfaceNames != null) {
            for(String e : superInterfaceNames) {
                ResourceType superInterface = target.getOrCreateType(e);
                resourceType.registerSuperInterfaceType(superInterface);
            }
        }
    }

    protected void addParseFieldDecl(ResourceType resourceType, String fieldname, FxObjNode node) {
        ResourceFieldDef.Builder b = new ResourceFieldDef.Builder();
        ResourceFieldDef foundField = resourceType.field(fieldname);
        if (foundField != null) {
            // => overwrite field infos..
        }
        
        String valueTypeName = FxNodeValueUtils.getOrDefault(node, "valueType", null);
        if (valueTypeName != null) {
            Class<?> valueClass;
            try {
                valueClass = Class.forName(valueTypeName);
                b.valueType(valueClass);
            } catch (ClassNotFoundException e) {
                LOG.error("Class not found for valueType '" + valueTypeName + "' on field decl " + resourceType.getName() + "." + fieldname);
            }
        }

        String description = FxNodeValueUtils.getOrDefault(node, "description", null);
        if (description != null) {
            b.description(description);
        }
        
        //TODO .. add more info
        
        resourceType.registerFieldDef(fieldname, b);
    }

    public void addParseAdapterFactoryDecl(FxObjNode obj) {
        String resourceTypeName = FxNodeValueUtils.getStringOrThrow(obj, "resourceTypeName");
        ResourceType resourceType = target.getOrCreateType(resourceTypeName);
        
        IAdapterAlternativeFactory factory;  
        String adapterFactoryClassName = FxNodeValueUtils.getOrDefault(obj, "adapterFactoryClassName", null);
        if (adapterFactoryClassName != null) {
            Class<?> adapterFactoryClass = classForNameOrRethrow(adapterFactoryClassName);
            Object factoryObj;
            try {
                factoryObj = adapterFactoryClass.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to instanciate object for class '" + adapterFactoryClass + "'", ex);
            }
            factory = (IAdapterAlternativeFactory) factoryObj;
        } else if (adapterFactoryLookup != null) {
            String adapterFactoryName = FxNodeValueUtils.getStringOrThrow(obj, "adapterFactoryName");
            factory = adapterFactoryLookup.apply(adapterFactoryName);
            if (factory == null) {
                String err = "Failed to lookup adapterFactory by name: '" + adapterFactoryName + "'";
                LOG.error(err);
                if (strict) {
                    throw new IllegalStateException(err); 
                }
            }
        } else {
            throw new IllegalArgumentException("adapterFactory argument not set");
        }
        
        target.registerAdapters(factory, resourceType);
    }

//    protected ItfId<?> parseIItfId(FxObjNode obj, String fieldPrefix) {
//        String interfaceIdJavaClassName = FxNodeValueUtils.getStringOrThrow(obj, fieldPrefix + "JavaClassName");
//        String interfaceIdName = FxNodeValueUtils.getOrDefault(obj, fieldPrefix + "Name", "");
//        if (interfaceIdJavaClassName == null && interfaceIdName.isEmpty()) {
//            throw new IllegalArgumentException("invalid interfaceId: javaClass & name empty!");
//        }
//        Class<?> interfaceIdJavaClass = null;
//        if (interfaceIdJavaClassName != null) {
//            interfaceIdJavaClass = classForNameOrRethrow(interfaceIdJavaClassName);
//        } else {
//            interfaceIdJavaClass = Object.class;
//        }
//        return ItfId.of(interfaceIdJavaClass, interfaceIdName);
//    }

    protected Class<?> classForNameOrRethrow(String className) {
        Class<?> res;
        try {
            res = Class.forName(className);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to get class for name '" + className + "'", ex);
        }
        return res;
    }
}
