package org.cmdb4j.core.model.reflect;

import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;

import fr.an.dynadapter.alt.AdapterAlternativesManager;
import fr.an.dynadapter.typehiera.ITypeHierarchy;

public class ResourceTypeRepository {

    private Map<String,ResourceType> name2types = new HashMap<String,ResourceType>();
    
    private InnerTypeHierarchy innerTypeHierarchy = new InnerTypeHierarchy();
    
    /**
     * Manager for AdapterFactory .. to register new interface capabilities on existing types
     * (similar to eclipse PlaftormObject / IAdaptable / AdapterManager ... but with dynamic support)
     */
    private AdapterAlternativesManager<ResourceType> adapterManager = new AdapterAlternativesManager<>(innerTypeHierarchy);
    
    // ------------------------------------------------------------------------

    public ResourceTypeRepository() {
    }

    // ------------------------------------------------------------------------
    
    public AdapterAlternativesManager<ResourceType> getAdapterManager() {
        return adapterManager;
    }
    
    public ResourceType getOrCreateType(String name) {
        ResourceType res = name2types.get(name);
        if (res == null) {
            synchronized(this) {
                res = name2types.get(name);
                if (res == null) {
                    res = new ResourceType(name);
                    
                    HashMap<String,ResourceType> copyOnWrite = new HashMap<>(name2types);
                    copyOnWrite.put(name, res);
                    this.name2types = copyOnWrite;
                }
            }
        }
        return res;
    }
    
    public ResourceType getOrNull(String name) {
        return name2types.get(name);
    }

    public ResourceType getOrThrow(String name) {
        ResourceType res = name2types.get(name);
        if (res == null) {
            throw new CmdbObjectNotFoundException("resource type not found '" + name + "'");
        }
        return res;
    }

    // ------------------------------------------------------------------------

    private class InnerTypeHierarchy implements ITypeHierarchy<ResourceType> {

        @Override
        public ResourceType dataTypeOf(Object obj) {
            Resource resource = (Resource) obj;
            return resource.getType();
        }

        @Override
        public ResourceType[] computeSuperTypesOrder(ResourceType type) {
            return type.computeSuperTypesOrder();
        }

        @Override
        public void flushLookup() {
        }
        
    }
    
    
}
