package org.cmdb4j.core.model.reflect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.util.CmdbObjectNotFoundException;
import org.cmdb4j.core.util.CopyOnWriteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * 
 */
public class ResourceTypeRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceTypeRepository.class);
    
    private final Object lock = new Object();
    
    /**
     * thread safety: immutable with copy-on-write, @ProtectedBy(lock)
     */
    private Map<String,ResourceType> name2types = Collections.emptyMap();
    
//    /**
//     * cached super-type herarchy for a type:
//     * 
//     * equivalent example with classes: 
//     * given "class A{}; class B extends A {}; class C extends B {}; class D extends A {}"
//     * => superTypesOf(A) = [A], superTypesOf(B) = [B, A], superTypesOf(C) = [C, B, A], ...  
//     * 
//     *  thread safety: immutable with copy-on-write, @ProtectedBy(lock)
//     */
//    private Map<ResourceType,Set<ResourceType>> cacheTypeToSuperTypes = Collections.emptyMap();

    /**
     * cached sub-type herarchy for a type:
     * 
     * equivalent example with classes: 
     * given "class A{}; class B extends A {}; class C extends B {}; class D extends A {}"
     * => subTypesOf(A) = [A, B, C, D], subTypesOf(B) = [B, C, D], subTypesOf(C) = [C], ...  
     * 
     *  thread safety: immutable with copy-on-write, @ProtectedBy(lock)
     */
    private Map<ResourceType,Set<ResourceType>> cacheTypeToSubTypes = Collections.emptyMap();
    
    /**
     * internal for AdapterAlternativesManager
     */
    private InnerTypeHierarchy innerTypeHierarchy = new InnerTypeHierarchy();
        
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
            synchronized(lock) {
                res = name2types.get(name);
                if (res == null) {
                    res = new ResourceType(this, name);
                    
                    HashMap<String,ResourceType> copyOnWrite = new LinkedHashMap<>(name2types);
                    copyOnWrite.put(name, res);
                    this.name2types = copyOnWrite;
                    
                    eventToSend = ResourceTypeRepositoryChange.newResourceTypeDeclChange(res, true);
                    invalidateHierarchyChange();
                }
            }
            if (eventToSend != null) { // fire out of synchronized
                fireChangeEvent(eventToSend);
            }
        }
        return res;
    }
    
    public ResourceType findByName(String name) {
        return name2types.get(name);
    }

    public ResourceType get(String name) {
        ResourceType res = name2types.get(name);
        if (res == null) {
            throw new CmdbObjectNotFoundException("resource type not found '" + name + "'");
        }
        return res;
    }

    public ResourceType get(ResourceTypeId resourceTypeId) {
        return get(resourceTypeId.getName());
    }
    

    // management of listeners
    // ------------------------------------------------------------------------

    public void addListener(ResourceTypeRepositoryListener listener) {
        synchronized(lock) {
            this.listeners = CopyOnWriteUtils.immutableCopyWithAdd(listeners, listener);
        }
    }

    public void removeListener(ResourceTypeRepositoryListener listener) {
        synchronized(lock) {
            this.listeners = CopyOnWriteUtils.immutableCopyWithRemove(listeners, listener);
        }
    }

    /*pp*/ void onChangeWithoutHierarchyChange(ResourceTypeRepositoryChange change) {
        fireChangeEvent(change);
    }

    /*pp*/ void onChangeWithHierarchyChange(ResourceTypeRepositoryChange change) {
        synchronized(lock) {
            invalidateHierarchyChange();
        }
        fireChangeEvent(change);
    }

    private void invalidateHierarchyChange() {
//        cacheTypeToSuperTypes = Collections.emptyMap();
        cacheTypeToSubTypes = Collections.emptyMap();
    }
    
    private void fireChangeEvent(ResourceTypeRepositoryChange change) {
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
    
    
    // (cached) helper for TypeHierarchy 
    // ------------------------------------------------------------------------

    public Set<ResourceType> superTypeHierarchyOf(ResourceType type) {
        ResourceType[] tmpres = innerTypeHierarchy.computeSuperTypesOrder(type);
        return ImmutableSet.copyOf(Arrays.asList(tmpres));
    }

    public Set<ResourceType> subTypeHierarchyOf(ResourceType type) {
        Set<ResourceType> res = cacheTypeToSubTypes.get(type);
        if (res == null) {
            synchronized(lock) {
                res = cacheTypeToSubTypes.get(type); //re-get within lock
                if (res == null) {
                    res = doSubTypeHierarchyOf(type);
                    
                    // copy-on-write cacheTypeToSubTypes.put(type, res);
                    cacheTypeToSubTypes = CopyOnWriteUtils.immutableCopyWithPut(cacheTypeToSubTypes, type, res);
                }
            }
        }
        return res;        
    }
    
    protected Set<ResourceType> doSubTypeHierarchyOf(ResourceType type) {
        Set<ResourceType> res = new LinkedHashSet<ResourceType>();
        res.add(type);
        doRecursiveScanDirectSubType(res, type);
        doRecursiveScanDirectSubInterface(res, type);
        return ImmutableSet.copyOf(res);
    }

    private void doRecursiveScanDirectSubType(Set<ResourceType> res, ResourceType type) {
        for(ResourceType t : name2types.values()) {
            if (t.getSuperType() == type && !res.contains(t)) {
                res.add(t);
                // recurse
                doRecursiveScanDirectSubType(res, t);
            }
        }
    }

    private void doRecursiveScanDirectSubInterface(Set<ResourceType> res, ResourceType type) {
        for(ResourceType t : name2types.values()) {
            ImmutableList<ResourceType> superInterfaces = t.getSuperInterfaces();
            if (superInterfaces != null && superInterfaces.contains(type) && !res.contains(t)) {
                res.add(t);
                // recurse
                doRecursiveScanDirectSubInterface(res, t);
            }
        }
    }
    
    // ------------------------------------------------------------------------

    private class InnerTypeHierarchy { // implements ITypeHierarchy<ResourceType> {

//        @Override
        public ResourceType dataTypeOf(Object obj) {
            Resource resource = (Resource) obj;
            return resource.getType();
        }

//        @Override
        public ResourceType[] computeSuperTypesOrder(ResourceType type) {
            return type.computeSuperTypesOrder();
        }

//        @Override
        public void flushLookup() {
        }
        
    }

}
