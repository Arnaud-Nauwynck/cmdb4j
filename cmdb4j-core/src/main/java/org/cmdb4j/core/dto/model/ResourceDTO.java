package org.cmdb4j.core.dto.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * DTO for Resource.
 *
 */
public class ResourceDTO implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;
    
    private String id;

    private String type;
    
    protected JsonNode objData;
    
    protected Set<String> requireResources = new LinkedHashSet<>();

    protected Set<String> subscribeResources = new LinkedHashSet<>();

    /** inverse of requireResources */ 
    protected Set<String> invRequiredFromResources = new LinkedHashSet<>();

    /** inverse of subscribeResources */ 
    protected Set<String> invSubscribedFromResources = new LinkedHashSet<>();
    
    protected Set<String> tags = new LinkedHashSet<>();
    
    // ------------------------------------------------------------------------

    public ResourceDTO() {
    }
    
    public ResourceDTO(String id, String type, JsonNode objData) {
        this.id = id;
        this.type = type;
        this.objData = objData;
    }

    // ------------------------------------------------------------------------

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public JsonNode getObjData() {
        return objData;
    }

    public void setObjData(JsonNode p) {
        this.objData = p;
    }
    
    public Set<String> getRequireResources() {
        return requireResources;
    }

    public void setRequireResources(Set<String> requireResources) {
        this.requireResources = requireResources;
    }

    public Set<String> getSubscribeResources() {
        return subscribeResources;
    }

    public void setSubscribeResources(Set<String> subscribeResources) {
        this.subscribeResources = subscribeResources;
    }

    public Set<String> getInvRequiredFromResources() {
        return invRequiredFromResources;
    }

    public void setInvRequiredFromResources(Set<String> invRequiredFromResources) {
        this.invRequiredFromResources = invRequiredFromResources;
    }

    public Set<String> getInvSubscribedFromResources() {
        return invSubscribedFromResources;
    }

    public void setInvSubscribedFromResources(Set<String> invSubscribedFromResources) {
        this.invSubscribedFromResources = invSubscribedFromResources;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> p) {
        this.tags = p;
    }
    
    public void addTag(String tag) {
        if (tags == null) {
            tags = new LinkedHashSet<>();
        }
        tags.add(tag);
    }

    public void removeTag(String tag) {
        if (tags == null) {
            return;
        }
        tags.remove(tag);
    }

    public void clearTags() {
        if (tags == null) {
            return;
        }
        tags.clear();
    }

    public void setAllTags(Collection<String> newTags) {
        if (tags == null) {
            tags = new LinkedHashSet<>();
        }
        this.tags.clear();
        if (newTags != null && !newTags.isEmpty()) {
            this.tags.addAll(newTags);
        }
    }

    public boolean hasTag(String tag) {
        if (tags == null) {
            return false;
        }
        return tags.contains(tag);
    }

    public boolean hasAllTag(Collection<String> elts) {
        boolean res = true;
        if (tags == null) {
            return false;
        }
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
        if (tags == null) {
            return false;
        }
        for(String elt : elts) {
            if (tags.contains(elt)) {
                res = true;
                break;
            }
        }
        return res;
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
        ResourceDTO other = (ResourceDTO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    // static utility methods
    // ------------------------------------------------------------------------
    
    public static List<String> toIds(Collection<ResourceDTO> ls) {
        return ls.stream().map(x -> x.id).collect(Collectors.toList());
    }

    public static Map<String,ResourceDTO> lsToIdMap(Collection<ResourceDTO> ls) {
        return lsToIdMap(new HashMap<>(), ls);
    }
    
    public static Map<String,ResourceDTO> lsToIdMap(Map<String,ResourceDTO> res, Collection<ResourceDTO> ls) {
        if (res == null) {
            res = new HashMap<>();
        }
        for(ResourceDTO e : ls) {
            res.put(e.id, e);
        }
        return res;
    }

}
