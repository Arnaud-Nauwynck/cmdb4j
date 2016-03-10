package org.cmdb4j.core.model.reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * 
 */
public class ResourceType {
    
    private static final Logger LOG = LoggerFactory.getLogger(ResourceType.class);
    
    private final ResourceTypeRepository owner;
    
    private final String name;
    
    
    /** dynamic list of contributed field defs (optionnal descriptions of fields)
     * typical usage: filled at startup by scanning definition files / plugins registration ... then unmodified
     * 
     * thread safety: immutable, copy on write map of fields 
     */
    private ImmutableMap<String,ResourceFieldDef> fields = ImmutableMap.of();
    
    /**
     * dynamic superType
     * typical usage: filled at startup by scanning definition files / plugins registration ... then unmodified
     * 
     * TODO allow several superTypes??  private ImmutableList<ResourceType> superTypes = ImmutableList.of(); 
     */
    private ResourceType superType;
    
    /**
     * dynamic list of superInterfaces
     * typical usage: filled at startup by scanning definition files / plugins registration ... then unmodified
     * 
     * thread safety: immutable, copy on write list 
     */
    private ImmutableList<ResourceType> superInterfaces = ImmutableList.of(); 

    // ------------------------------------------------------------------------
    
    public ResourceType(ResourceTypeRepository owner, String name) {
        this.owner = owner;
        this.name = name;
    }
    
    // ------------------------------------------------------------------------
    
    public ResourceTypeRepository getOwner() {
        return owner;
    }
    
    public String getName() {
        return name;
    }
    
    public Map<String, ResourceFieldDef> getFields() {
        return fields;
    }

    public Collection<ResourceFieldDef> fields() {
        return fields.values();
    }

    public <T> ResourceFieldDef getFieldOrThrow(String name) {
        ResourceFieldDef res = fields.get(name);
        if (res == null) {
            throw new RuntimeException("field not found: " + name);
        }
        return res;
    }

    public ResourceFieldDef field(String name) {
        return fields.get(name);
    }
    
    public ResourceType getSuperType() {
        return superType;
    }

    public ImmutableList<ResourceType> getSuperInterfaces() {
        return superInterfaces;
    }

    public ResourceFieldDef registerFieldDef(String fieldname, ResourceFieldDef.Builder fieldDefBuilder) {
        ResourceFieldDef prev = fields.get(fieldname);
        if (prev != null) {
            // OK accept overload data! (no merge supported yet?)
            LOG.info("overwritting ResourceType field def " + this.name + "." + fieldname);
        }
        ResourceFieldDef res = new ResourceFieldDef(this, fieldname, fieldDefBuilder);
        
        Map<String,ResourceFieldDef> tmp = new LinkedHashMap<String,ResourceFieldDef>(fields);
        tmp.put(fieldname, res);
        this.fields = ImmutableMap.copyOf(tmp);
        return res;
    }

    public void registerSuperType(ResourceType type) {
        if (this.superType == type) {
            return; // already registered
        }
        if (this.superType != null) {
            throw new IllegalStateException("multiple superTypes not allowed on type '" + name + "'" + 
                    " : type already inherit from '" + superType.name + "', trying to add '" + type.name + "'");
        }
        // check non circular loop...
        
        this.superType = type;
    }
    
    public void registerSuperInterfaceType(ResourceType superType) {
        if (superInterfaces.contains(superType)) {
            return; // already registered
        }
        // check non circular loop...
        
        List<ResourceType> tmp = new ArrayList<ResourceType>(this.superInterfaces);
        tmp.add(superType);
        this.superInterfaces = ImmutableList.copyOf(tmp);
    }

    /**
     * Returns the super-type search order starting with this type
     */
    public ResourceType[] computeSuperTypesOrder() {
        Set<ResourceType> seen = new LinkedHashSet<>(4);
        // first traverse type hierarchy
        List<ResourceType> allSuperTypes = new ArrayList<ResourceType>();
        ResourceType iterSuperType = this;
        while (iterSuperType != null) {
            allSuperTypes.add(iterSuperType);
            seen.add(iterSuperType);
            iterSuperType = iterSuperType.superType;
        }
        // now traverse interface hierarchy for each class
        for (ResourceType type : allSuperTypes) {
            computeInterfaceOrder(type.superInterfaces, seen);
        }
        return (ResourceType[]) seen.toArray(new ResourceType[seen.size()]);
    }

    private void computeInterfaceOrder(Collection<ResourceType> superInterfaces, Set<ResourceType> seen) {
        for (ResourceType e : superInterfaces) {
            if (!seen.contains(e)) {
                seen.add(e);
            }
        }
        // recurse
        for (ResourceType e : superInterfaces) {
            computeInterfaceOrder(e.superInterfaces, seen);
        }
    }

    
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ResourceType other = (ResourceType) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
    
}
