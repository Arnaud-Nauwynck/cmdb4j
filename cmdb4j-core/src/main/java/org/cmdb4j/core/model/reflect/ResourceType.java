package org.cmdb4j.core.model.reflect;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 */
public class ResourceType {

    private final String name;

    private Map<String,ResourceFieldDef> fields = new LinkedHashMap<String,ResourceFieldDef>();
    
    // ------------------------------------------------------------------------
    
    public ResourceType(String name) {
        this.name = name;
    }
    
    // ------------------------------------------------------------------------
    
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

    public ResourceFieldDef registerFieldDef(String name, ResourceFieldDef.Builder fieldDefBuilder) {
        ResourceFieldDef prev = fields.get(name);
        if (prev != null) throw new IllegalStateException();
        ResourceFieldDef res = new ResourceFieldDef(this, name, fieldDefBuilder);
        fields.put(name, res);
        return res;
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
