package org.cmdb4j.core.env;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.helper.FxObjValueHelper;
import fr.an.fxtree.impl.helper.FxReplaceNodeCopyVisitor;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;
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
 *         (prefix by "envName/")             |                   |                    | build Resource from FxNode
 *                |                           |                   |                    |
 *               \/                           \/                 \/                    \/
 *  scan files --+--->  rawTemplateRootNode --+-->  rawRootNode --+-->   rootNode  ----+---->  ResourceRepository
 *  yaml/json             FxNode with #{param}       FxNode               FxNode                  Map<Id,Resource>
 *                           and "@fx-eval"          with "@fx-eval"
 * </PRE>
 */
public class EnvResourceTreeRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(EnvResourceTreeRepository.class);
    
    private static final String CTX_EnvResourceTreeRepository = "EnvResourceTreeRepository"; 
    
    private final String envName;

    private Object lock = new Object();
    
    private FxNode rawTemplateRootNode;
    private EnvTemplateInstanceParameters templateParams;

    private FxNode rawRootNode; // = rawTemplateRootNode with replaced templateParams  
    
    private List<String> phases = new ArrayList<>(ImmutableList.of("phase0"));
    private FxNodeFuncRegistry funcRegistry;
    private Map<String,Object> registerCtxVars = new HashMap<>(); 
    private FxNode rootNode; // = rawRootNode with evaluated funcs for "#phase0" .. 

    private ResourceTypeRepository resourceTypeRepository;
    
    /** built resources repository (=Map<Id,Resource>) for rootNode */
    private final ResourceRepository resourceRepository;
    
    // ------------------------------------------------------------------------

    public EnvResourceTreeRepository(String envName, File envDir,
            EnvTemplateInstanceParameters templateParams, 
            FxNode rawTemplateRootNode,
            FxNodeFuncRegistry funcRegistry,
            ResourceTypeRepository resourceTypeRepository) {
        this.envName = envName;
        this.templateParams = templateParams;
        this.rawTemplateRootNode = rawTemplateRootNode;
        this.funcRegistry = funcRegistry;
        this.resourceTypeRepository = resourceTypeRepository;
        this.resourceRepository = new ResourceRepository(resourceTypeRepository);
    }

    public static EnvResourceTreeRepository ctxGet(FxEvalContext ctx) {
        return (EnvResourceTreeRepository) ctx.lookupVariable(CTX_EnvResourceTreeRepository);
    }
    
    /**
     * eval Node + build Resources from node
     */
    public void init() {
        synchronized(lock) {
            reevalRawRootForTemplateParams();
            reevalRootNodeFromRawRootNode();
            reevalResourcesFromNodes();
        }
    }
    
    // ------------------------------------------------------------------------
    
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
    
    public void setTemplateParams(EnvTemplateInstanceParameters templateParams) {
        this.templateParams = templateParams;
        reevalRawRootForTemplateParams();
        onChangeCtxReevalResources();
    }
    
    public void putRegisterCtxVar(String key, Object value) {
        this.registerCtxVars.put(key, value);
    }
    
    public void onChangeCtxReevalResources() {
        synchronized(lock) {
            reevalRootNodeFromRawRootNode();
            reevalResourcesFromNodes();
        }
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
        ctx.putVariable(CTX_EnvResourceTreeRepository, this);
        ctx.putVariable("env", envName);
        ctx.putVariableAll(registerCtxVars);
        
        FxMemRootDocument processedDoc = new FxMemRootDocument();
        // *** eval rawRootNode -> rootNode  by evaluating recursive functions for "phase0", ... ***
        FxPhaseRecursiveEvalFunc.evalPhases(processedDoc.contentWriter(), phases, ctx, rawRootNode, funcRegistry);

        this.rootNode = processedDoc.getContent();
    }
    
    /**
     * recursive scan all nodes with <PRE>{id="", type="" ...}</PRE>
     */
    protected void reevalResourcesFromNodes() {
        // take snapshot of previously defined resource ids
        Map<ResourceId,Resource> remainingResources = new HashMap<>(resourceRepository.findAllAsMap());
        // scan (new/update) resources from tree node
        // => fx-tree library use FIELD_id="id", FIELD_type="type"
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
                resource.setObjData(objNode);  // => invalidate cached adapter(s) if any
            }
        });
        
        // step 2: remove previous resources not present any more after reeval
        if (! remainingResources.isEmpty()) {
            for(Resource remainingResource : remainingResources.values()) {
                resourceRepository.remove(remainingResource);
            }
        }
        
        Map<ResourceId,Resource> newResources = new HashMap<>(resourceRepository.findAllAsMap());
        
        // step 3/a: snapshot copy of getRequireResources() for all resources
        Map<ResourceId,Set<ResourceId>> prevResources_requireResourceIds = new HashMap<ResourceId,Set<ResourceId>>(); 
        Map<ResourceId,Set<ResourceId>> prevResources_subscribeResourceIds = new HashMap<ResourceId,Set<ResourceId>>(); 
        for(Resource resource : newResources.values()) {
            Set<ResourceId> prevRequireIds = new HashSet<>(resource.getRequireResources().keySet());
            prevResources_requireResourceIds.put(resource.getId(), prevRequireIds);   
            Set<ResourceId> prevSubscribeIds = new HashSet<>(resource.getSubscribeResources().keySet());
            prevResources_subscribeResourceIds.put(resource.getId(), prevSubscribeIds);   
        }
        
        // step 3/b : reeval parse requiredResources, subscribeResources, tags...
        for(Resource resource : newResources.values()) {
            FxObjNode objData = resource.getObjData();
            FxObjValueHelper objDataHelper = new FxObjValueHelper(objData);
            // parse resource requiredResources = [ otherId1, otherId2 .. ] 
            String[] requiredResourceIds = objDataHelper.getStringArrayOrNull(Resource.FIELD_requiredResources, true);
            if (requiredResourceIds != null && requiredResourceIds.length != 0) {
                for(String requiredResourceId : requiredResourceIds) {
                    Resource requiredResource = newResources.get(ResourceId.valueOf(requiredResourceId));
                    if (requiredResource != null) {
                        resource.addRequireResource(requiredResource);
                    } else {
                        LOG.warn("Resource '" + requiredResourceId + "' not found, referenced from '" + resource.getId() + "' " + Resource.FIELD_requiredResources + "[..]");
                    }
                }
            }
            // parse resource requiredResources = [ otherId1, otherId2 .. ] 
            String[] subscribeResourceIds = objDataHelper.getStringArrayOrNull(Resource.FIELD_subscribeResources, true);
            if (subscribeResourceIds != null && subscribeResourceIds.length != 0) {
                for(String subscribeResourceId : subscribeResourceIds) {
                    Resource subscribeResource = newResources.get(ResourceId.valueOf(subscribeResourceId));
                    if (subscribeResource != null) {
                        resource.addSubscribeResource(subscribeResource);
                    } else {
                        LOG.warn("Resource '" + subscribeResourceId + "' not found, referenced from '" + resource.getId() + "' " + Resource.FIELD_subscribeResources + "[..]");
                    }
                }
            }
            // parse tags
            List<String> newTags = objDataHelper.getStringListOrNull(Resource.FIELD_tags, true);
            Set<String> prevTags = resource.getTags();
            if (newTags != null && !newTags.isEmpty()) {
                resource.setAllTags(newTags);
            } else {
                if (! prevTags.isEmpty()) {
                    resource.clearTags();
                }
            }
        }
        // step 3/c : remove previous link resource->requiredResources/subscribeResources no more needed
        for(Resource resource : newResources.values()) {
            Set<ResourceId> prevRequireResourceIds = prevResources_requireResourceIds.get(resource.getId());
            if (prevRequireResourceIds == null) {
                // should not occur!
                prevRequireResourceIds = new HashSet<>();
            }
            Set<ResourceId> requireIdsToRemove = new HashSet<>();
            requireIdsToRemove.addAll(prevRequireResourceIds);
            requireIdsToRemove.removeAll(resource.getRequireResources().keySet());
            if (! requireIdsToRemove.isEmpty()) {
                for (ResourceId requireIdToRemove : requireIdsToRemove) {
                    resource.removeRequireResourceId(requireIdToRemove);
                }
            }
            
            Set<ResourceId> prevSubscribeResourceIds = prevResources_subscribeResourceIds.get(resource.getId());
            if (prevSubscribeResourceIds == null) {
                // should not occur!
                prevSubscribeResourceIds = new HashSet<>();
            }
            Set<ResourceId> subscribeIdsToRemove = new HashSet<>();
            subscribeIdsToRemove.addAll(prevSubscribeResourceIds);
            subscribeIdsToRemove.removeAll(resource.getSubscribeResources().keySet());
            if (! subscribeIdsToRemove.isEmpty()) {
                for (ResourceId subscribeIdToRemove : subscribeIdsToRemove) {
                    resource.removeSubscribeResourceId(subscribeIdToRemove);
                }
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
