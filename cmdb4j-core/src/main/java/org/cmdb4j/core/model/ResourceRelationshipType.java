package org.cmdb4j.core.model;

public class ResourceRelationshipType {
    public final String name;
    public final String inverseName;
    
    public ResourceRelationshipType(String name, String inverseName) {
        this.name = name;
        this.inverseName = inverseName;
    }

    public ResourceRelationshipType inv() {
        return new ResourceRelationshipType(inverseName, name);
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
        ResourceRelationshipType other = (ResourceRelationshipType) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    
}