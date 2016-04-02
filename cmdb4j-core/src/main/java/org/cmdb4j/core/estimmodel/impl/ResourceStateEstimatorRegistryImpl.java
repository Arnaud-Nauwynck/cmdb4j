package org.cmdb4j.core.estimmodel.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.estimmodel.ControlToEstimatedResource;
import org.cmdb4j.core.estimmodel.IResourceStateEstimatorProvider;
import org.cmdb4j.core.estimmodel.ResourceStateEstimatorRegistry;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

import fr.an.fxtree.model.FxObjNode;

/**
 * 
 */
public class ResourceStateEstimatorRegistryImpl implements ResourceStateEstimatorRegistry {

    protected ResourceTypeRepository typeRepository;
    
    protected ResourceRepository controlResourceRepository;
    
    protected ResourceRepository estimatedResourceRepository;
    
    protected FxObjNode estimatedStorageNode;

    protected Map<ResourceId,ControlToEstimatedResource> mappingResources = new HashMap<>();
    

    // ------------------------------------------------------------------------
    
    public ResourceStateEstimatorRegistryImpl(
            ResourceTypeRepository typeRepository,
            ResourceRepository controlResourceRepository, 
            ResourceRepository estimatedResourceRepository,
            FxObjNode estimatedStorageNode
            ) {
        this.typeRepository = typeRepository;
        this.controlResourceRepository = controlResourceRepository;
        this.estimatedResourceRepository = estimatedResourceRepository;
        this.estimatedStorageNode = estimatedStorageNode;
    }
    
    // ------------------------------------------------------------------------

    public void init() {
        List<Resource> controlResources = controlResourceRepository.findAll();
        Map<ResourceId,Resource> estimatedResources = Resource.lsToIdMap(estimatedResourceRepository.findAll());
        for(Resource controlResource : controlResources) {
            ResourceId id = controlResource.getId();
            // build corresponding resource estimated state (empty)
            Resource estimatedResource = estimatedResources.get(id);
            if (estimatedResource == null) {
                estimatedResource = buildEmptyEstimatedResource(controlResource);
                estimatedResources.put(id, estimatedResource);
            }
            // lookup or create mapping association
            ControlToEstimatedResource mapping = mappingResources.get(id);
            if (mapping == null) {
                mapping = new ControlToEstimatedResource(controlResource, estimatedResource, this);
                mappingResources.put(id, mapping);
            }
            // update mapping
            // TODO..
            
            Map<String, IResourceStateEstimatorProvider> estimatorProviders = typeRepository.getAdapterManager().getAdapters(controlResource, IResourceStateEstimatorProvider.ITF_ID);
            for(Map.Entry<String, IResourceStateEstimatorProvider> estimatorProviderEntry : estimatorProviders.entrySet()) {
                IResourceStateEstimatorProvider estimatorProvider = estimatorProviderEntry.getValue();
                Object estimator = estimatorProvider.createResourceEstimator(mapping);
                
            }
        }
        // (for incremental update) find all ControlToEstimatedResource not in control 
        // purge...
    }

    private Resource buildEmptyEstimatedResource(Resource controlResource) {
        ResourceId id = controlResource.getId();
        ResourceType type = controlResource.getType();
        FxObjNode resObj = estimatedStorageNode.putObj(id.toString());
        resObj.putObj("@estimated-fields");
        Resource res = new Resource(id, type, resObj);
        return res;
    }
    
}
