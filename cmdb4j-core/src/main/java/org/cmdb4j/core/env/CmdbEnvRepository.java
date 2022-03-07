package org.cmdb4j.core.env;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.ResourceFileContent;
import org.cmdb4j.core.env.prototype.FxObjNodePrototypeRegistry;
import org.cmdb4j.core.env.template.EnvTemplateDescr;
import org.cmdb4j.core.env.template.EnvTemplateInstanceParameters;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.store.IEnvValueDecrypter;
import org.cmdb4j.core.store.IServerCredentialsStore;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;

/**
 * root directory for list of CmdEnv, based on file system Json / Yaml pre-processed files.<br/>
 * <p/>
 * 
 * The list of environments is dynamic, obtained by scanning all sub-dirs of the top level base directory,<br/>
 * with 3 special directories for handling "Default", "Templates" definition environment, and "cloud" template instance.
 * 
 * <H2>file system layout for multi-environments</H2>
 * 
 * <PRE>
 * baseEnvsDirs/
 *   Default/ ... not an environment
 *   Templates/
 *     cloud1-template/
 *     cloud2-template/
 *     ..
 *   cloud/
 *     cloud1-instance1/
 *     cloud1-instance2/
 *     cloud2-instance1/
 *     ..
 *   DEV1/
 *   INT1/
 *   UAT1/
 *   TNH1/
 *   PROD/
 *   ..
 * </PRE>
 * 
 * 
 * Each environment dir can contains any number of **\/*.yaml and **\/*.json files recursively<br/>
 * Data loading step:
 * <ol>
 * <li> Files are recursively scanned, parsed as Yaml or Json tree, 
 *   with implicit "relativeId" field prepended to give "id" for the current file path.<br/>
 *   "id" fields is also impicitely prepended by environment name (force environment isolation, except when explicitely set as '//otherEnv/a/b' ).<br/>
 *   Content trees are concatenated as a list nodes... in effect, this is equivalent to having 1 single file, with "[ file1Content, file2Content, ...]"
 *   </li>
 * <li>when template environment parameters are present (cloud env instance), parameters are substitued in tree content.</BR>
 *   Typical parameters = number of node to provision, name of user, topology parameters, ..
 *   </li>
 * <li>Content tree is then preprocessed to evaluate functions with "@eval-function" field markers...
 *   </li>
 * </ol>
 * 
 * <H2>Example of a single environment file system layout</H2>
 * example:
 * 
 * <PRE>
 * baseEnvsDirs/
 *   env123/
 *      env*.json        ==> all fields {"id": "a/b"} replaced by {"id":"env123/a/b" } 
 *      env*.yaml        ==> idem
 * </PRE>
 * 
 */
public class CmdbEnvRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(CmdbEnvRepository.class);

    private static final String RULES_DIRNAME = "Rules";
    private static final String DYNSTORE_CLOUD_LIST_KEY = CmdbInputsSource.CLOUD_DIRNAME + "/.list";
    private static long DYNSTORE_CLOUD_LIST_KEY_EXPIRY_MILLIS = 3*60*1000;
    private static final String DYNSTORE_TEMPLATE_PARAM_KEY = "template-param.json";
        
    private CmdbInputsSource envsInputSource;

    protected IServerCredentialsStore serverCredentialsStore;
    
    private DynStoreService dynStoreService;

    protected ResourceTypeRepository resourceTypeRepository;

    private FxNodeFuncRegistry funcRegistry;
    
    private IEnvValueDecrypter envValueDecrypter;
    
    private ResourceRepository globalResources;
    
    private File rulesDir;
    
    private KieContainer drulesEnvsResourceContainer;

    
    private ImmutableSet<String> _cacheEnvNames;

    private Map<String, CmdbEnv> _cacheEnvByName = Collections.synchronizedMap(new HashMap<>());

    private final StringListCachedDynStoreEntry _cacheCloudEnvNames;
    private Map<String,EnvTemplateInstanceParameters> _cacheCloudEnvParameters = Collections.synchronizedMap(new HashMap<>());
    
    private FxObjNodePrototypeRegistry prototypesRegistry;
    
    // ---------------------------------------------------------------------- --

    public CmdbEnvRepository(
            CmdbInputsSource envsInputSource,
            DynStoreService dynStoreService,
            ResourceTypeRepository resourceTypeRepository, 
            FxNodeFuncRegistry funcRegistry,
            IEnvValueDecrypter envValueDecrypter) {
        this.envsInputSource = envsInputSource;
        this.dynStoreService = dynStoreService;
        this.rulesDir = new File(envsInputSource.getDefaultCheckoutDir(), CmdbInputsSource.DEFAULT_DIRNAME + "/" + RULES_DIRNAME);
        this.resourceTypeRepository = resourceTypeRepository;
        this.funcRegistry = funcRegistry;
        this.envValueDecrypter = envValueDecrypter;
        this.globalResources = new ResourceRepository(resourceTypeRepository);
        this._cacheCloudEnvNames = new StringListCachedDynStoreEntry(dynStoreService, DYNSTORE_CLOUD_LIST_KEY, DYNSTORE_CLOUD_LIST_KEY_EXPIRY_MILLIS);
        this.prototypesRegistry = new FxObjNodePrototypeRegistry(envsInputSource);
        buildEnvsResourceRulesContainer();
    }

    // ---------------------------------------------------------------------- --

    public void purge() {
        // optim gc: tmp copy refs to dispose 
        Map<String, CmdbEnv> copyCacheEnvByName = new HashMap<>(_cacheEnvByName); 
        
        this._cacheEnvNames = null;
        _cacheEnvByName.clear();
        _cacheCloudEnvParameters.clear();

        for(CmdbEnv env : copyCacheEnvByName.values()) {
            env.dispose();
        }

        buildEnvsResourceRulesContainer();
        prototypesRegistry.purgeReload();
    }

    public void purgeEnvsCache(Collection<String> envNames) {
        for (String envName : envNames) {
            _cacheEnvByName.remove(envName);
        }
    }
    
    public CmdbInputsSource getEnvsInputSource() {
        return envsInputSource;
    }
    
    public ResourceTypeRepository getResourceTypeRepository() {
        return resourceTypeRepository;
    }

    public FxNodeFuncRegistry getFuncRegistry() {
        return funcRegistry;
    }
    
    public FxObjNodePrototypeRegistry getPrototypesRegistry() {
        return prototypesRegistry;
    }

    public ResourceRepository getGlobalResources() {
        return globalResources;
    }
    
    public Set<String> listEnvs() {
        ImmutableSet<String> res = _cacheEnvNames;
        if (res == null) {
            Set<String> tmpres = new LinkedHashSet<>();
            tmpres.addAll(envsInputSource.listEnvs());
            Collection<String> cloudEnvs = listCloudEnvs();
            if (cloudEnvs != null && !cloudEnvs.isEmpty()) {
                tmpres.addAll(cloudEnvs);
            }

            res = ImmutableSet.copyOf(tmpres);
            _cacheEnvNames = res;
        }
        return res;
    }
    

    public String resourceIdToEnvName(ResourceId resourceId) {
        if (resourceId == null) {
            return null;
        }
        final int pathLen = resourceId.size();
        if (pathLen == 0) {
            return null;
        }
        String res = resourceId.get(0);
        if (res.equals("")) {
            return null;
        }
        if (res.equals(CmdbInputsSource.CLOUD_DIRNAME)) {
            if (pathLen == 1) {
                return null;
            }
            res += "/" + resourceId.get(1);
        }
        // check env dir exists
//        File envDir = new File(baseEnvsDir, res);
//        if (!envDir.exists() || !envDir.isDirectory()) {
//            return null;
//        }
        return res;
    }

    public Resource getResourceById(ResourceId resourceId) {
        Resource res = globalResources.findById(resourceId);
        if (res == null) {
            String envName = resourceIdToEnvName(resourceId);
            if (envName == null) {
                return null;
            }
            CmdbEnv envRepo = getEnv(envName);
            res = envRepo.getResourceRepository().getById(resourceId);
        }
        return res;
    }

    public CmdbEnv getEnv(String envName) {
        if (envName == null || envName.isEmpty() 
                || CmdbInputsSource.isReservedEnvDirName(envName)) {
            return null;
        }
        CmdbEnv res = _cacheEnvByName.get(envName);
        if (res == null) {
            Set<String> envs = listEnvs();
            if (! envs.contains(envName)) {
                return null;
            }
            
            res = onParseEnv(envName);
            
            _cacheEnvByName.put(envName, res);
            
            onInitEnv(res);
        }
        return res;
    }

    protected void onInitEnv(CmdbEnv envRepo) {
        envRepo.init();
    }

    protected CmdbEnv onParseEnv(String envName) {
        CmdbEnv res;
        if (! envName.startsWith(CmdbInputsSource.CLOUD_DIRNAME + "/")) {
            res = parseStdEnvResourcesTree(envName);
        } else {
            res = parseCloudEnvResourcesTree(envName);
        }
        return res;
    }


    protected CmdbEnv parseStdEnvResourcesTree(String envName) {
        // recursive scan <<inputDirs>>/<<envName>>/**/env*.(json|yaml)
        Supplier<List<ResourceFileContent>> contentSupplier = () -> envsInputSource.scanEnvResourceFiles(envName);
        return buildEnvResourceTreeRepository(envName, contentSupplier, null); 
    }


    protected CmdbEnv parseCloudEnvResourcesTree(String envName) {
        // read <<inputDirs>>/<<cloud-envName>>/template-params.(json|yaml)
        EnvTemplateInstanceParameters templateParams = 
                envsInputSource.readEnvTemplateInstanceParams(envName);
        
        Supplier<List<ResourceFileContent>> contentSupplier = () -> {
            String templateName = templateParams.getSourceTemplateName();
            // recursive scan "<<inputDirs>>/Templates/<<templateName>>/**/env*.(yaml|json)"
            EnvTemplateDescr templateDescr = envsInputSource.getEnvTemplateDescr(templateName);
            List<ResourceFileContent> templateFileContents = templateDescr.getResourceFileContents();
            return templateFileContents;
        };
        return buildEnvResourceTreeRepository(envName, contentSupplier, templateParams); 
    }
    
    protected CmdbEnv buildEnvResourceTreeRepository(String envName, 
            Supplier<List<ResourceFileContent>> rawRootContentsSupplier, 
            EnvTemplateInstanceParameters templateParams) {
        CmdbEnv res = new CmdbEnv(this, envName, templateParams, rawRootContentsSupplier, envValueDecrypter, prototypesRegistry);
        // cf next... "res.init();" called from outer (may need registerCtxVar ...)
        return (CmdbEnv) res;
    }
    
    // Management of Env Templates
    // ------------------------------------------------------------------------


    public ImmutableSet<String> listEnvTemplates() {
        return envsInputSource.listEnvTemplates();
    }

    public List<EnvTemplateDescr> listEnvTemplateDescrs() {
        return envsInputSource.listEnvTemplateDescrs();
    }

    public EnvTemplateDescr getEnvTemplateDescr(String templateName) {
        return envsInputSource.getEnvTemplateDescr(templateName);
    }
    
    // Management of cloud env (="ephemeral") instance from template
    // ------------------------------------------------------------------------

    public Collection<String> listCloudEnvs() {
        return _cacheCloudEnvNames.getValue();
    }

    public CmdbEnv createEnvFromTemplate(EnvTemplateInstanceParameters envParams) {
        String envName = envParams.getEnvName();
        if (envName == null) {
            throw new IllegalArgumentException("envName param is null");
        }
        if (! envName.startsWith(CmdbInputsSource.CLOUD_DIRNAME)) {
            envName = CmdbInputsSource.CLOUD_DIRNAME + "/" + envName;
        }
        Collection<String> cloudEnvs = _cacheCloudEnvNames.getValueForceRefresh();
        if (cloudEnvs.contains(envName)) {
            throw new IllegalArgumentException("env '" + envName + "' already exists");
        }
        String sourceTemplateName = envParams.getSourceTemplateName();
        if (sourceTemplateName == null) {
            throw new IllegalArgumentException("sourceTemplateName param is null");
        }
        EnvTemplateDescr envTemplateDescr = envsInputSource.getEnvTemplateDescr(sourceTemplateName);
        if (envTemplateDescr == null) {
            throw new IllegalArgumentException("sourceTemplateName '" + sourceTemplateName + "' not found");
        }
        
        // hook to fill values
        onCreateEnvFromTemplate_FillParams(envName, envParams);
        
        // TODO ... check mandatory params + param validity + add meta parameters (creationDate, ..)
        LOG.info("create env '" + envName + "' from sourceTemplateName:" + sourceTemplateName + ", parameters:" + envParams.getParameters());
        
        String envParamJson = FxJsonUtils.valueToJsonText(envParams);
        String envParamKey = envName + "/" + DYNSTORE_TEMPLATE_PARAM_KEY;
        
        dynStoreService.set(envParamKey, envParamJson);
        
        _cacheCloudEnvNames.add(envName);
        
        // invalidate/fire change..
        if (_cacheEnvNames != null) {
            _cacheEnvNames = ImmutableSet.<String>builder().addAll(_cacheEnvNames).add(envName).build();
        }
        
        // TODO dynStoreService.publish("env", "create", envName);
        
        // init new env repo
        CmdbEnv envRepo = getEnv(envName);
        
        return envRepo;
    }
    
    protected void onCreateEnvFromTemplate_FillParams(String cloudEnvName, EnvTemplateInstanceParameters instanceParam) {
        // overridable, default do nothing
    }
    

    // Management of inference engine rules
    // ------------------------------------------------------------------------

    protected void buildEnvsResourceRulesContainer() {
        if (rulesDir.exists() && rulesDir.isDirectory()) {
            LOG.info("building inference engine rules for Cmdb Resources");
            try {
                KieServices ks = KieServices.Factory.get();
                
                KieRepository kr = ks.getRepository();
                KieFileSystem kfs = ks.newKieFileSystem();

                // scan all *.drl files in "Default/Rules/**/*.drl"
                recursiveScanDrulesFiles(kfs, rulesDir, "");
        
                KieBuilder kb = ks.newKieBuilder(kfs);
        
                kb.buildAll();
                
                Results kbBuildResults = kb.getResults();
                if (kbBuildResults.hasMessages(Message.Level.ERROR)) {
                    throw new RuntimeException("Failed to build inference engine rules for Cmdb Resources:\n" + kbBuildResults.toString());
                }
        
                this.drulesEnvsResourceContainer = ks.newKieContainer(kr.getDefaultReleaseId());
            } catch(Exception ex) {
                LOG.error("Failed to build inference engine rules for Cmdb Resources: ex:" + ex.getMessage(), ex);
                // TODO ignore all rules or rethrow?
            }
        }
    }

    protected void recursiveScanDrulesFiles(KieFileSystem kfs, File dir, String currPathId) {
        File[] files = dir.listFiles();
        if (files == null)
            return; // dir not found?
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith(".")) continue;
            if (file.isDirectory()) {
                String childPathId = currPathId + fileName + "/";
                // recurse in sub dir
                recursiveScanDrulesFiles(kfs, file, childPathId);
            } else if (file.isFile() && file.canRead() && file.getName().endsWith(".drl")) {
                LOG.info("detected inference rule file:" + file);
                kfs.write(ResourceFactory.newFileResource(file));
            }
        }
    }
    
    /*pp*/ KieSession buildCmdbResourceInferenceSession() {
        if (drulesEnvsResourceContainer == null) {
            return null;
        }
        return drulesEnvsResourceContainer.newKieSession();
    }
    
}
