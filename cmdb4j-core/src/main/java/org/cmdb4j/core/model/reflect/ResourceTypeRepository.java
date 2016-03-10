package org.cmdb4j.core.model.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.cmdb4j.core.util.CopyOnWriteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypeRepository.class);
    
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
            ResourceTypeRepositoryChange eventToSend = null;
            synchronized(this) {
                res = name2types.get(name);
                if (res == null) {
                    res = new ResourceType(this, name);
                    
                    HashMap<String,ResourceType> copyOnWrite = new LinkedHashMap<>(name2types);
                    copyOnWrite.put(name, res);
                    this.name2types = copyOnWrite;
                    
                    eventToSend = ResourceTypeRepositoryChange.newResourceTypeDeclChange(res, true); 
                }
            }
            if (eventToSend != null) { // fire out of synchronized
                fireChangeEvent(eventToSend);
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

    /*pp*/ void fireChangeEvent(ResourceTypeRepositoryChange change) {
        final List<ResourceTypeRepositoryListener> tmp = listeners;
        if (tmp != null && !tmp.isEmpty()) {
            for(ResourceTypeRepositoryListener e : listeners) {
                try {
                    e.onChange(change);
                } catch(Exception ex) {
                    LOG.error("Failure occured on listener onChange ..ignore, no rethrow!", ex);
                }
            }
        }
    }
    
    // management of adapter / AdapterFactory / ItfId
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

    
    // expose IAdapterAlternativesManagerSPI (wrap with proxy to fire change events)
    // => delegate all methods + fire events
    // ------------------------------------------------------------------------
    
    public IAdapterAlternativesManagerSPI<ResourceType> getAdapterManagerSPI() {
        return new IAdapterAlternativesManagerSPI<ResourceType>() {
            @Override
            public void registerAdapters(IAdapterAlternativeFactory factory, ResourceType adaptableType) {
                adapterManager.registerAdapters(factory, adaptableType);
                fireChangeEvent(ResourceTypeRepositoryChange.newResourceAdapterFactoryChange(null, factory, adaptableType));
            }
            @Override
            public void unregisterAdapters(IAdapterAlternativeFactory factory, ResourceType adaptableType) {
                adapterManager.unregisterAdapters(factory, adaptableType);
                fireChangeEvent(ResourceTypeRepositoryChange.newResourceAdapterFactoryChange(factory, null, adaptableType));
            }
            @Override
            public void registerAdapters(Collection<AdapterAltFactoryRegistration<ResourceType>> registrations) {
                adapterManager.registerAdapters(registrations);
                List<ResourceTypeRepositoryChange> chgs = new ArrayList<>();
                for(AdapterAltFactoryRegistration<ResourceType> e : registrations) {
                    chgs.add(ResourceTypeRepositoryChange.newResourceAdapterFactoryChange(null, e.factory, e.adaptableDataType));
                }
                fireChangeEvent(ResourceTypeRepositoryChange.newCompositeResourceTypeRepositoryChange(chgs));
            }
            @Override
            public void unregisterAdapters(Collection<AdapterAltFactoryRegistration<ResourceType>> registrations) {
                adapterManager.unregisterAdapters(registrations);
                List<ResourceTypeRepositoryChange> chgs = new ArrayList<>();
                for(AdapterAltFactoryRegistration<ResourceType> e : registrations) {
                    chgs.add(ResourceTypeRepositoryChange.newResourceAdapterFactoryChange(e.factory, null, e.adaptableDataType));
                }
                fireChangeEvent(ResourceTypeRepositoryChange.newCompositeResourceTypeRepositoryChange(chgs));
            }
            @Override
            public void flushLookup() {
                adapterManager.flushLookup();
                // fire event?
            }
        };
    }
    
    /** same as getAdapterManagerSPI().registerAdapters() */
    public void registerAdapters(IAdapterAlternativeFactory factory, ResourceType adaptableType) {
        getAdapterManagerSPI().registerAdapters(factory, adaptableType);
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
