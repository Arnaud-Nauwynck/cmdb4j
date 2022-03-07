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

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.util.CmdbAssertUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxObjNode;
import lombok.Getter;

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
    
    public static final ResourceRelationshipType REL_require = new ResourceRelationshipType("require", "is-required-by");
    public static final ResourceRelationshipType REL_is_required_by = REL_require.inv();

    public static final ResourceRelationshipType REL_subscribe = new ResourceRelationshipType("subscribe", "notify");
    public static final ResourceRelationshipType REL_notify = REL_subscribe.inv();

    
    @Getter
    private final ResourceId id;

    @Getter
    private final ResourceType type;
    
    @Getter
    protected FxObjNode objData;

    @Getter
    protected FxSourceLoc declarationLocation;
    
    protected Map<String,Map<ResourceId,Resource>> relationships = new LinkedHashMap<>();
        
    protected Set<String> tags = new LinkedHashSet<String>();

    // ------------------------------------------------------------------------

    public Resource(ResourceId id, ResourceType type, FxObjNode objData, FxSourceLoc declarationLocation) {
        this.id = id;
        this.type = type;
        this.objData = objData;
        this.declarationLocation = declarationLocation;
    }

    // ------------------------------------------------------------------------

    @JsonIgnore
    public final String getIdAsString() {
        return id != null? id.toString() : null;
    }

    @JsonIgnore
    public String getTypeName() {
        return type != null? type.getName() : null;
    }

    public void setObjData(FxObjNode p) {
        this.objData = p;
    }

    protected Map<ResourceId,Resource> getOrCreateRelationship(ResourceRelationshipType relationshipType) {
        Map<ResourceId,Resource> res = relationships.get(relationshipType.name);
        if (res == null) {
            res = new LinkedHashMap<>();
            relationships.put(relationshipType.name, res);
        }
        return res;
    }

    /** @return unmodifiable and not null resources map for relationship */ 
    public Map<ResourceId, Resource> getRelationship(ResourceRelationshipType relationshipType) {
        return getRelationship(relationshipType.name);
    }
    
    /** @return unmodifiable and not null resources map for relationship */ 
    public Map<ResourceId, Resource> getRelationship(String relationshipType) {
        Map<ResourceId,Resource> tmpres = relationships.get(relationshipType);
        return (tmpres != null)? Collections.unmodifiableMap(tmpres) : Collections.emptyMap();
    }
        
    public void addRelation(ResourceRelationshipType relationshipType, Resource ref) {
        CmdbAssertUtils.checkNotNull(ref);
        ResourceRelationshipType invRelType = relationshipType.inv();
        Map<ResourceId,Resource> tos = getOrCreateRelationship(relationshipType);
        Resource prevRef = tos.put(ref.getId(), ref);
        if (prevRef != null) {
            Map<ResourceId,Resource> invFromRel = prevRef.getOrCreateRelationship(invRelType);
            invFromRel.remove(this.getId());
        }
        ref.getOrCreateRelationship(invRelType).put(this.getId(), this);
    }
    
    public void removeRelation(ResourceRelationshipType relationshipType, Resource ref) {
        CmdbAssertUtils.checkNotNull(ref);
        ResourceRelationshipType invRelType = relationshipType.inv();
        Map<ResourceId,Resource> tos = getOrCreateRelationship(relationshipType);
        Resource removedRef = tos.remove(ref.getId());
        if (removedRef != null) {
            removedRef.getOrCreateRelationship(invRelType).remove(this.getId());
        }
    }
    
    public void removeRelation(ResourceRelationshipType relationshipType, ResourceId refId) {
        CmdbAssertUtils.checkNotNull(refId);
        ResourceRelationshipType invRelType = relationshipType.inv();
        Map<ResourceId,Resource> tos = getOrCreateRelationship(relationshipType);
        Resource removedRef = tos.remove(refId);
        if (removedRef != null) {
            removedRef.getOrCreateRelationship(invRelType).remove(this.getId());
        }
    }
    
    
    public void addRequireResource(Resource ref) {
        addRelation(REL_require, ref);
    }

    public void removeRequireResource(Resource ref) {
        removeRelation(REL_require, ref);
    }

    public void removeRequireResourceId(ResourceId refId) {
        removeRelation(REL_require, refId);
    }

    public void addSubscribeResource(Resource ref) {
        addRelation(REL_subscribe, ref);
    }

    public void removeSubscribeResource(Resource refId) {
        removeRelation(REL_subscribe, refId);
    }
    
    public void removeSubscribeResourceId(ResourceId refId) {
        removeRelation(REL_subscribe, refId);
    }
    
    public Map<ResourceId, Resource> getRequireResources() {
        return getRelationship(REL_require);
    }

    public Set<ResourceId> getRequireResourceIds() {
        return getRelationship(REL_require).keySet();
    }

    public Map<ResourceId, Resource> getSubscribeResources() {
        return getRelationship(REL_subscribe);
    }

    public Map<ResourceId, Resource> getInvRequiredFromResources() {
        return getRelationship(REL_is_required_by);
    }

    public Map<ResourceId, Resource> getInvSubscribedFromResources() {
        return getRelationship(REL_notify);
    }

    
    /**
     * @return Transitive closure for resource --> dependenciesOf(resource) --> dependenciesOf(dependenciesOf(resource)) ... 
     */
    public Map<ResourceId, Resource> transitiveClosureResources(ResourceRelationshipType relationshipType) {
        return transitiveClosureResources(resource -> resource.getRelationship(relationshipType).values()); 
    }

    /**
     * @return Transitive closure for resource --> dependenciesOf(resource) --> dependenciesOf(dependenciesOf(resource)) ... 
     */
    public Map<ResourceId, Resource> transitiveClosureResources(String relationshipName) {
        return transitiveClosureResources(resource -> resource.getRelationship(relationshipName).values()); 
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
     * @return Transitive closure for resource --> require Resources --> ... 
     */
    public Map<ResourceId, Resource> getTransitiveRequireResources() {
        return transitiveClosureResources(REL_require);
    }

    /**
     * @return Transitive closure for resource --> is-required-by Resources --> ... 
     */
    public Map<ResourceId, Resource> getTransitiveInvRequireFromResources() {
        return transitiveClosureResources(REL_is_required_by);
    }
    
    /**
     * @return Transitive closure for resource --> subscribe Resources --> ... 
     */
    public Map<ResourceId, Resource> getTransitiveSubscribeResources() {
        return transitiveClosureResources(REL_subscribe);
    }
    
    /**
     * @return Transitive closure for resource --> notify Resources ->- ... 
     */
    public Map<ResourceId, Resource> getTransitiveNotifyResources() {
        return transitiveClosureResources(REL_notify);
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
        if (ls == null) {
            return  null;
        }
        return ls.stream().map(x -> x.id).collect(Collectors.toList());
    }

    public static Set<ResourceId> toIdSet(Collection<Resource> ls) {
        if (ls == null) {
            return null;
        }
        return ls.stream().map(x -> x.id).collect(Collectors.toSet());
    }

    public static Map<ResourceId,Resource> lsToIdMap(Collection<Resource> ls) {
        if (ls == null) {
            return null;
        }
        return lsToIdMap(new HashMap<>(), ls);
    }
    
    public static Map<ResourceId,Resource> lsToIdMap(Map<ResourceId,Resource> res, Collection<Resource> ls) {
        if (res == null) {
            res = new HashMap<>();
        }
        if (ls == null) {
            return res;
        }
        for(Resource e : ls) {
            res.put(e.id, e);
        }
        return res;
    }

    public static List<String> toIdAsStringList(Collection<Resource> ls) {
        if (ls == null) {
            return null;
        }
        return ls.stream().map(x -> x.getIdAsString()).collect(Collectors.toList());
    }

    public static Set<String> toIdAsStringSet(Collection<Resource> ls) {
        if (ls == null) {
            return null;
        }
        return ls.stream().map(x -> x.getIdAsString()).collect(Collectors.toSet());
    }

}
