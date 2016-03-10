package org.cmdb4j.core.model.reflect;

public abstract class ResourceTypeRepositoryChange {
    private final ResourceTypeRepository owner;

    protected ResourceTypeRepositoryChange(ResourceTypeRepository owner) {
        this.owner = owner;
    }

    public ResourceTypeRepository getOwner() {
        return owner;
    }
    
}