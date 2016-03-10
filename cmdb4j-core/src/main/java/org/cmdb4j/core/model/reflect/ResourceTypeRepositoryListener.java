package org.cmdb4j.core.model.reflect;

@FunctionalInterface
public interface ResourceTypeRepositoryListener {

    public void onChange(ResourceTypeRepositoryChange change);
    
}
