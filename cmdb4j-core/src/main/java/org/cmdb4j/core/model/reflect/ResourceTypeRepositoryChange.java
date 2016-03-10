package org.cmdb4j.core.model.reflect;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;

import fr.an.dynadapter.alt.IAdapterAlternativeFactory;

/**
 * Event class hierarchy for change elements in ResourceType repository:
 * <PRE>
 *                       +------------------------------+
 *                       | ResourceTypeRepositoryChange |   <------------------------------------                                                   
 *                       | -repository                  |                                         \
 *                       +------------------------------+                                         |
 *                                     /\                                                         |
 *                                     |                                                          |
 *                     +-----------------------------------+----------------------------+         |
 *                     |                                   |                            |         |
 *      +----------------------------+  +---------------------------------+   +-----------------+ |
 *      | AbstractResourceTypeChange |  | ResourceAdapterFactoryChange    |   |Composite..Change| /
 *      | - resourceType             |  | - added/removed AdapterFactory  |   | - elements    --|-
 *      +----------------------------+  | - adapteableBaseResourceType    |   +-----------------+
 *                    /\                +---------------------------------+
 *                    |
 *         +--------------------------------+--------------------------+
 *         |                                |                          |
 * +-----------------------+  +--------------------------+ +-----------------------------+     
 * |ResourceTypeDeclChange |  | ResourceTypeFieldsChange | | ResourceTypeHierarchyChange |   
 * | - addedOrRemoved      |  | - previous/new superType | | - previous/new fields       |
 * +-----------------------+  +--------------------------+ | - previous/new interfaces   |
 *                                                          +-----------------------------+
 * </PRE>
 * 
 * thread safety: immutable sub-classes, temporary object not to be kept by listeners
 */
public abstract class ResourceTypeRepositoryChange {
    
    protected final ResourceTypeRepository resourceTypeRepository;

    protected ResourceTypeRepositoryChange(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
    }

    public ResourceTypeRepository getResourceTypeRepository() {
        return resourceTypeRepository;
    }

    // build utility methods for sub-classes constructors
    // ------------------------------------------------------------------------
    
    public static ResourceTypeDeclChange newResourceTypeDeclChange(ResourceType type, boolean added) {
        return new ResourceTypeDeclChange(type, added);
    }
    
    public static ResourceTypeFieldsChange newResourceTypeFieldsChange(ResourceType target, Collection<ResourceFieldDef> previousFields, Collection<ResourceFieldDef> newFields) {
        return new ResourceTypeFieldsChange(target, previousFields, newFields);
    }
    
    public static ResourceTypeFieldsChange newResourceTypeFieldsChange(ResourceType target, ResourceFieldDef previousFields, ResourceFieldDef newFields) {
        return new ResourceTypeFieldsChange(target, singletonOrEmptyList(previousFields), singletonOrEmptyList(newFields));
    }
    
    protected static <T> List<T> singletonOrEmptyList(T elt) {
        return (elt != null)? Collections.singletonList(elt) : Collections.emptyList();
    }
    
    public static ResourceTypeHierarchyChange newResourceTypeHierarchyChange(ResourceType target, 
            ResourceType previousSuperType, ResourceType newSuperType,
            Collection<ResourceType> previousInterfaces, Collection<ResourceType> newInterfaces) {
        return new ResourceTypeHierarchyChange(target, previousSuperType, newSuperType, previousInterfaces, newInterfaces);
    }
    
    public static ResourceAdapterFactoryChange newResourceAdapterFactoryChange(IAdapterAlternativeFactory prevAdapterFactory, IAdapterAlternativeFactory newAdapterFactory,
            ResourceType adaptableBaseResourceType) {
        return new ResourceAdapterFactoryChange(prevAdapterFactory, newAdapterFactory, adaptableBaseResourceType);
    }
    
    public static CompositeResourceTypeRepositoryChange newCompositeResourceTypeRepositoryChange(List<ResourceTypeRepositoryChange> elements) {
        return new CompositeResourceTypeRepositoryChange(elements);
    }
    
    // ------------------------------------------------------------------------

    /**
     * abstract base class for change on ResourceType
     * 
     * thread safety: immutable sub-classes, temporary object not to be kept by listeners
     */
    public static abstract class AbstractResourceTypeChange extends ResourceTypeRepositoryChange {
        
        protected final ResourceType target;

        protected AbstractResourceTypeChange(ResourceType target) {
            super(target.getOwner());
            this.target = target;
        }

        public ResourceType getTarget() {
            return target;
        }

        @Override
        public String toString() {
            return "ResourceTypeChange [target=" + target + "]";
        }
        
    }

    /**
     * Change event for fields change (added/updated/removed) on a ResourceType
     * 
     * thread safety: immutable class, temporary object not to be kept by listeners
     */
    public static class ResourceTypeDeclChange extends AbstractResourceTypeChange {
        
        private final boolean addedOrRemoved;

        public ResourceTypeDeclChange(ResourceType target, boolean addedOrRemoved) {
            super(target);
            this.addedOrRemoved = addedOrRemoved;
        }

        @Override
        public String toString() {
            return "ResourceTypeDeclChange [addedOrRemoved=" + addedOrRemoved + "]";
        }
        
    }
    
    
    /**
     * Change event for fields change (added/updated/removed) on a ResourceType
     * 
     * thread safety: immutable class, temporary object not to be kept by listeners
     */
    public static class ResourceTypeFieldsChange extends AbstractResourceTypeChange {
        
        protected final ImmutableList<ResourceFieldDef> previousFields;
        protected final ImmutableList<ResourceFieldDef> newFields;

        protected ResourceTypeFieldsChange(ResourceType target, 
                Collection<ResourceFieldDef> previousFields, Collection<ResourceFieldDef> newFields) {
            super(target);
            this.previousFields = ImmutableList.copyOf(previousFields);
            this.newFields = ImmutableList.copyOf(newFields);
        }
        
        public ImmutableList<ResourceFieldDef> getPreviousFields() {
            return previousFields;
        }

        public ImmutableList<ResourceFieldDef> getNewFields() {
            return newFields;
        }

        @Override
        public String toString() {
            return "ResourceFieldsChange [target=" + target + ", fields prev:" + previousFields + ", new:" + newFields + "]";
        }
        
    }
    
    
    /**
     * Change event for super type/ super interfaces change (added/removed) on a ResourceType
     * 
     * thread safety: immutable class, temporary object not to be kept by listeners 
     */
    public static class ResourceTypeHierarchyChange extends AbstractResourceTypeChange {

        private final ResourceType previousSuperType;
        private final ResourceType newSuperType;
        
        protected final ImmutableList<ResourceType> previousInterfaces;
        protected final ImmutableList<ResourceType> newInterfaces;
        
        protected ResourceTypeHierarchyChange(ResourceType target, 
                ResourceType previousSuperType, ResourceType newSuperType,
                Collection<ResourceType> previousInterfaces, Collection<ResourceType> newInterfaces
                ) {
            super(target);
            this.previousSuperType = previousSuperType;
            this.newSuperType = newSuperType;
            this.previousInterfaces = ImmutableList.copyOf(previousInterfaces);
            this.newInterfaces = ImmutableList.copyOf(newInterfaces);
        }

        public ResourceType getPreviousSuperType() {
            return previousSuperType;
        }

        public ResourceType getNewSuperType() {
            return newSuperType;
        }
        
        public ImmutableList<ResourceType> getPreviousInterfaces() {
            return previousInterfaces;
        }

        public ImmutableList<ResourceType> getNewInterfaces() {
            return newInterfaces;
        }

        @Override
        public String toString() {
            return "ResourceSuperTypeChange [" + target + 
                    ", superType previous: " + previousSuperType + ", new:" + newSuperType + "]";
        }
    }
    
    /**
     * Change event for AdapterFactory added/remove on an adaptable ResourceType(so applicable for all sub-classes of adaptable..)
     * 
     * thread safety: immutable class, temporary object not to be kept by listeners
     */
    public static class ResourceAdapterFactoryChange extends ResourceTypeRepositoryChange {
        
        protected final IAdapterAlternativeFactory prevAdapterFactory;
        protected final IAdapterAlternativeFactory newAdapterFactory;
        protected final ResourceType adaptableBaseResourceType;

        protected ResourceAdapterFactoryChange(
                IAdapterAlternativeFactory prevAdapterFactory, IAdapterAlternativeFactory newAdapterFactory,
                ResourceType adaptableBaseResourceType) {
            super(adaptableBaseResourceType.getOwner());
            this.prevAdapterFactory = prevAdapterFactory;
            this.newAdapterFactory = newAdapterFactory;
            this.adaptableBaseResourceType = adaptableBaseResourceType;
        }
        
        public IAdapterAlternativeFactory getPrevAdapterFactory() {
            return prevAdapterFactory;
        }

        public IAdapterAlternativeFactory getNewAdapterFactory() {
            return newAdapterFactory;
        }

        public ResourceType getAdaptableBaseResourceType() {
            return adaptableBaseResourceType;
        }


        @Override
        public String toString() {
            return "ResourceAdapterFactoryChange [adaptableBaseResourceType=" + adaptableBaseResourceType
                    + ", adapterFactory prev:" + prevAdapterFactory + ", new:" + newAdapterFactory + "]";
        }
        
    }
    
    /**
     * composite (design-pattern) for ResourceTypeRepositoryChange  
     * 
     * thread safety: immutable class, temporary object not to be kept by listeners
    */
    public static class CompositeResourceTypeRepositoryChange extends ResourceTypeRepositoryChange {
        
        private final ImmutableList<ResourceTypeRepositoryChange> elements;

        public CompositeResourceTypeRepositoryChange(List<ResourceTypeRepositoryChange> elements) {
            super(elements.get(0).resourceTypeRepository);
            this.elements = ImmutableList.copyOf(elements);
        }

        public ImmutableList<ResourceTypeRepositoryChange> getElements() {
            return elements;
        }

        @Override
        public String toString() {
            return "CompositeRepositoryChange [elements=" + elements + "]";
        }
        
    }
    
}