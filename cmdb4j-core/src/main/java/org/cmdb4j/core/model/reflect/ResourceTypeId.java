package org.cmdb4j.core.model.reflect;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * id for ResourceType (= name typed String)
 */
public final class ResourceTypeId implements Serializable, Comparable<ResourceTypeId> {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;

    private final String name;

    // ------------------------------------------------------------------------
    
    public ResourceTypeId(String name) {
        this.name = name;
    }

    @JsonCreator
    public static ResourceTypeId of(@JsonProperty("name") String name) {
        return new ResourceTypeId(name);
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
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
        ResourceTypeId other = (ResourceTypeId) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public int compareTo(ResourceTypeId other) {
        if (other == null) return +1;
        return name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return name;
    }

}
