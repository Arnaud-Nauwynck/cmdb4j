package org.cmdb4j.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.util.CmdbAssertUtils;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceRepository.class);
    
    private Map<ResourceId,Resource> id2resources = new ConcurrentHashMap<>();

    private Map<String,Map<ResourceId,Resource>> type2id2resources = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------------

    public ResourceRepository() {
    }

    // ------------------------------------------------------------------------
    
    public Resource findById(ResourceId id) {
        return id2resources.get(id);
    }
    
    public Resource getById(ResourceId id) {
        Resource res = id2resources.get(id);
        if (res == null) {
            throw new CmdbObjectNotFoundException("resource not found: '" + id + "'");
        }
        return res;
    }

    public void add(Resource obj) {
        CmdbAssertUtils.checkNotNull(obj);
        ResourceId id = obj.getId();
        id2resources.put(id, obj);
        
        String resourceName = obj.getType().getName();
        Map<ResourceId, Resource> byTypes = type2id2resources.get(resourceName);
        if (byTypes == null) {
            byTypes = new ConcurrentHashMap<>();
            type2id2resources.put(resourceName, byTypes);
        }
        byTypes.put(id, obj);
    }

    public void remove(Resource obj) {
        CmdbAssertUtils.checkNotNull(obj);
        ResourceId id = obj.getId();
        remove(id);
    }
    
    public void remove(ResourceId id) {
        CmdbAssertUtils.checkNotNull(id);
        Resource obj = id2resources.remove(id);
        if (obj != null) {
            String resourceName = obj.getType().getName();
            Map<ResourceId, Resource> byTypes = type2id2resources.get(resourceName);
            if (byTypes != null) {
                byTypes.remove(id);
            }// else should not occur!
        } else {
            LOG.debug("resource id '" + id + "' not found to remove .. ignore");
        }
    }

    public List<Resource> findByType(ResourceType type) {
        Collection<Resource> tmp = resourcesByType(type);
        return new ArrayList<>(tmp);
    }

    public List<Resource> findByCrit(Predicate<Resource> predicate) {
        List<Resource> res = new ArrayList<>();
        for(Resource e : id2resources.values()) {
            if (predicate == null || predicate.test(e)) {
                res.add(e);
            }
        }
        return res;
    }

    public Resource findFirstByCrit(Predicate<Resource> predicate) {
        Resource res = null;
        for(Resource e : id2resources.values()) {
            if (predicate == null || predicate.test(e)) {
                res = e;
                break;
            }
        }
        return res;
    }
    
    public List<Resource> findByTypeAndCrit(ResourceType type, Predicate<Resource> predicate) {
        List<Resource> res = new ArrayList<>();
        Collection<Resource> tmp = resourcesByType(type);
        for(Resource e : tmp) {
            if (predicate == null || predicate.test(e)) {
                res.add(e);
            }
        }
        return res;
    }

    public Resource findFirstByTypeAndCrit(ResourceType type, Predicate<Resource> predicate) {
        Resource res = null;
        Collection<Resource> tmp = resourcesByType(type);
        for(Resource e : tmp) {
            if (predicate == null || predicate.test(e)) {
                res = e;
                break;
            }
        }
        return res;
    }
    
    private Collection<Resource> resourcesByType(ResourceType type) {
        Collection<Resource> tmp;
        if (type == null) {
            tmp = id2resources.values();
        } else {
            Map<ResourceId,Resource> byTypes = type2id2resources.get(type.getName());
            tmp = (byTypes != null)? byTypes.values() : Collections.emptyList();
        }
        return tmp;
    }
    
}
