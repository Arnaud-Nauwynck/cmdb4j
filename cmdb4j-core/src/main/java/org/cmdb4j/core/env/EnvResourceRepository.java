package org.cmdb4j.core.env;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
 *      env.yaml                              cloud1-instance1/
 *      env.json                                 template-param.yaml                 
 *      subDir1/
 *        env.yaml      
 * </PRE>
 * 
 * <p>
 * data flow transformation for defining Resources from Yaml/Json files: 
 * <PRE>
 *           replace Id              replace templateParams    Eval "phase0:"         Scan Resource Id+Type          Eval Rule Inference
 *         (prefix by "envName/")             |                   |                    | build Resource from FxNode   |
 *                |                           |                   |                    |                              |
 *               \/                           \/                 \/                    \/                             \/
 *  scan files --+--->  rawTemplateRootNode --+-->  rawRootNode --+-->   rootNode  ----+---->  ResourceRepository --------> ResourceRepository 
 *  yaml/json             FxNode with #{param}       FxNode               FxNode                  Map<Id,Resource>           Map<Id,Resource>
 *                           and "@fx-eval"          with "@fx-eval"                              explicit only              explicit + infered
 * </PRE>
 */
public class EnvResourceRepository {

	private static final Logger LOG = LoggerFactory.getLogger(EnvResourceRepository.class);

	private static final String CTX_EnvResourceTreeRepository = "EnvResourceTreeRepository";

	private final EnvDirsResourceRepositories owner;
	private final String envName;

	private Object lock = new Object();

	private FxNode rawTemplateRootNode;
	private EnvTemplateInstanceParameters templateParams;

	private FxNode rawRootNode; // = rawTemplateRootNode with replaced
								// templateParams

	private List<String> phases = new ArrayList<>(ImmutableList.of("phase0"));
	private FxNodeFuncRegistry funcRegistry;
	private Map<String, Object> registerCtxVars = new HashMap<>();
	private FxNode rootNode; // = rawRootNode with evaluated funcs for "#phase0"

	private ResourceTypeRepository resourceTypeRepository;

	/** built resources repository (=Map<Id,Resource>) for rootNode */
	private final ResourceRepository resourceRepository;

	// ------------------------------------------------------------------------

	public EnvResourceRepository(EnvDirsResourceRepositories owner, String envName, File envDir, EnvTemplateInstanceParameters templateParams,
			FxNode rawTemplateRootNode) {
		this.owner = owner;
		this.envName = envName;
		this.templateParams = templateParams;
		this.rawTemplateRootNode = rawTemplateRootNode;
		this.funcRegistry = owner.getFuncRegistry();
		this.resourceTypeRepository = owner.getResourceTypeRepository();
		this.resourceRepository = new ResourceRepository(resourceTypeRepository);
	}

	public static EnvResourceRepository ctxGet(FxEvalContext ctx) {
		return (EnvResourceRepository) ctx.lookupVariable(CTX_EnvResourceTreeRepository);
	}

	/**
	 * eval Node + build Resources from node
	 */
	public void init() {
		synchronized (lock) {
			reevalRawRootForTemplateParams();
			doReevalResources();
		}
	}

	protected void doReevalResources() {
		reevalRootNodeFromRawRootNode();
		reevalResourcesFromNodes();
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
		synchronized (lock) {
			doReevalResources();
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
					templateParams.getParameters());
			res = templateReplDoc.getContent();
		}
		this.rawRootNode = res;
	}

	private void reevalRootNodeFromRawRootNode() {
		// preprocess eval : recursive replace all "@eval-function" by their
		// invocation result
		FxEvalContext ctx = new FxEvalContext(null, funcRegistry);
		ctx.putVariable(CTX_EnvResourceTreeRepository, this);
		ctx.putVariable("env", envName);
		ctx.putVariableAll(registerCtxVars);

		FxMemRootDocument processedDoc = new FxMemRootDocument();
		// *** eval rawRootNode -> rootNode by evaluating recursive functions
		// for "phase0", ... ***
		FxPhaseRecursiveEvalFunc.evalPhases(processedDoc.contentWriter(), phases, ctx, rawRootNode, funcRegistry);

		this.rootNode = processedDoc.getContent();
	}

	
	public static class ResourceBuilder {
		Map<ResourceId, Resource> reusePrevResources;
		
		public ResourceBuilder(Map<ResourceId, Resource> reusePrevResources) {
			this.reusePrevResources = reusePrevResources;
		}
	
		public Resource updateOrCreateResource(ResourceId id, ResourceType type, FxObjNode objData) {
			Resource resource = reusePrevResources.get(id);
			if (resource != null) {
				// check if type changed (eventhough should not occur..)
				if (! resource.getType().equals(type)) {
					resource = null;
				}
			}
			if (resource == null) {
				// create
				resource = new Resource(id, type, objData);
			} else {
				// update
				resource.setObjData(objData); // => invalidate cached adapter(s) if any
			}
			return resource;
		}
	}
	
	/**
	 * recursive scan all nodes with
	 * 
	 * <PRE>
	 * {id="", type="" ...}
	 * </PRE>
	 */
	protected void reevalResourcesFromNodes() {
		// take snapshot of previously defined resource ids
		Map<ResourceId, Resource> prevResources = new HashMap<>(resourceRepository.findAllAsMap());
		Map<ResourceId, Resource> reusePrevResources = ImmutableMap.copyOf(prevResources);
		
		ResourceBuilder resourceBuilder = new ResourceBuilder(reusePrevResources);

		Map<ResourceId, Resource> explicitResources = new HashMap<>();

		// scan tree nodes for creating explicit resources (=> to add in resourceRepository, re-use if already created)
		// => fx-tree library use FIELD_id="id", FIELD_type="type"
		FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(rootNode, (id, typeName, objData) -> {
			ResourceId resourceId = ResourceId.valueOf(id);
			ResourceType type = resourceTypeRepository.getOrCreateType(typeName);
			Resource resource = resourceBuilder.updateOrCreateResource(resourceId, type, objData);
			explicitResources.put(resourceId, resource);
		});
		
		// run inference engine rules, to add implicit resources
		Map<ResourceId, Resource> allResources = runInferenceEngineResourceRules(explicitResources, resourceBuilder);
		
		// finish resolve cross resource relationships
		resolveStdNodeRelationshipsToResourceRelationships(allResources);

		// update resourceRepository with added/updated/removed resources 
		// remove previous resources not present any more after reeval
		updateRepositoryAllResources(allResources);
		
	}

	private void updateRepositoryAllResources(Map<ResourceId, Resource> allResources) {
		Map<ResourceId, Resource> resourceToRemoves = new HashMap<>(resourceRepository.findAllAsMap());
		for(Resource resource : allResources.values()) {
			resourceToRemoves.remove(resource.getId());
		}
		if (!resourceToRemoves.isEmpty()) {
			for (Resource resourceToRemove : resourceToRemoves.values()) {
				resourceRepository.remove(resourceToRemove);
			}
		}
		// check for type modified resources, and added resources
		for(Resource resource : allResources.values()) {
			ResourceId id = resource.getId();
			Resource prev = resourceRepository.findById(id);
			if (prev != null) {
				if (prev.getType().equals(resource.getType())) {
					// type mutated.. remove + add!
					resourceRepository.remove(prev);
					resourceRepository.add(resource);
				}
			} else {
				resourceRepository.add(resource);
			}
		}
	}

	private Map<ResourceId, Resource> runInferenceEngineResourceRules(Map<ResourceId, Resource> explicitResources, ResourceBuilder resourceBuilder) {
        Map<ResourceId, Resource> res = new HashMap<>();
        KieSession ksession = owner.buildCmdbResourceInferenceSession();
        if (ksession == null) {
        	return explicitResources;
        }
        try {
	        // put globals
	        Globals kGlobals = ksession.getGlobals();
	        // kGlobals.set("envResourceRepository", this); // should be useless .. try to eval without side-effect!
	        // kGlobals.set("resourceRepository", resourceRepository);        
	        kGlobals.set("resourceTypeRepository", resourceTypeRepository);
	        Map<ResourceId, Resource> resourceById = new HashMap<>(explicitResources);
	        kGlobals.set("resourceById", resourceById);
	        kGlobals.set("resourceBuilder", resourceBuilder); 
	        
	        // put all explicit resources
	        for(Resource obj : explicitResources.values()) {
	        	ksession.insert(obj);
	        }
	        
	        // *** the Biggy : compute rules ***
	        LOG.info("fireAllRules");
	        ksession.fireAllRules();
	
	        Collection<?> resultObjects = new ArrayList<>(ksession.getObjects());
	        LOG.info("objects after inference rules:" + resultObjects.size());
	        
	        // determine implicit resources to add
	        for(Object obj : resultObjects) {
	        	if (obj instanceof Resource) {
	        		Resource resObj = (Resource) obj;
	        		res.put(resObj.getId(), resObj);
					if (!explicitResources.containsKey(resObj.getId())) {
						LOG.info("implicit resources after inference rules:" + resObj);
					}
	        	}
	        }
	        
        } finally {
        	ksession.dispose();
        }
        return res;
	}

	private void resolveStdNodeRelationshipsToResourceRelationships(Map<ResourceId, Resource> resources) {

		// snapshot copy of getRequireResources() for all resources
		Map<ResourceId, Set<ResourceId>> prevResources_requireResourceIds = new HashMap<ResourceId, Set<ResourceId>>();
		Map<ResourceId, Set<ResourceId>> prevResources_subscribeResourceIds = new HashMap<ResourceId, Set<ResourceId>>();
		for (Resource resource : resources.values()) {
			Set<ResourceId> prevRequireIds = new HashSet<>(resource.getRequireResources().keySet());
			prevResources_requireResourceIds.put(resource.getId(), prevRequireIds);
			Set<ResourceId> prevSubscribeIds = new HashSet<>(resource.getSubscribeResources().keySet());
			prevResources_subscribeResourceIds.put(resource.getId(), prevSubscribeIds);
		}

		// reeval parse requiredResources, subscribeResources, tags...
		for (Resource resource : resources.values()) {
			FxObjNode objData = resource.getObjData();
			FxObjValueHelper objDataHelper = new FxObjValueHelper(objData);
			// parse resource requiredResources = [ otherId1, otherId2 .. ]
			String[] requiredResourceIds = objDataHelper.getStringArrayOrNull(Resource.FIELD_requiredResources, true);
			if (requiredResourceIds != null && requiredResourceIds.length != 0) {
				for (String requiredResourceId : requiredResourceIds) {
					Resource requiredResource = resources.get(ResourceId.valueOf(requiredResourceId));
					if (requiredResource != null) {
						resource.addRequireResource(requiredResource);
					} else {
						LOG.warn("Resource '" + requiredResourceId + "' not found, referenced from '" + resource.getId()
								+ "' " + Resource.FIELD_requiredResources + "[..]");
					}
				}
			}
			// parse resource subscribeResources = [ otherId1, otherId2 .. ]
			String[] subscribeResourceIds = objDataHelper.getStringArrayOrNull(Resource.FIELD_subscribeResources, true);
			if (subscribeResourceIds != null && subscribeResourceIds.length != 0) {
				for (String subscribeResourceId : subscribeResourceIds) {
					Resource subscribeResource = resources.get(ResourceId.valueOf(subscribeResourceId));
					if (subscribeResource != null) {
						resource.addSubscribeResource(subscribeResource);
					} else {
						LOG.warn("Resource '" + subscribeResourceId + "' not found, referenced from '"
								+ resource.getId() + "' " + Resource.FIELD_subscribeResources + "[..]");
					}
				}
			}
			// parse tags
			List<String> newTags = objDataHelper.getStringListOrNull(Resource.FIELD_tags, true);
			Set<String> prevTags = resource.getTags();
			if (newTags != null && !newTags.isEmpty()) {
				resource.setAllTags(newTags);
			} else {
				if (!prevTags.isEmpty()) {
					resource.clearTags();
				}
			}
		}
		// remove previous link
		// resource->requiredResources/subscribeResources no more needed
		for (Resource resource : resources.values()) {
			Set<ResourceId> prevRequireResourceIds = prevResources_requireResourceIds.get(resource.getId());
			if (prevRequireResourceIds == null) {
				// should not occur!
				prevRequireResourceIds = new HashSet<>();
			}
			Set<ResourceId> requireIdsToRemove = new HashSet<>();
			requireIdsToRemove.addAll(prevRequireResourceIds);
			requireIdsToRemove.removeAll(resource.getRequireResources().keySet());
			if (!requireIdsToRemove.isEmpty()) {
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
			if (!subscribeIdsToRemove.isEmpty()) {
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
		return envName + " (from '" + templateParams.getSourceTemplateName() + "' with params: "
				+ templateParams.getParameters() + ")";
	}

}
