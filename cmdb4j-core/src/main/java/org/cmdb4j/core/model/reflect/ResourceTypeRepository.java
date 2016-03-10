package org.cmdb4j.core.model.reflect;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.cmdb4j.core.util.CopyOnWriteUtils;

import com.google.common.collect.ImmutableList;

import fr.an.dynadapter.alt.AdapterAlternativesManager;
import fr.an.dynadapter.alt.IAdapterAlternativeFactory;
import fr.an.dynadapter.alt.IAdapterAlternativesManager;
import fr.an.dynadapter.alt.IAdapterAlternativesManagerSPI;
import fr.an.dynadapter.alt.ItfAlternativeId;
import fr.an.dynadapter.alt.ItfId;
import fr.an.dynadapter.typehiera.ITypeHierarchy;

/**
 * 
 */
public class ResourceTypeRepository {

    /**
     * thread safety: copy on write
     */
    private Map<String,ResourceType> name2types = Collections.emptyMap();
    
    private InnerTypeHierarchy innerTypeHierarchy = new InnerTypeHierarchy();
    
    /**
     * Manager for AdapterFactory .. to register new interface capabilities on existing types
     * (similar to eclipse PlaftormObject / IAdaptable / AdapterManager ... but with dynamic support)
     */
    private AdapterAlternativesManager<ResourceType> adapterManager = new AdapterAlternativesManager<>(innerTypeHierarchy);
    
    /**
     * thread safety: copy on write
     */
    private List<ResourceTypeRepositoryListener> listeners = ImmutableList.of();
    
    // ------------------------------------------------------------------------

    public ResourceTypeRepository() {
    }

    // ------------------------------------------------------------------------
    
    public ResourceType getOrCreateType(String name) {
        ResourceType res = name2types.get(name);
        if (res == null) {
            synchronized(this) {
                res = name2types.get(name);
                if (res == null) {
                    res = new ResourceType(this, name);
                    
                    HashMap<String,ResourceType> copyOnWrite = new LinkedHashMap<>(name2types);
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

    // management of listeners
    // ------------------------------------------------------------------------

    public void addistener(ResourceTypeRepositoryListener listener) {
        this.listeners = CopyOnWriteUtils.immutableCopyWithAdd(listeners, listener);
    }

    public void removeListener(ResourceTypeRepositoryListener listener) {
        this.listeners = CopyOnWriteUtils.immutableCopyWithRemove(listeners, listener);
    }

    
    
    // management of adapter / AdaptterFactory / ItfId
    // ------------------------------------------------------------------------
    
    public IAdapterAlternativesManager<ResourceType> getAdapterManager() {
        return adapterManager;
    }

    public <T> T getAdapter(Resource adaptable, ItfId<T> interfaceId) {
        return adapterManager.getAdapter(adaptable, interfaceId);
    }

    public <T> T getAdapter(Resource adaptable, ItfAlternativeId<T> interfaceAlternativeId) {
        return adapterManager.getAdapter(adaptable, interfaceAlternativeId);
    }

    
    
    public IAdapterAlternativesManagerSPI<ResourceType> getAdapterManagerSPI() {
        return adapterManager;
    }

    public void registerAdapters(IAdapterAlternativeFactory factory, ResourceType adaptableType) {
        adapterManager.registerAdapters(factory, adaptableType);
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
