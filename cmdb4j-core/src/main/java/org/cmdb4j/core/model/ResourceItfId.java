package org.cmdb4j.core.model;

import java.io.Serializable;

import org.cmdb4j.core.util.CmdbAssertUtils;

import fr.an.dynadapter.alt.ItfId;

/**
 * immutable pair for [ResourceId, ItfId]
 *
 */
public final class ResourceItfId implements Comparable<ResourceItfId>, Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    private final ResourceId resourceId;
    
    private final ItfId<?> itfId;
    
    // ------------------------------------------------------------------------
    
    public ResourceItfId(ResourceId resourceId, ItfId<?> itfId) {
        CmdbAssertUtils.checkNotNull(resourceId);
        CmdbAssertUtils.checkNotNull(itfId);
        this.resourceId = resourceId;
        this.itfId = itfId;
    }

    // ------------------------------------------------------------------------
    
    public ResourceId getResourceId() {
        return resourceId;
    }

    public ItfId<?> getItfId() {
        return itfId;
    }

    @Override
    public int hashCode() {
        return ((itfId == null) ? 0 : itfId.hashCode()) ^ ((resourceId == null) ? 0 : resourceId.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResourceItfId other = (ResourceItfId) obj;
        if (itfId == null) {
            if (other.itfId != null)
                return false;
        } else if (!itfId.equals(other.itfId))
            return false;
        if (resourceId == null) {
            if (other.resourceId != null)
                return false;
        } else if (!resourceId.equals(other.resourceId))
            return false;
        return true;
    }
    
    @Override
    public int compareTo(ResourceItfId other) {
        int res = 0;
        if (resourceId != null && other.resourceId != null) {
            res = resourceId.compareTo(other.resourceId);
        } else {
            res = (resourceId == null)? ((other.resourceId != null)? +1 : 0) : 0;
        }
        if (res == 0) {
            if (itfId != null && other.itfId != null) {
                res = itfId.compareTo(other.itfId);
            } else {
                res = (itfId == null)? ((other.itfId != null)? +1 : 0) : 0;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        return "[" + resourceId + " - " + itfId + "]";
    }
    
}
