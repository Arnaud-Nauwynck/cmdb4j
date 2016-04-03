package org.cmdb4j.core.model;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepositoryChange;
import org.cmdb4j.core.model.reflect.ResourceTypeRepositoryChange.AbstractResourceTypeChange;
import org.cmdb4j.core.model.reflect.ResourceTypeRepositoryChange.CompositeResourceTypeRepositoryChange;
import org.cmdb4j.core.model.reflect.ResourceTypeRepositoryListener;
import org.cmdb4j.core.util.CmdbAssertUtils;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import fr.an.dynadapter.alt.IAdapterAlternativesManager;
import fr.an.dynadapter.alt.ItfId;

/**
 * Repository for Resource elements.
 * <p>
 * 
 * Resources can be added/removed, and modified.<BR/>
 * ... id and type do not change per Resource, everything else can dynamically
 * change (type hierarchy..)
 * 
 * Resources can be queryed by id, by exact datatype or superType hierarchy, and
 * by (adapter) interface. Query results are internally cached when it involves
 * only id/type/superType/interfaces, but query with other Predicate may be
 * slower. It is expected that there are less than million(s) of resources, so
 * everything fits in memory.
 *
 * <p>
 * Thread safety: thread-safe using internal lock
 */
public class ResourceRepository implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRepository.class);

    /**
     * corresponding ResourceType repository (contains all known ResourceType(s)
     * + hierarchy of type->superType/superInterfaces + cached sub type
     * hierarchies)
     */
    private final ResourceTypeRepository resourceTypeRepository;

    private ResourceTypeRepositoryListener innerTypeListener;

    private final Object lock = new Object();

    /**
     * Thread safety: @ProtectedBy("lock")
     */
    private final Map<ResourceId, Resource> id2resources = new LinkedHashMap<>();

    /**
     * derived index field for <code>id2resources</code> equivalent to pseudo ql
     * "select id,resource from id2resources groupby resource.type" Thread
     * safety: @ProtectedBy("lock")
     */
    private final Map<ResourceType, Map<ResourceId, Resource>> type2Id2Resources = new LinkedHashMap<>();

    /**
     * lazy computed cache, derived index field for <code>id2resources</code>
     * equivalent to pseudo ql
     * "select superType,id,resource from id2resources, ResourceType superType where (resource.type instanceof superType) groupby superType"
     * this requires registering to repositoryType change events, as type
     * hierarchy may change Thread safety: @ProtectedBy("lock")
     */
    private final Map<ResourceType, Map<ResourceId, Resource>> cacheSupertype2Id2Resources = new LinkedHashMap<>();

    /**
     * lazy computed cache, derived index field for <code>id2resources</code>
     * equivalent to pseudo ql
     * "select itfId,id,getAdapter(resource,itfId) from id2resources groupby itfId"
     * Thread safety: @ProtectedBy("lock")
     */
    private final Map<ItfId<?>, Map<ResourceId, ?>> cacheItf2Id2Adapters = new LinkedHashMap<>();

    // ------------------------------------------------------------------------

    public ResourceRepository(ResourceTypeRepository resourceTypeRepository) {
        CmdbAssertUtils.checkNotNull(resourceTypeRepository);
        this.resourceTypeRepository = resourceTypeRepository;
        this.innerTypeListener = chg -> onResourceTypeRepositoryChange(chg);
        resourceTypeRepository.addListener(innerTypeListener);
    }

    public ResourceRepository(ResourceTypeRepository resourceTypeRepository, Collection<Resource> resources) {
        this(resourceTypeRepository);
        addAll(resources);
    }

    @Override
    public void close() {
        if (innerTypeListener != null) {
            resourceTypeRepository.removeListener(innerTypeListener);
            innerTypeListener = null;
            synchronized (lock) {
                id2resources.clear();
                type2Id2Resources.clear();
                cacheSupertype2Id2Resources.clear();
                cacheItf2Id2Adapters.clear();
            }
        }
    }

    // -----------------------------------------------------------------------

    public Resource findById(ResourceId id) {
        synchronized (lock) {
            return id2resources.get(id);
        }
    }

    public Resource getById(ResourceId id) {
        Resource res;
        synchronized (lock) {
            res = id2resources.get(id);
        }
        if (res == null) {
            throw new CmdbObjectNotFoundException("resource not found: '" + id + "'");
        }
        return res;
    }

    public void addAll(Collection<Resource> objs) {
        for (Resource e : objs) {
            add(e);
        }
    }

    public void add(Resource obj) {
        CmdbAssertUtils.checkNotNull(obj);
        ResourceId id = obj.getId();
        ResourceType resourceType = obj.getType();
        synchronized (lock) {
            id2resources.put(id, obj);
            Map<ResourceId, Resource> byTypes = type2Id2Resources.get(resourceType);
            if (byTypes == null) {
                byTypes = new ConcurrentHashMap<>();
                type2Id2Resources.put(resourceType, byTypes);
            }
            byTypes.put(id, obj);

            // TOOPTIM? .. right now, simply re-index all
            cacheSupertype2Id2Resources.clear();
            cacheItf2Id2Adapters.clear();
        }
    }

    public void remove(Resource obj) {
        CmdbAssertUtils.checkNotNull(obj);
        ResourceId id = obj.getId();
        remove(id);
    }

    public void remove(ResourceId id) {
        CmdbAssertUtils.checkNotNull(id);
        synchronized (lock) {
            Resource obj = id2resources.remove(id);
            if (obj != null) {
                ResourceType resourceType = obj.getType();
                Map<ResourceId, Resource> byTypes = type2Id2Resources.get(resourceType);
                if (byTypes != null) {
                    byTypes.remove(id);
                } // else should not occur!
            } else {
                LOG.debug("resource id '" + id + "' not found to remove .. ignore");
            }

            // TOOPTIM? .. right now, simply re-index all
            cacheSupertype2Id2Resources.clear();
            cacheItf2Id2Adapters.clear();
        }
    }

    public List<Resource> findAll() {
        synchronized (lock) {
            return new ArrayList<>(id2resources.values());
        }
    }

    public Map<ResourceId,Resource> findAllAsMap() {
        synchronized (lock) {
            return new HashMap<>(id2resources);
        }
    }
    
    public List<ResourceId> findAllIds() {
        synchronized (lock) {
            return new ArrayList<>(id2resources.keySet());
        }
    }

    public List<Resource> findByCrit(Predicate<Resource> predicate) {
        List<Resource> res = new ArrayList<>();
        synchronized (lock) {
            for (Resource e : id2resources.values()) {
                if (predicate == null || predicate.test(e)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    public Resource findFirstByCrit(Predicate<Resource> predicate) {
        Resource res = null;
        synchronized (lock) {
            for (Resource e : id2resources.values()) {
                if (predicate == null || predicate.test(e)) {
                    res = e;
                    break;
                }
            }
        }
        return res;
    }

    public List<Resource> findByExactType(ResourceType type) {
        synchronized (lock) {
            Collection<Resource> tmp = internalResourcesByExactType(type);
            return new ArrayList<>(tmp);
        }
    }

    public List<Resource> findByExactTypeAndCrit(ResourceType type, Predicate<Resource> predicate) {
        List<Resource> res = new ArrayList<>();
        synchronized (lock) {
            Collection<Resource> tmp = internalResourcesByExactType(type);
            for (Resource e : tmp) {
                if (predicate == null || predicate.test(e)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    public Resource findFirstByExactTypeAndCrit(ResourceType type, Predicate<Resource> predicate) {
        Resource res = null;
        synchronized (lock) {
            Collection<Resource> tmp = internalResourcesByExactType(type);
            for (Resource e : tmp) {
                if (predicate == null || predicate.test(e)) {
                    res = e;
                    break;
                }
            }
        }
        return res;
    }

    // Query using data Type->SubType hierarchy
    // ------------------------------------------------------------------------

    public List<Resource> findBySubType(ResourceType ancestorType) {
        synchronized (lock) {
            return new ArrayList<>(internalResourcesBySubType(ancestorType).values());
        }
    }

    public List<Resource> findBySubTypeAndCrit(ResourceType ancestorType, Predicate<Resource> predicate) {
        List<Resource> res = new ArrayList<>();
        synchronized (lock) {
            Map<ResourceId, Resource> tmp = internalResourcesBySubType(ancestorType);
            for (Resource e : tmp.values()) {
                if (predicate == null || predicate.test(e)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    public Resource findFirstBySubTypeAndCrit(ResourceType ancestorType, Predicate<Resource> predicate) {
        Resource res = null;
        synchronized (lock) {
            Map<ResourceId, Resource> tmp = internalResourcesBySubType(ancestorType);
            for (Resource e : tmp.values()) {
                if (predicate == null || predicate.test(e)) {
                    res = e;
                    break;
                }
            }
        }
        return res;
    }

    // Query using registered adapterFactory per interfaceId: ItfId->Adaptable
    // ------------------------------------------------------------------------

    public <T> T findAdapterById(ResourceId resourceId, ItfId<T> itfId) {
        Resource resource = findById(resourceId);
        if (resource == null) {
            return null;
        }
        return resourceToAdapter(resource, itfId);
    }

    public <T> List<T> findAdaptersByItf(ItfId<T> itfId) {
        synchronized (lock) {
            return new ArrayList<>(internalAdaptersByItf(itfId).values());
        }
    }

    public <T> List<T> findAdaptersByItfAndCrit(ItfId<T> itfId, Predicate<T> adapterPredicate) {
        List<T> res = new ArrayList<>();
        synchronized (lock) {
            Map<ResourceId, T> tmp = internalAdaptersByItf(itfId);
            for (T e : tmp.values()) {
                if (adapterPredicate == null || adapterPredicate.test(e)) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    public <T> T findFirstAdapterByItfAndCrit(ItfId<T> itfId, Predicate<T> adapterPredicate) {
        T res = null;
        synchronized (lock) {
            Map<ResourceId, T> tmp = internalAdaptersByItf(itfId);
            for (T e : tmp.values()) {
                if (adapterPredicate == null | adapterPredicate.test(e)) {
                    res = e;
                    break;
                }
            }
        }
        return res;
    }

    // Internal Cached Query per Type & SubType
    // ------------------------------------------------------------------------

    private Collection<Resource> internalResourcesByExactType(ResourceType type) {
        Collection<Resource> tmp;
        if (type == null) {
            tmp = id2resources.values();
        } else {
            Map<ResourceId, Resource> byTypes = type2Id2Resources.get(type);
            tmp = (byTypes != null) ? byTypes.values() : Collections.emptyList();
        }
        return tmp;
    }

    protected Map<ResourceId, Resource> internalResourcesBySubType(ResourceType ancestorType) {
        Map<ResourceId, Resource> res = cacheSupertype2Id2Resources.get(ancestorType);
        if (res == null) {
            res = doInternalResourcesBySubType(ancestorType);
            cacheSupertype2Id2Resources.put(ancestorType, res);
        }
        return res;
    }

    protected Map<ResourceId, Resource> doInternalResourcesBySubType(ResourceType ancestorType) {
        Map<ResourceId, Resource> res = new LinkedHashMap<>();
        Set<ResourceType> subTypes = resourceTypeRepository.subTypeHierarchyOf(ancestorType);
        for (ResourceType subType : subTypes) {
            Collection<Resource> tmpres = internalResourcesByExactType(subType);
            Resource.lsToIdMap(res, tmpres);
        }
        return ImmutableMap.copyOf(res);
    }

    // Internal Cached Query Adapters per ItfId
    // ------------------------------------------------------------------------

    protected <T> Map<ResourceId, T> internalAdaptersByItf(ItfId<T> itfId) {
        @SuppressWarnings("unchecked")
        Map<ResourceId, T> res = (Map<ResourceId, T>) cacheItf2Id2Adapters.get(itfId);
        if (res == null) {
            res = doInternalAdaptersByItf(itfId);
            cacheItf2Id2Adapters.put(itfId, res);
        }
        return res;
    }

    protected <T> Map<ResourceId, T> doInternalAdaptersByItf(ItfId<T> itfId) {
        Map<ResourceId, T> res = new LinkedHashMap<>();
        boolean useNaiveImpl = false; // true
        if (useNaiveImpl) {
            doResourcesToAdapters(res, id2resources.values(), itfId);
        } else {
            Collection<ResourceType> resourceTypes = resourceTypeRepository.computeDataTypesHavingAdapterItf(itfId);
            for (ResourceType resourceType : resourceTypes) {
                Collection<Resource> resources = internalResourcesByExactType(resourceType);
                doResourcesToAdapters(res, resources, itfId);
            }
        }
        return ImmutableMap.copyOf(res);
    }

    protected <T> void doResourcesToAdapters(Map<ResourceId, T> res, Collection<Resource> resources, ItfId<T> itfId) {
        for (Resource resource : resources) {
            ResourceId id = resource.getId();
            try {
                T resElt = resourceToAdapter(resource, itfId);
                if (resElt != null) {
                    res.put(id, resElt);
                }
            } catch (Exception ex) {
                LOG.warn("Failed to query adapter " + itfId + " for resource " + id + " ... ignore, skip  ex:" + ex.getMessage());
            }
        }
    }

    public <T> T resourceToAdapter(Resource resource, ItfId<T> itfId) {
        IAdapterAlternativesManager<ResourceType> adapterManager = resourceTypeRepository.getAdapterManager();
        // TODO.. may cache wrap adapter on resource?
        T res = adapterManager.getAdapter(resource, itfId);
        return res;
    }

    // ------------------------------------------------------------------------

    /* callback listener for resourceTypeRepository.addistener */
    protected void onResourceTypeRepositoryChange(ResourceTypeRepositoryChange change) {
        if (change instanceof AbstractResourceTypeChange || change instanceof CompositeResourceTypeRepositoryChange) {
            // TOOPTIM? .. right now, simply re-index all
            synchronized (lock) {
                cacheSupertype2Id2Resources.clear();
                cacheItf2Id2Adapters.clear();
            }
        } else {
            // (AdapterFactory added/removed ..)
            synchronized (lock) {
                cacheItf2Id2Adapters.clear();
            }
        }
    }

}
