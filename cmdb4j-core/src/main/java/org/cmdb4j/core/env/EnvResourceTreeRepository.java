package org.cmdb4j.core.env;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

import fr.an.fxtree.impl.helper.FxMemoizedFileStoreFuncHelper;
import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.helper.FxPendingJobsFileStoreHelper;
import fr.an.fxtree.impl.helper.FxReplaceNodeCopyVisitor;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxEvalContext;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;

/**
 * holder for Resource(s) of an environment, based on a processable node tree
 *
 * <p>
 * typical file system layout for standard env ... or from templatized env 
 * <PRE>
 * baseEnvsDirs/                          baseEnvsDirs/
 *   env123/                                cloud/
 *      .memoized-store.yaml                   cloud1-instance1/
 *      .pending-jobs-store.yaml                  .memoized-store.yaml                 
 *      env.yaml                                  .pending-jobs-store.yaml
 *      env.json                                  template-param.yaml
 *      subDir1/
 *        env.yaml      
 * </PRE>
 * 
 * <p>
 * data flow transformation for defining Resources from Yaml/Json files: 
 * <PRE>
 *           replace Id              replace templateParams    Eval "phase0:"         Scan Resource Id+Type
 *         (prefix by "envName/")             |                   | use memoize store  | build Resource from FxNode
 *                |                           |                   |   + pending jobs   |
 *               \/                           \/                 \/                    \/
 *  scan files --+--->  rawTemplateRootNode --+-->  rawRootNode --+-->   rootNode  ----+---->  ResourceRepository
 *  yaml/json             FxNode with #{param}       FxNode               FxNode                  Map<Id,Resource>
 *                           and "@fx-eval"          with "@fx-eval"
 * </PRE>
 */
public class EnvResourceTreeRepository {

    protected static final String DEFAULT_MEMOIZED_STORE_FILENAME = ".memoized-store.yaml";
    protected static final String DEFAULT_PENDING_JOBS_STORE_FILENAME = ".pending-jobs-store.yaml";
    
    private final EnvDirsResourceTreeRepository parent;
    
    private final String envName;

    private FxNode rawTemplateRootNode;
    private EnvTemplateInstanceParameters templateParams;

    private FxNode rawRootNode; // = rawTemplateRootNode with replaced templateParams  
    
    private FxNodeFuncRegistry funcRegistry;
    private FxNode rootNode; // = rawRootNode with evaluated funcs for "#phase0" .. 

    private ResourceTypeRepository resourceTypeRepository;
    
    /** built resources repository (=Map<Id,Resource>) for rootNode */
    private final ResourceRepository resourceRepository;

    private FxPendingJobsFileStoreHelper pendingJobsFileStoreHelper;
    private FxMemoizedFileStoreFuncHelper memoizedFileStoreFuncHelper;
    
    // ------------------------------------------------------------------------

    public EnvResourceTreeRepository(EnvDirsResourceTreeRepository parent, String envName, File envDir,
            EnvTemplateInstanceParameters templateParams, 
            FxNode rawTemplateRootNode) {
        this.parent = parent;
        this.envName = envName;
        this.templateParams = templateParams;
        this.rawTemplateRootNode = rawTemplateRootNode;
        
        File pendingJobsFile = new File(envDir, DEFAULT_PENDING_JOBS_STORE_FILENAME);
        this.pendingJobsFileStoreHelper = new FxPendingJobsFileStoreHelper(pendingJobsFile);
        File memoizedFileStore = new File(envDir, DEFAULT_MEMOIZED_STORE_FILENAME);
        this.memoizedFileStoreFuncHelper = new FxMemoizedFileStoreFuncHelper(memoizedFileStore, pendingJobsFileStoreHelper);
        
        this.funcRegistry = parent.getFuncRegistry();
        this.resourceTypeRepository = parent.getResourceTypeRepository();
        this.resourceRepository = new ResourceRepository(resourceTypeRepository);
    }

    /**
     * eval Node + build Resources from node
     */
    public void init() {
        reevalRawRootForTemplateParams();
        reevalRootNodeFromRawRootNode();
        reevalResourcesFromNodes();
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
    
    public FxPendingJobsFileStoreHelper getPendingJobsFileStoreHelper() {
        return pendingJobsFileStoreHelper;
    }
    
    public FxMemoizedFileStoreFuncHelper getMemoizedFileStoreFuncHelper() {
        return memoizedFileStoreFuncHelper;
    }

    public void setTemplateParams(EnvTemplateInstanceParameters templateParams) {
        this.templateParams = templateParams;
        reevalRawRootForTemplateParams();
    }
    
    
    
    // ------------------------------------------------------------------------
    
    private void reevalRawRootForTemplateParams() {
        // replace template parameters
        FxNode res;
        if (templateParams == null) {
            res = rawTemplateRootNode;
        } else {
            FxMemRootDocument templateReplDoc = new FxMemRootDocument();
            FxReplaceNodeCopyVisitor.copyWithReplaceTo(templateReplDoc.contentWriter(), rawTemplateRootNode, 
                templateParams.getTemplateParameters());
            res = templateReplDoc.getContent();
        }
        this.rawRootNode = res;
    }

    private void reevalRootNodeFromRawRootNode() {
        // preprocess eval : recursive replace all "@eval-function" by their invocation result
        FxEvalContext ctx = new FxEvalContext(null, funcRegistry);
        ctx.putVariable("EnvResourceTreeRepository", this);
        ctx.putVariable("env", envName);
        ctx.putVariable("FxMemoizedFileStoreFuncHelper", memoizedFileStoreFuncHelper);
        
        FxPhaseRecursiveEvalFunc phase0Func = new FxPhaseRecursiveEvalFunc("phase0", funcRegistry);
        FxMemRootDocument processedDoc = new FxMemRootDocument();
        phase0Func.eval(processedDoc.contentWriter(), ctx, rawRootNode);

        this.rootNode = processedDoc.getContent();
    }
    
    /**
     * recursive scan all nodes with <PRE>{id="", type="" ...}</PRE>
     */
    protected void reevalResourcesFromNodes() {
        Map<ResourceId,Resource> remainingResources = new HashMap<>(resourceRepository.findAllAsMap());
        // scan resources from tree node
        FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(rootNode, (id, typeName, objNode) -> {
            ResourceId resourceId = ResourceId.valueOf(id);
            Resource resource = remainingResources.remove(resourceId);
            if (resource != null) {
                // check if type changed (eventhough should not occur..)
                if (resource.getType().getName().equals(typeName)) {
                    resourceRepository.remove(resource); // remove then re-create!
                    resource = null;
                }
            }
            if (resource == null) {
                // create
                ResourceType type = resourceTypeRepository.getOrCreateType(typeName);
                resource = new Resource(resourceId, type, objNode);
                resourceRepository.add(resource);
            } else {
                // update
                resource.setObjData(objNode);
                // TODO .. resourceRepository.update  ... invalidate adapter if cached
            }
            // TODO ... reeval parse requiredResources, subscribeResources, tags...
        });
        // step 2: remove previous resources not present any more after reeval
        if (! remainingResources.isEmpty()) {
            for(Resource remainingResource : remainingResources.values()) {
                resourceRepository.remove(remainingResource);
            }
        }
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
