package org.cmdb4j.core.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.Flat3Map;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.util.CmdbAssertUtils;

import fr.an.dynadapter.alt.IAdapterAlternativesManager;
import fr.an.dynadapter.alt.ItfId;
import fr.an.fxtree.model.FxObjNode;

/**
 * CMDB Resource element. 
 *
 */
public class Resource {

    public static final String FIELD_id = "id";
    public static final String FIELD_type = "type";
    public static final String FIELD_requiredResources = "requiredResources";
    public static final String FIELD_subscribeResources = "subscribeResources";
    public static final String FIELD_tags = "tags";
    
    
    private final ResourceId id;

    private final ResourceType type;
    
    protected FxObjNode objData;
    
    protected Map<ResourceId,Resource> requireResources = new LinkedHashMap<ResourceId,Resource>();

    protected Map<ResourceId,Resource> subscribeResources = new LinkedHashMap<ResourceId,Resource>();

    /** inverse of requireResources */ 
    protected Map<ResourceId,Resource> invRequiredFromResources = new LinkedHashMap<ResourceId,Resource>();

    /** inverse of subscribeResources */ 
    protected Map<ResourceId,Resource> invSubscribedFromResources = new LinkedHashMap<ResourceId,Resource>();
    
    protected Set<String> tags = new LinkedHashSet<String>();

    /**
     * cached created adapters, using <code>IAdapterAlternativesManager.getAdapter(itf, this)</code>
     */
    @SuppressWarnings("unchecked")
    protected Map<ItfId<?>,Object> cachedAdapters = (Map<ItfId<?>,Object>) new Flat3Map();
    
    // ------------------------------------------------------------------------

    public Resource(ResourceId id, ResourceType type, FxObjNode objData) {
        this.id = id;
        this.type = type;
        this.objData = objData;
    }

    // ------------------------------------------------------------------------

    public final ResourceId getId() {
        return id;
    }
    
    public ResourceType getType() {
        return type;
    }

    public FxObjNode getObjData() {
        return objData;
    }

    public void setObjData(FxObjNode p) {
        this.objData = p;
        synchronized (cachedAdapters) {
            this.cachedAdapters.clear();
        }
    }

    public void addRequireResource(Resource p) {
        CmdbAssertUtils.checkNotNull(p);
        Resource prev = requireResources.put(p.getId(), p);
        if (prev != null) {
            prev.invRequiredFromResources.remove(this.getId());
        }
        p.invRequiredFromResources.put(this.getId(), this);
    }

    public void removeRequireResource(Resource p) {
        CmdbAssertUtils.checkNotNull(p);
        Resource removed = requireResources.remove(p.getId());
        if (removed != null) {
            removed.invRequiredFromResources.remove(this.getId());
        }
    }

    public void removeRequireResourceId(ResourceId requireResourceId) {
        Resource removed = requireResources.remove(requireResourceId);
        if (removed != null) {
            removed.invRequiredFromResources.remove(this.getId());
        }
    }

    public void addSubscribeResource(Resource p) {
        CmdbAssertUtils.checkNotNull(p);
        Resource prev = subscribeResources.put(p.getId(), p);
        if (prev != null) {
            prev.invSubscribedFromResources.remove(this.getId());
        }
        p.invSubscribedFromResources.put(this.getId(), this);
    }

    public void removeSubscribeResource(Resource p) {
        CmdbAssertUtils.checkNotNull(p);
        Resource removed = subscribeResources.remove(p.getId());
        if (removed != null) {
            removed.invSubscribedFromResources.remove(this.getId());
        }
    }
    
    public void removeSubscribeResourceId(ResourceId subscribeResourceId) {
        Resource removed = subscribeResources.remove(subscribeResourceId);
        if (removed != null) {
            removed.invSubscribedFromResources.remove(this.getId());
        }
    }
    
    public Map<ResourceId, Resource> getRequireResources() {
        return Collections.unmodifiableMap(requireResources);
    }

    public Set<ResourceId> getRequireResourceIds() {
        return Collections.unmodifiableSet(requireResources.keySet());
    }

    public Map<ResourceId, Resource> getSubscribeResources() {
        return Collections.unmodifiableMap(subscribeResources);
    }

    public Map<ResourceId, Resource> getInvRequiredFromResources() {
        return Collections.unmodifiableMap(invRequiredFromResources);
    }

    public Map<ResourceId, Resource> getInvSubscribedFromResources() {
        return Collections.unmodifiableMap(invSubscribedFromResources);
    }

    /**
     * @return Transitive closure for resource --> dependenciesOf(resource) --> dependenciesOf(dependenciesOf(resource)) ... 
     */
    public Map<ResourceId, Resource> transitiveClosureResources(Function<Resource,Collection<Resource>> resourceDepsFunc) {
        Map<ResourceId, Resource> res = new LinkedHashMap<>();
        Queue<Resource> remain = new LinkedList<>();
        remain.addAll(resourceDepsFunc.apply(this));
        for(Resource r : remain) {
            res.put(r.getId(), r);
        }
        while(! remain.isEmpty()) {
            Resource resource = remain.poll();
            Collection<Resource> nextResources = resourceDepsFunc.apply(resource);
            if (nextResources != null && !nextResources.isEmpty()) {
                for(Resource next : nextResources) {
                    if (! res.containsKey(next.getId())) {
                        res.put(next.getId(), next);
                        remain.add(next);
                    }
                }
            }
        }
        return res;
    }

    /**
     * @return Transitive closure for resource --> requireResources --> ... 
     */
    public Map<ResourceId, Resource> getTransitiveRequireResources() {
        return transitiveClosureResources(x -> x.requireResources.values());
    }

    /**
     * @return Transitive closure for resource <-- invRequiredFromResources <-- ... 
     */
    public Map<ResourceId, Resource> getTransitiveInvRequireFromResources() {
        return transitiveClosureResources(x -> x.invRequiredFromResources.values());
    }
    
    /**
     * @return Transitive closure for resource --> subscribeResources --> ... 
     */
    public Map<ResourceId, Resource> getTransitiveSubscribeResources() {
        return transitiveClosureResources(x -> x.subscribeResources.values());
    }
    
    /**
     * @return Transitive closure for resource <-- invSubscribedFromResources <-- ... 
     */
    public Map<ResourceId, Resource> getAllTransitiveInvRequireResources() {
        return transitiveClosureResources(x -> x.invSubscribedFromResources.values());
    }
    
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public void clearTags() {
        tags.clear();
    }

    public void setAllTags(Collection<String> newTags) {
        this.tags.clear();
        if (newTags != null && !newTags.isEmpty()) {
            this.tags.addAll(newTags);
        }
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean hasAllTag(Collection<String> elts) {
        boolean res = true;
        for(String elt : elts) {
            if (! tags.contains(elt)) {
                res = false;
                break;
            }
        }
        return res;
    }

    public boolean hasAnyTag(Collection<String> elts) {
        boolean res = false;
        for(String elt : elts) {
            if (tags.contains(elt)) {
                res = true;
                break;
            }
        }
        return res;
    }
    
    private static Object NULL_MARKER = new Object();
    
    @SuppressWarnings("unchecked")
    public <T> T getAdapter(ItfId<T> itfId, IAdapterAlternativesManager<ResourceType> adapterManager) {
        synchronized (cachedAdapters) {
            Object tmpres = cachedAdapters.get(itfId);
            if (tmpres == NULL_MARKER) {
                return null;
            }
            T res;
            if (tmpres == null) {
                res = adapterManager.getAdapter(this, itfId);
                tmpres = (res != null)? res : NULL_MARKER;
                cachedAdapters.put(itfId, tmpres);
            } else {
                res = (T) tmpres;
            }
            return res;
        }
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return id + " (" + type + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    // static utility methods
    // ------------------------------------------------------------------------
    
    public static List<ResourceId> toIds(Collection<Resource> ls) {
        return ls.stream().map(x -> x.id).collect(Collectors.toList());
    }

    public static Map<ResourceId,Resource> lsToIdMap(Collection<Resource> ls) {
        return lsToIdMap(new HashMap<>(), ls);
    }
    
    public static Map<ResourceId,Resource> lsToIdMap(Map<ResourceId,Resource> res, Collection<Resource> ls) {
        if (res == null) {
            res = new HashMap<>();
        }
        for(Resource e : ls) {
            res.put(e.id, e);
        }
        return res;
    }

}
