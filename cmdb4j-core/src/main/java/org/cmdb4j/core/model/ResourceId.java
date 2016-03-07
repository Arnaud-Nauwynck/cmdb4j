package org.cmdb4j.core.model;

import org.cmdb4j.core.util.CmdbAssertUtils;
import org.cmdb4j.core.util.PathId;

/**
 * Id for a Resource element
 */
public final class ResourceId implements Comparable<ResourceId> {

    private final PathId id;

    // ------------------------------------------------------------------------
    
    public ResourceId(PathId id) {
        CmdbAssertUtils.checkNotNull(id);
        this.id = id;
    }

    // ------------------------------------------------------------------------

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
        ResourceId other = (ResourceId) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public int compareTo(ResourceId other) {
        if (other == null) return +1;
        int res = id.compareTo(other.id);
        return res;
    }
    
    @Override
    public String toString() {
        return id.toString();
    }
    
}
