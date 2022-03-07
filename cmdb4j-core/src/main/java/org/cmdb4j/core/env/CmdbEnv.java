package org.cmdb4j.core.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.cmdb4j.core.env.input.ResourceFileContent;
import org.cmdb4j.core.env.prototype.FxObjNodePrototypeRegistry;
import org.cmdb4j.core.env.template.EnvTemplateInstanceParameters;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.store.IEnvValueDecrypter;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.an.fxtree.format.yaml.FxYamlUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.helper.FxObjValueHelper;
import fr.an.fxtree.impl.helper.FxReplaceNodeCopyVisitor;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.impl.stdfunc.FxCurrEvalCtxUtil;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.model.FxArrayNode;
import fr.an.fxtree.model.FxChildWriter;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;
import fr.an.fxtree.model.FxTextNode;
import fr.an.fxtree.model.func.FxEvalContext;
import fr.an.fxtree.model.func.FxNodeFunc;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;
import lombok.val;

/**
 * holder for Resource(s) of an environment, based on a processable node tree
 *
 * <p>
 * typical file system layout for standard env ... or from templatized env
 * 
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
 * 
 * <PRE>
 *           replace Id               replace           Pre-Process        Eval "phase0:"         Scan Resource Id+Type          
 *         (prefix by "envName/")      templateParams    (Prototype..)         |                    | build Resource from FxNode
 *                |                           |             |                  |                    |
 *               \/                           \/            \/                 \/                    \/
 *  scan files --+--->  rawTemplateRootNode --+---> rawNode -+--> rawRootNode --+-->   rootNode  ----+---->  ResourceRepository  -- ..
 *  yaml/json             FxNode with #{param}                    FxNode               FxNode                  Map<Id,Resource>   
 *                          and prototype                         with "@fx-eval"                              explicit only      
 *                          and "@fx-eval"
 *
 *
 * repeated at reload until fully infered/discovered
 * 
 *       Eval Rules Inference(*)      Expand Rule(*)         Discovery(*)                   
 *         |                               |                    |
 *         |                               |                    |
 *        \/                              \/                    \/
 *     --------> ResourceRepository  ----------->         -------------->
 *             Map<Id,Resource>                Map<Id,Resource>     Map<Id,Resource>
 *             explicit + infered                                     enriched with discovery
 *
 *
 *
 * Updated at runtime (without re-reapeating inference/discovery)
 *  
 *       Monitored(Runtime)
 *         |
 *         |    
 *        \/     
 *     --------> CmdbEnv
 *                    Map<Id,Resource>
 *                    + Resource->Metrics result
 * </PRE>
 */
public class CmdbEnv {

	private static final Logger LOG = LoggerFactory.getLogger(CmdbEnv.class);

	private static final String CTX_CmdbEnv = "CmdbEnv";

	private final CmdbEnvRepository owner;
	protected final String envName;

	private final Object lock = new Object();

	private EnvTemplateInstanceParameters templateParams;

	private Supplier<List<ResourceFileContent>> rawTemplateContentsProvider;

	private List<String> phases = new ArrayList<>(ImmutableList.of("phase0"));
	private FxNodeFuncRegistry funcRegistry;
	private IEnvValueDecrypter envValueDecrypter;
	private Map<String, Object> registerCtxVars = new HashMap<>();

	private ResourceTypeRepository resourceTypeRepository;

	/** built resources repository (=Map<Id,Resource>) for rootNode */
	protected ResourceRepository resourceRepository;

	protected FxObjNodePrototypeRegistry prototypesRegistry;

	protected FxNodeFunc envEvalFunc;

	// ------------------------------------------------------------------------

	public CmdbEnv(CmdbEnvRepository owner, String envName, EnvTemplateInstanceParameters templateParams,
			Supplier<List<ResourceFileContent>> rawTemplateContentsProvider, IEnvValueDecrypter envValueDecrypter,
			FxObjNodePrototypeRegistry prototypesRegistry) {
		this.owner = owner;
		this.envName = envName;
		this.templateParams = templateParams;
		this.rawTemplateContentsProvider = rawTemplateContentsProvider;
		this.funcRegistry = owner.getFuncRegistry();
		this.resourceTypeRepository = owner.getResourceTypeRepository();
		this.envValueDecrypter = envValueDecrypter;
		this.resourceRepository = new ResourceRepository(resourceTypeRepository);
		this.prototypesRegistry = prototypesRegistry;
		this.envEvalFunc = new EnvEvalFxNodeFunc();
	}

	public void dispose() {
		// this.owner = null;
		// this.envName = null;
		this.funcRegistry = null;
		this.templateParams = null;
		this.rawTemplateContentsProvider = null;
		this.resourceTypeRepository = null;
		this.envValueDecrypter = null;
		if (registerCtxVars != null) {
			registerCtxVars.clear();
		}
		this.registerCtxVars = null;
		if (resourceRepository != null) {
			resourceRepository.close();
		}
		this.resourceRepository = null;
		this.prototypesRegistry = null;
		this.envEvalFunc = null;
	}

	protected boolean isDisposed() {
		return funcRegistry == null;
	}

	public static CmdbEnv ctxGet(FxEvalContext ctx) {
		return (CmdbEnv) ctx.lookupVariable(CTX_CmdbEnv);
	}

	/**
	 * eval Node + build Resources from node
	 */
	public void init() {
		synchronized (lock) {
			doReevalYamlAndResources();
		}
	}

	public void onChangeCtxReevalResources() {
		synchronized (lock) {
			doReevalYamlAndResources();
		}
	}

	protected final void doReevalYamlAndResources() {
		List<ResourceFileContent> evalContents = doReevalYaml();
		doReevalResourcesFromYaml(evalContents);
	}

	protected List<ResourceFileContent> doReevalYaml() {
		List<ResourceFileContent> rawTemplateContents = rawTemplateContentsProvider.get();

		if (isDisposed()) {
			return null;
		}

		List<ResourceFileContent> rawContents = reevalRawRootForTemplateParams(rawTemplateContents);

		if (isDisposed()) {
			return null;
		}
		rawTemplateContents = null; // optim gc

		List<ResourceFileContent> evalContents = reevalRootNodeFromRawRootNode(rawContents);

		if (isDisposed()) {
			return null;
		}
		return evalContents;
	}

	protected void doReevalResourcesFromYaml(List<ResourceFileContent> evalContents) {
		Map<ResourceId, Resource> resources = reevalResourcesFromNodes(evalContents);

		if (isDisposed()) {
			return;
		}
		evalContents = null; // optim gc

		// update resourceRepository with added/updated/removed resources
		// remove previous resources not present any more after reeval
		updateRepositoryAllResources(resources);

		if (isDisposed()) {
			return;
		}

		onPostProcessReevalKnownAdaptersFromResources();
	}

	// ------------------------------------------------------------------------

	public String getEnvName() {
		return envName;
	}

	public ResourceRepository getResourceRepository() {
		return resourceRepository;
	}

	public EnvTemplateInstanceParameters getTemplateParams() {
		return templateParams;
	}

//    public void setTemplateParams(EnvTemplateInstanceParameters templateParams) {
//        this.templateParams = templateParams;
//        reevalRawRootForTemplateParams();
//        onChangeCtxReevalResources();
//    }

	public void putRegisterCtxVar(String key, Object value) {
		this.registerCtxVars.put(key, value);
	}

	// ------------------------------------------------------------------------

	private List<ResourceFileContent> reevalRawRootForTemplateParams(List<ResourceFileContent> rawTemplateContents) {
		// replace template parameters
		List<ResourceFileContent> res;
		if (templateParams == null) {
			res = rawTemplateContents;
		} else {
			res = new ArrayList<>();
			for (ResourceFileContent e : rawTemplateContents) {
				FxNode data = e.getData();
				FxSourceLoc source = e.getSource();
				FxMemRootDocument templateReplDoc = new FxMemRootDocument(source);
				FxReplaceNodeCopyVisitor.copyWithReplaceTo(templateReplDoc.contentWriter(), data,
						templateParams.getParameters());
				res.add(new ResourceFileContent(source, templateReplDoc.getContent()));

				if (isDisposed()) {
					break;
				}
			}
		}
		return res;
	}

	// compute rootContents from rawContents
	private List<ResourceFileContent> reevalRootNodeFromRawRootNode(List<ResourceFileContent> rawContents) {
		// preprocess eval : recursive eval "@fx-eval"
		FxEvalContext ctx = createFxEvalContext();

		List<ResourceFileContent> res = new ArrayList<>();
		for (ResourceFileContent e : rawContents) {
			FxSourceLoc source = e.getSource();
			FxNode sourceData = e.getData();

			FxNode evalNode = replaceAndEvalPhases(ctx, source, sourceData);

			if (LOG.isDebugEnabled()) {
				List<String> evalIds = new ArrayList<>();
				FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(evalNode,
						(id, typeName, objData, loc) -> {
							evalIds.add(id);
						});
				LOG.debug("parse+eval yaml " + source + " => " + evalIds.size() + "\n" + " " + evalIds);
			}

			if (evalNode != null) {
				res.add(new ResourceFileContent(source, evalNode));
			}

			if (isDisposed()) {
				break;
			}
		}

		return res;
	}

	public Map<String, FxNode> createEnvReplNodes() {
		Map<String, FxNode> envNameReplacements = new HashMap<>();
		FxSourceLoc loc = new FxSourceLoc("env", envName);
		envNameReplacements.put("env", new FxMemRootDocument(loc).contentWriter().add(envName, loc));
		return envNameReplacements;
	}

	private FxEvalContext createFxEvalContext() {
		FxEvalContext ctx = new FxEvalContext(null, funcRegistry);
		ctx.putVariable(CTX_CmdbEnv, this);
		ctx.putVariable("env", envName);
		ctx.putVariableAll(registerCtxVars);
		return ctx;
	}

	private FxNode replaceAndEvalPhases(FxEvalContext ctx, FxSourceLoc source, FxNode sourceData) {
		final String sourceFilePath = source.getFilePath();
		FxNode resourceNode;

		FxMemRootDocument processedDoc = new FxMemRootDocument(source);
		try {
			String phase0 = phases.get(0);
			FxEvalContext childCtx = FxCurrEvalCtxUtil.childEvalCtx(ctx, phase0, envEvalFunc);

			envEvalFunc.eval(processedDoc.contentWriter(), childCtx, sourceData);

			// prepend all { id: "x/y"..} by { id: "<<envName>>/x/y" }
			resourceNode = copyReplaceRelativeIdWithEnv(processedDoc.getContent(), sourceFilePath, envName);
		} catch (Exception ex) {
			LOG.error("Failed to eval json/yaml for " + source + " ... ignore, no rethrow!", ex);
			resourceNode = null;
		}

		return resourceNode;
	}

	public class EnvEvalFxNodeFunc extends FxNodeFunc {
		@Override
		public void eval(FxChildWriter dest, FxEvalContext ctx, FxNode src) {
			runEvalContentFunc(dest, ctx, src);
		}
	}

	// function eval called for content, and recursively called for included content
	protected void runEvalContentFunc(FxChildWriter dest, FxEvalContext ctx, FxNode src) {
		Map<String, FxNode> envNameReplacements = createEnvReplNodes();

		FxNode resourceNode = src;

		// pre-process prototypes
		if (prototypesRegistry != null) {
			resourceNode = prototypesRegistry.copyPreprocessPrototypes(resourceNode);
		}

		// replace all "..@{env}.." by "..<<envName>>.."
		{
			FxMemRootDocument replaceEnvNameDoc = new FxMemRootDocument(src.getSourceLoc());
			FxReplaceNodeCopyVisitor.copyWithReplaceTo(replaceEnvNameDoc.contentWriter(), resourceNode,
					envNameReplacements);
			resourceNode = replaceEnvNameDoc.getContent();
		}

		// eval credentialIds, decrypt ...
		resourceNode = copyReplaceEnvValueDecrypt(resourceNode);

		// evaluate all { "@fx-eval": "phase0:<<func>>" .. }
		// *** eval rawRootNode -> rootNode by evaluating recursive functions
		// for "phase0", ... ***
		FxPhaseRecursiveEvalFunc.evalPhases(dest, phases, ctx, resourceNode, funcRegistry);

		// TODO repeat replace+prototype+decrypt+eval ... if needed ?!

	}

	protected FxNode copyReplaceRelativeIdWithEnv(FxNode sourceData, final String sourceFilePath,
			final String envName) {
		// recursive replace "relativeId", and "id" by prepending current pathId
		// FxReplaceNodeCopyVisitor.copyWithReplaceTo(dest, template, varReplacements);
		FxNodeCopyVisitor relativeIdTransformCopier = new FxNodeCopyVisitor() {
			@Override
			public FxNode visitObj(FxObjNode src, FxChildWriter out) {
				FxObjNode res = out.addObj(src.getSourceLoc());
				for (Iterator<Map.Entry<String, FxNode>> iter = src.fields(); iter.hasNext();) {
					Entry<String, FxNode> srcFieldEntry = iter.next();
					String fieldname = srcFieldEntry.getKey();
					FxNode srcValue = srcFieldEntry.getValue();
					if (srcValue == null) {
						// TODO ignore {fieldname: null ...}
						continue;
					}
					FxSourceLoc srcValueLoc = srcValue.getSourceLoc();

					if ("relativeId".equals(fieldname)) {
						String relativeId = FxNodeValueUtils.nodeToString(srcValue);
						String id = sourceFilePath + relativeId;
						// replace field name "relativeId" by "id" + prepend content value by pathId
						// example: { relativeId: "a/b" } => { id: "pathId/a/b" }
						res.put("id", id, srcValueLoc);
					} else if ("id".equals(fieldname)) {
						String srcId = FxNodeValueUtils.nodeToString(srcValue);
						String id;
						if (srcId.startsWith("//")) {
							// detected escape for forcing outer environement id.. un-escape
							// example: { id: "//anotherEnv/a/b" } => { id: "anotherEnv/a/b" }
							id = srcId.substring(2);
						} else if (srcId.startsWith("/")) {
							// should not occur? ... detected extra "/"
							// example: { id: "/a/b" } => { id: "envName/a/b" }
							id = envName + srcId;
						} else {
							id = envName + "/" + srcId;
						}
						res.put("id", id, srcValueLoc);
					} else {
						// recurse copy object field value
						FxChildWriter outChildAdder = res.putBuilder(fieldname);
						srcValue.accept(this, outChildAdder);
					}
				}
				return res;
			}
		};

		FxSourceLoc loc = sourceData.getSourceLoc();
		FxMemRootDocument rawEnvDoc = new FxMemRootDocument(loc);
		FxArrayNode rawRootNode = rawEnvDoc.contentWriter().addArray(loc);
		FxChildWriter rawNodesWriter = rawRootNode.insertBuilder();

		sourceData.accept(relativeIdTransformCopier, rawNodesWriter);
		return rawRootNode;
	}

	protected FxNode copyReplaceEnvValueDecrypt(FxNode src) {
		FxNodeCopyVisitor transformCopier = new FxNodeCopyVisitor() {
			@Override
			public FxNode visitTextValue(FxTextNode src, FxChildWriter out) {
				String value = src.getValue();
				FxSourceLoc loc = src.getSourceLoc();
				if (value != null && value.startsWith(IEnvValueDecrypter.PREFIX_CMDB)) {
					loc = FxSourceLoc.newFrom("decrypt", src.getSourceLoc());
					value = envValueDecrypter.decryptValueForEnv(value, envName);
				}
				return out.add(value, loc);
			}
		};
		FxMemRootDocument doc = new FxMemRootDocument(src.getSourceLoc());
		FxChildWriter writer = doc.contentWriter();
		src.accept(transformCopier, writer);
		return doc.getContent();
	}

	public static class ResourceBuilder {
		CmdbEnv owner;
		Map<ResourceId, Resource> reusePrevResources; // while re-evaluating => reuse already allocated objects
		Map<ResourceId, Resource> resultResources = new HashMap<>();

		public ResourceBuilder(CmdbEnv owner, Map<ResourceId, Resource> reusePrevResources) {
			this.owner = owner;
			this.reusePrevResources = reusePrevResources;
		}

		public Resource createResource(ResourceId id, ResourceType type, FxObjNode objData,
				FxSourceLoc declarationLocation) {
			Resource res = resultResources.get(id);
			if (res != null) {
				LOG.error("Resource already defined for id:" + id + " .. ignore inconsistent re-definition!!\n"
						+ " decl location:" + res.getDeclarationLocation() + "\n" + " new decl:" + declarationLocation);
				return res;
			}

			res = reusePrevResources.get(id);
			if (res != null) {
				if (!res.getType().equals(type)) {
					// type changed after re-eval!
					res = null;
				}
			}
			if (res == null) {
				// create
				res = new Resource(// owner,
						id, type, objData, declarationLocation);
			} else {
				// update
				res.setObjData(objData); // => invalidate cached adapter(s) if any
			}

			resultResources.put(id, res);
			return res;
		}

		public Map<ResourceId, Resource> getResultResources() {
			return resultResources;
		}

		public Resource findResultResource(ResourceId id) {
			return resultResources.get(id);
		}

	}

	/**
	 * recursive scan all nodes with
	 * 
	 * <PRE>
	 * {id="", type="" ...}
	 * </PRE>
	 */
	protected Map<ResourceId, Resource> reevalResourcesFromNodes(List<ResourceFileContent> rootContents) {
		// take snapshot of previously defined resource ids
		Map<ResourceId, Resource> prevResources = new HashMap<>(resourceRepository.findAllAsMap());
		Map<ResourceId, Resource> reusePrevResources = ImmutableMap.copyOf(prevResources);

		ResourceBuilder resourceBuilder = new ResourceBuilder(this, reusePrevResources);

		Map<ResourceId, Resource> explicitResources = new HashMap<>();

		// scan tree nodes for creating explicit resources (=> to add in
		// resourceRepository, re-use if already created)
		// => fx-tree library use FIELD_id="id", FIELD_type="type"
		for (ResourceFileContent e : rootContents) {
			FxNode data = e.getData();

			FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(data, (id, typeName, objData, loc) -> {
				ResourceId resourceId = ResourceId.valueOf(id);
				ResourceType type = resourceTypeRepository.getOrCreateType(typeName);

				FxSourceLoc objLoc = objData.getSourceLoc();
				if (objLoc == null) {
					objLoc = e.getSource(); // should not occur
				}

				// TODO TOCHANGE very unsafe...
				// overridding resource for same id should not be allowed..
				// but re-evaluating yaml->resource could re-use allocated object...
				Resource resource = resourceBuilder.createResource(resourceId, type, objData, objLoc);

				explicitResources.put(resourceId, resource);
			});

			if (isDisposed()) {
				break;
			}
		}

		// run inference engine rules, to add implicit resources
		Map<ResourceId, Resource> allResources = runInferenceEngineResourceRules(explicitResources, resourceBuilder);

		// finish resolve cross resource relationships
		resolveStdNodeRelationshipsToResourceRelationships(allResources);

		return allResources;
	}

	protected List<Resource> dataToResources(FxNode data, FxSourceLoc declarationLocation) {
		val res = new ArrayList<Resource>();
		FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(data, (id, typeName, objData, loc) -> {
			ResourceId resourceId = ResourceId.valueOf(id);
			ResourceType type = resourceTypeRepository.getOrCreateType(typeName);
			Resource resource = new Resource(// this,
					resourceId, type, objData, declarationLocation); // TODO ARN loc ??
			res.add(resource);
		});
		return res;
	}

	public List<Resource> evalDataAddResources(FxNode sourceData, FxSourceLoc sourceLocation) {
		FxEvalContext ctx = createFxEvalContext();

		FxNode evalData = replaceAndEvalPhases(ctx, sourceLocation, sourceData);

		List<Resource> res = dataToResources(evalData, sourceLocation);

		addResources(res);

		return res;
	}

	private void addResources(List<Resource> res) {
		Map<ResourceId, Resource> prevResources = resourceRepository.findAllAsMap();
		Map<ResourceId, Resource> newResources = new HashMap<>(prevResources);
		newResources.putAll(Resource.lsToIdMap(res));
		updateRepositoryAllResources(newResources);
	}

	private void updateRepositoryAllResources(Map<ResourceId, Resource> allResources) {
		Map<ResourceId, Resource> resourceToRemoves = new HashMap<>(resourceRepository.findAllAsMap());
		for (Resource resource : allResources.values()) {
			resourceToRemoves.remove(resource.getId());
		}
		if (!resourceToRemoves.isEmpty()) {
			for (Resource resourceToRemove : resourceToRemoves.values()) {
				resourceRepository.remove(resourceToRemove);
			}
		}
		// check for type modified resources, and added resources
		for (Resource resource : allResources.values()) {
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

	private Map<ResourceId, Resource> runInferenceEngineResourceRules(Map<ResourceId, Resource> explicitResources,
			ResourceBuilder resourceBuilder) {
		Map<ResourceId, Resource> res = new HashMap<>();
		KieSession ksession = owner.buildCmdbResourceInferenceSession();
		if (ksession == null) {
			return explicitResources;
		}
		try {
			// put globals
			Globals kGlobals = ksession.getGlobals();
			EnvResourceDRulesCtx envResourceCtx = new EnvResourceDRulesCtx(this, ksession, resourceBuilder);
			kGlobals.set("envResourceCtx", envResourceCtx);
			kGlobals.set("envName", envName);
			// kGlobals.set("envResourceRepository", this); // should be useless .. try to
			// eval without side-effect!
			// kGlobals.set("resourceRepository", resourceRepository);
			kGlobals.set("resourceTypeRepository", resourceTypeRepository);
			Map<ResourceId, Resource> resourceById = new HashMap<>(explicitResources);
			kGlobals.set("resourceById", resourceById);
			kGlobals.set("resourceBuilder", resourceBuilder);

			for (Map.Entry<String, Object> e : registerCtxVars.entrySet()) {
				kGlobals.set(e.getKey(), e.getValue());
			}
			// callback to register additionnals Kie global.. but different than FxTree
			// eval?
			Map<String, Object> globalsToAdd = onPostProcessDRules_globalsToAdd(envResourceCtx);
			if (globalsToAdd != null && !globalsToAdd.isEmpty()) {
				for (Map.Entry<String, Object> e : globalsToAdd.entrySet()) {
					kGlobals.set(e.getKey(), e.getValue());
				}
			}

			// put all explicit resources
			for (Resource obj : explicitResources.values()) {
				ksession.insert(obj);
			}

			// callback to register additionnals Kie global.. but different than FxTree
			// eval?
			List<Object> factsToInsert = onPostProcessDRules_factsToInsert(envResourceCtx);
			if (factsToInsert != null && !factsToInsert.isEmpty()) {
				for (Object fact : factsToInsert) {
					ksession.insert(fact);
				}
			}

			Collection<?> objectsBefore = new ArrayList<>(ksession.getObjects());

			LOG.debug("init env '" + envName + "' : eval DRules");
			long startTime = System.currentTimeMillis();
			try {

				// *** the Biggy : compute rules ***
				ksession.fireAllRules();

			} catch (Exception ex) {
				LOG.error("Failed post-processing rules for '" + envName + "' .. ignore, no rethrow!", ex);
			}
			long millis = System.currentTimeMillis() - startTime;
			if (millis > 1000) {
				LOG.info("slow post-processing rules for '" + envName + "' : took " + millis + "ms");
			}

			onPostProcessDRules_finishRules(envResourceCtx);

			Collection<?> resultObjects = new ArrayList<>(ksession.getObjects());

			// determine resources to add from created objects
			for (Object obj : resultObjects) {
				if (obj instanceof Resource) {
					Resource resObj = (Resource) obj;
					res.put(resObj.getId(), resObj);
				}
			}

			int countNewObjects = resultObjects.size() - objectsBefore.size();
			if (countNewObjects > 0) {
				LOG.info("init env:'" + envName + "' : objects created by inference rules:" + countNewObjects);
			}

		} finally {
			ksession.dispose();
		}
		return res;
	}

	protected Map<String, Object> onPostProcessDRules_globalsToAdd(EnvResourceDRulesCtx drulesCtx) {
		// overridable
		return null;
	}

	protected List<Object> onPostProcessDRules_factsToInsert(EnvResourceDRulesCtx drulesCtx) {
		// overridable
		return null;
	}

	protected void onPostProcessDRules_finishRules(EnvResourceDRulesCtx drulesCtx) {
		// overridable
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

		// resolve parent
		for (Resource resource : resources.values()) {
			ResourceId parentId = resource.getId().parent();
			Resource parent = resources.get(parentId);
			// TODO ARN resource._setParent(parent);
		}
	}

	protected void onPostProcessReevalKnownAdaptersFromResources() {
		// cf override to eagerly parse all resource with known resource type, and get
		// corresponding adapter
	}

	// delegate methods to resourceRepository
	// ------------------------------------------------------------------------

	public List<Resource> findAll() {
		return resourceRepository.findAll();
	}

	public Resource findById(ResourceId pathId) {
		return resourceRepository.findById(pathId);
	}

	public List<Resource> findByExactType(ResourceType type) {
		return resourceRepository.findByExactType(type);
	}

	public Resource findFirstByExactTypeAndCrit(ResourceType resourceType, Predicate<Resource> predicate) {
		return resourceRepository.findFirstByExactTypeAndCrit(resourceType, predicate);
	}

	public List<Resource> findBySubType(ResourceType resourceType) {
		return resourceRepository.findBySubType(resourceType);
	}

	public List<Resource> findBySubTypeAndCrit(ResourceType resourceType, Predicate<Resource> predicate) {
		return resourceRepository.findBySubTypeAndCrit(resourceType, predicate);
	}

	public List<Resource> findByExactTypeAndCrit(ResourceType type, Predicate<Resource> predicate) {
		return resourceRepository.findByExactTypeAndCrit(type, predicate);
	}

	public Resource findAncestorByType(ResourceId id, ResourceType ancestorType) {
		return resourceRepository.findAncestorByType(id, ancestorType);
	}

	/** for interactive testing yaml processing */
	public String evalYaml(String inputText) {
		FxEvalContext ctx = createFxEvalContext();

		FxSourceLoc source = new FxSourceLoc("", "eval-input");
		FxNode sourceData = FxYamlUtils.yamlTextToTree(inputText);

		FxNode evalNode = replaceAndEvalPhases(ctx, source, sourceData);

		String res = FxYamlUtils.treeToYamlText(evalNode);
		return res;
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "EnvResourceRepository[" + toStringBase() + "]";
	}

	public String toStringBase() {
		if (templateParams == null) {
			return envName;
		}
		return envName + " (from '" + templateParams.getSourceTemplateName() + "' with params: "
				+ templateParams.getParameters() + ")";
	}

}
