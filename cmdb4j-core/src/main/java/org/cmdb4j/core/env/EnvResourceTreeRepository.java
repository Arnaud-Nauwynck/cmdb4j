package org.cmdb4j.core.env;

import org.cmdb4j.core.model.ResourceRepository;

import fr.an.fxtree.model.FxNode;

/**
 * holder for Resource(s) of an environment, based on a preprocessed node tree
 */
public class EnvResourceTreeRepository {

    private final EnvDirsResourceTreeRepository parent;
    
    private final String envName;

    private final EnvTemplateInstanceParameters templateParams;

    private FxNode rootNode;
    
    /** built resource repository (=Map<Id,Resource>) for rootNode */
    private final ResourceRepository resourceRepository;

    // ------------------------------------------------------------------------

    public EnvResourceTreeRepository(EnvDirsResourceTreeRepository parent, String envName, 
            EnvTemplateInstanceParameters templateParams, 
            FxNode rootNode,
            ResourceRepository resourceRepository) {
        this.parent = parent;
        this.envName = envName;
        this.templateParams = templateParams;
        this.rootNode = rootNode;
        this.resourceRepository = resourceRepository;
    }

    // ------------------------------------------------------------------------

    public EnvDirsResourceTreeRepository getParent() {
        return parent;
    }
    
    public String getEnvName() {
        return envName;
    }

    public FxNode getRootNode() {
        return rootNode;
    }

    public ResourceRepository getResourceRepository() {
        return resourceRepository;
    }
    
    public EnvTemplateInstanceParameters getTemplateParams() {
        return templateParams;
    }


    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        if (templateParams == null) {
            return envName;
        }
        return envName + " (from '" + templateParams.getTemplateSourceEnvName() + "' with params: " + templateParams.getTemplateParameters() + ")";
    }

}
