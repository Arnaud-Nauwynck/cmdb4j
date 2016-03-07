package org.cmdb4j.core.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cmdb4j.core.util.CmdbObjectNotFoundException;

public class ResourceRepository {

    private Map<ResourceId,Resource> id2resources = new ConcurrentHashMap<>();
    
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

    public void put(ResourceId id, Resource obj) {
        id2resources.put(id, obj);
    }

    public void remove(ResourceId id) {
        id2resources.remove(id);
    }

}
