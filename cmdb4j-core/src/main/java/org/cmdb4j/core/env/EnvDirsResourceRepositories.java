package org.cmdb4j.core.env;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.cmdb4j.core.dto.env.EnvTemplateInstanceParametersDTO;
import org.cmdb4j.core.env.impl.EnvTemplateDescrDTOMapper;
import org.cmdb4j.core.env.impl.EnvTemplateInstanceParametersDTOMapper;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.ResourceRepository;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.util.CopyOnWriteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.model.FxArrayNode;
import fr.an.fxtree.model.FxChildWriter;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;

/**
 * root directory for list of EnvResourceTreeRepository(ies), base on file system Json / Yaml pre-processed files.<br/>
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
 *      /env.json        ==> all fields {"id": "a/b"} replaced by {"id":"env123/a/b" } 
 *      /env.yaml        ==> idem
 *      /file1.json      ==> all implicit fields {"relativeId": "a/b"}  replaced by {"id":"env123/file1/a/b" }
 *                           all fields {"id": "a/b"} replaced by {"id":"env123/a/b" } 
 *      /file1/yaml      ==> idem
 *      /subDir1/
 *         /env.json     ==> all implicit fields {"relativeId": "a/b"}  replaced by {"id":"env123/subDir1/a/b" }
 *                           all fields {"id": "a/b"}  replaced by {"id":"env123/a/b" }
 *         /env.yaml     ==> idem
 *         /file1.json   ==> all implicit fields {"relativeId": "a/b"}  replaced by {"id":"env123/subDir1/file1/a/b" } 
 *                           all fields {"id": "a/b"}  replaced by {"id":"env123/a/b" }
 *         /file1/yaml   ==> idem
 *         /subSubDir1/
 *           ..
 * </PRE>
 * 
 */
public class EnvDirsResourceRepositories {
    
    private static final Logger LOG = LoggerFactory.getLogger(EnvDirsResourceRepositories.class);

    private static final String DEFAULT_DIRNAME = "Default";
    private static final String TEMPLATES_DIRNAME = "Templates";
    private static final String TEMPLATE_PARAM_BASEFILENAME = "template-param";
    private static final String ENV_TEMPLATE_DECSCR_BASEFILENAME = "env-template-descr";
    private static final String ENV_BASEFILENAME = "env";
    
    private static final Predicate<String> DEFAULT_DIRNAME_ENV_ACCEPT = name -> ! (
            name.equals(DEFAULT_DIRNAME) || name.equals(TEMPLATES_DIRNAME) || name.startsWith("test-"));
    
    private File baseEnvsDir;

    private File defaultDir;
    private File baseEnvTemplatesDir;

    private String cloudDirname = "cloud";
    private File baseCloudDir;
    
    
    private Predicate<String> dirnameEnvAccept = DEFAULT_DIRNAME_ENV_ACCEPT;
    
    protected ResourceTypeRepository resourceTypeRepository;

    private FxNodeFuncRegistry funcRegistry;
    
    private ResourceRepository globalResources;
    
    private List<String> _cacheListEnvs;

    private Map<String, EnvResourceRepository> _cacheEnv2Repo = Collections.synchronizedMap(new HashMap<>());

    
    private List<String> _cacheListEnvTemplates;
    
    private Map<String,EnvTemplateDescr> _cacheEnvTemplateDescr = new HashMap<>();
    
    protected EnvTemplateInstanceParametersDTOMapper envTemplateInstanceParametersDTOMapper = new EnvTemplateInstanceParametersDTOMapper();
    protected EnvTemplateDescrDTOMapper envTemplateDescrDTOMapper = new EnvTemplateDescrDTOMapper();
    
    // ---------------------------------------------------------------------- --

    public EnvDirsResourceRepositories(File baseEnvsDir, ResourceTypeRepository resourceTypeRepository, FxNodeFuncRegistry funcRegistry) {
        this.baseEnvsDir = baseEnvsDir;
        this.baseEnvTemplatesDir = new File(baseEnvsDir, TEMPLATES_DIRNAME);
        this.defaultDir = new File(baseEnvsDir, DEFAULT_DIRNAME); 
        this.baseCloudDir = new File(baseEnvsDir, cloudDirname);
        this.resourceTypeRepository = resourceTypeRepository;
        this.funcRegistry = funcRegistry;
        this.globalResources = new ResourceRepository(resourceTypeRepository);
    }

    // ---------------------------------------------------------------------- --

    public void purge() {
        _cacheListEnvs = null;
        _cacheEnv2Repo.clear();
        _cacheListEnvTemplates = null;
        _cacheEnvTemplateDescr.clear();
    }

    public ResourceTypeRepository getResourceTypeRepository() {
        return resourceTypeRepository;
    }

    public FxNodeFuncRegistry getFuncRegistry() {
        return funcRegistry;
    }

    public ResourceRepository getGlobalResources() {
        return globalResources;
    }
    
    public List<String> listEnvs() {
        List<String> res = _cacheListEnvs;
        if (res == null) {
            res = new ArrayList<>();
            listEnvNamesInDir(res, baseEnvsDir, "");
            if (baseCloudDir.exists()) {
                listEnvNamesInDir(res, baseCloudDir, cloudDirname + "/");
            }

            res = ImmutableList.copyOf(res);
            _cacheListEnvs = res;
        }
        return res;
    }

    protected void listEnvNamesInDir(List<String> res, File dir, String prefix) {
        File[] files = dir.listFiles();
        for(File file : files) {
            String name = file.getName();
            if (!file.isDirectory() || name.startsWith(".") 
                    || file.equals(baseCloudDir) || file.equals(defaultDir) || file.equals(baseEnvTemplatesDir)
                    ) {
                continue;
            }
            if (dirnameEnvAccept != null && ! dirnameEnvAccept.test(name)) {
                continue;
            }
            res.add(prefix + name);
        }
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
        if (res.equals(cloudDirname)) {
            if (pathLen == 1) {
                return null;
            }
            res += "/" + resourceId.get(1);
        }
        // check env dir exists
        File envDir = new File(baseEnvsDir, res);
        if (!envDir.exists() || !envDir.isDirectory()) {
            return null;
        }
        return res;
    }

    public Resource getResourceById(ResourceId resourceId) {
        Resource res = globalResources.findById(resourceId);
        if (res == null) {
            String envName = resourceIdToEnvName(resourceId);
            if (envName == null) {
                return null;
            }
            EnvResourceRepository envRepo = getEnvRepo(envName);
            res = envRepo.getResourceRepository().getById(resourceId);
        }
        return res;
    }

    
    public EnvResourceRepository getEnvRepo(String envName) {
        if (envName == null || envName.isEmpty()
                || envName.equals(cloudDirname) || envName.equals(TEMPLATES_DIRNAME) || envName.equals(DEFAULT_DIRNAME)) {
            return null;
        }
        EnvResourceRepository res = _cacheEnv2Repo.get(envName);
        if (res == null) {
            File envDir = new File(baseEnvsDir, envName);
            if (!envDir.exists() || !envDir.isDirectory()) {
                return null;
            }
            if (! envName.startsWith(cloudDirname + "/")) {
                res = parseStdEnvResourcesTree(envName);
            } else {
                res = parseCloudEnvResourcesTree(envName);
            }
            onInitEnv(res);
            _cacheEnv2Repo.put(envName, res);
        }
        return res;
    }

    // Management of Env Templates
    // ------------------------------------------------------------------------

    public List<String> listEnvTemplates() {
        List<String> res = _cacheListEnvTemplates;
        if (res == null) {
            res = new ArrayList<>();
            listEnvNamesInDir(res, baseEnvTemplatesDir, "");
            res = ImmutableList.copyOf(res);
            _cacheListEnvTemplates = res;
        }
        return res;
    }

    public EnvTemplateDescr getEnvTemplateDescr(String templateName) {
        EnvTemplateDescr res = _cacheEnvTemplateDescr.get(templateName);
        if (res == null) {
            File envDir = new File(baseEnvTemplatesDir, templateName);
            if (!envDir.exists() || !envDir.isDirectory()) {
                return null;
            }
            // read file "Templates/<<name>>/env-template-descr.yaml"
            FxNode templateDescrNode = FxFileUtils.readFirstFileWithSupportedExtension(envDir, ENV_TEMPLATE_DECSCR_BASEFILENAME);
            if (templateDescrNode == null) {
                throw new RuntimeException("template descr file not found");
                // LOG.warn("template descr file not found '" + templateName + "/" + ENV_TEMPLATE_DECSCR_BASEFILENAME + ".(yaml|json)' .. using empty");
                // res = new EnvTemplateDescr(templateName, null, null, new LinkedHashMap<>(), new LinkedHashMap<>(), null);
            } else {
                res = envTemplateDescrDTOMapper.fromFxTree(templateDescrNode);
            }

            // scan files "Templates/<<name>>/**/env.yaml"
            FxArrayNode scannedRawRootNodes = scanAndAppendRawContents(envDir, templateName);
            
            // merge descr "rawNode" with nodes from "**/env.yaml"
            FxArrayNode mergeRawNode = scannedRawRootNodes;
            if (mergeRawNode == null) {
                mergeRawNode = new FxMemRootDocument().setContentArray();
            }
            FxNode descrRawNode = res.getRawNode();
            if (descrRawNode != null) {
                FxNodeCopyVisitor.copyTo(mergeRawNode.insertBuilder(), descrRawNode);
            }
            
            _cacheEnvTemplateDescr.put(templateName, res);
        }
        return res;
    }

    // Management of cloud env (="ephemeral") instance from template
    // ------------------------------------------------------------------------

    public EnvResourceRepository createEnvFromTemplate(String cloudSubEnvName, EnvTemplateInstanceParametersDTO instanceParamDTO) {
        if (cloudSubEnvName == null) {
            throw new IllegalArgumentException("cloud envName param is null");
        }
        String envName = cloudDirname + "/" + cloudSubEnvName;
        File cloudEnvDir = new File(baseCloudDir, cloudSubEnvName);
        if (cloudEnvDir.exists()) {
            throw new IllegalArgumentException("env '" + envName + "' already exists");
        }
        String sourceTemplateName = instanceParamDTO.getSourceTemplateName();
        if (sourceTemplateName == null) {
            throw new IllegalArgumentException("sourceTemplateName param is null");
        }
        EnvTemplateDescr envTemplateDescr = getEnvTemplateDescr(sourceTemplateName);
        if (envTemplateDescr == null) {
            throw new IllegalArgumentException("sourceTemplateName '" + sourceTemplateName + "' not found");
        }
        
        // convert from DTO to obj, and fill values
        EnvTemplateInstanceParameters instanceParam = envTemplateInstanceParametersDTOMapper.fromDTO(instanceParamDTO);
        onCreateEnvFromTemplate_FillParams(envName, instanceParam);
        
        // TODO ... check mandatory params + param validity + add meta parameters (creationDate, ..)
        
        LOG.info("create env '" + envName + "' from sourceTemplateName:" + sourceTemplateName + ", parameters:" + instanceParam.getParameters());
        FxNode templateInstanceParamNode = envTemplateInstanceParametersDTOMapper.formatNode(instanceParam);
        cloudEnvDir.mkdir();
        File templateInstanceParamFile = new File(cloudEnvDir, TEMPLATE_PARAM_BASEFILENAME + FxFileUtils.YAML_EXT);
        FxFileUtils.writeTree(templateInstanceParamFile, templateInstanceParamNode);
        
        // invalidate/fire change..
        if (_cacheListEnvs != null) {
            _cacheListEnvs = CopyOnWriteUtils.immutableCopyWithAdd(_cacheListEnvs, envName);
        }
        // init new env repo
        EnvResourceRepository envRepo = getEnvRepo(envName);
        
        return envRepo;
    }
    
    protected void onCreateEnvFromTemplate_FillParams(String cloudEnvName, EnvTemplateInstanceParameters instanceParam) {
        // overridable, default do nothing
    }

    // protected
    // ------------------------------------------------------------------------
    
    protected void onInitEnv(EnvResourceRepository envRepo) {
        envRepo.init();
    }
    
    protected EnvResourceRepository parseStdEnvResourcesTree(String envName) {
        // recursive scan dir/files <<baseEnvsDir>>/<<envName>>/**/*.[json|yaml]
        File envDir = new File(baseEnvsDir, envName);

        FxArrayNode rawRootNode = scanAndAppendRawContents(envDir, envName);
        
        return buildEnvResourceTreeRepository(envName, envDir, rawRootNode, null); 
    }

    protected FxArrayNode scanAndAppendRawContents(File envDir, String envName) {
        FxMemRootDocument rawEnvDoc = new FxMemRootDocument();
        FxArrayNode rawRootNode = rawEnvDoc.contentWriter().addArray();
        FxChildWriter rawNodesWriter = rawRootNode.insertBuilder();
        
        recursiveScanAndConcatenateRelativeFiles(rawNodesWriter, envDir, envName, envName + "/");
        return rawRootNode;
    }

    protected EnvResourceRepository parseCloudEnvResourcesTree(String envName) {
        File envDir = new File(baseEnvsDir, envName);
        
        // scan <<baseEnvsDir>>/<<envName>>/template-params.[json|yaml]
        EnvTemplateInstanceParameters templateParams = scanTemplateParamsFiles(envDir);
        
        // recursive scan dir/files  <<baseEnvsDir>>/Templates/<<sourceTemplateEnvName>>/**/*.[json|yaml]
        String sourceTemplateEnvName = templateParams.getSourceTemplateName();
        File sourceTemplateEnvDir = new File(baseEnvsDir, TEMPLATES_DIRNAME + "/" + sourceTemplateEnvName);

        FxArrayNode rawRootNode = scanAndAppendRawContents(sourceTemplateEnvDir, envName);

        return buildEnvResourceTreeRepository(envName, envDir, rawRootNode, templateParams); 
    }
    
    protected EnvResourceRepository buildEnvResourceTreeRepository(String envName, File envDir, FxNode rawRootNode, EnvTemplateInstanceParameters templateParams) {
        EnvResourceRepository res = new EnvResourceRepository(envName, envDir, templateParams, 
            rawRootNode, funcRegistry, resourceTypeRepository);
        // cf next... "res.init();" called from outer (may need registerCtxVar ...)
        return res;
    }
    
    protected void recursiveScanAndConcatenateRelativeFiles(FxChildWriter resultWriter, File dir, String envName, String currPathId) {
        File[] files = dir.listFiles();
        if (files == null) return; // dir not found?
        for(File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith(".")) continue;
            if (file.isDirectory()) {
                String childPathId = currPathId + fileName + "/";
                // recurse in sub dir
                recursiveScanAndConcatenateRelativeFiles(resultWriter, file, envName, childPathId);
            } else {
                // process file
                if (FxFileUtils.isSupportedFileExtension(file)) {
                    int indexExtension = fileName.lastIndexOf('.');
                    String baseFilename = (indexExtension != -1)? fileName.substring(0, indexExtension) : fileName;
                    String childPathId = currPathId + ((baseFilename.equals(ENV_BASEFILENAME))? "" : baseFilename + "/");

                    // parse json/yaml file + replace relativeId + concatenate results to resultWriter
                    processFxTreeFile(resultWriter, file, envName, childPathId);
                } else {
                    // ignore unrecognised file suffix
                    LOG.debug("unrecognized file suffix .. ignore " + file);
                }
            }
        }
        
    }

    protected void processFxTreeFile(FxChildWriter resultWriter, File file, String envName, String pathId) {
        if (file.getName().startsWith(TEMPLATE_PARAM_BASEFILENAME)
                || file.getName().startsWith(ENV_TEMPLATE_DECSCR_BASEFILENAME)
                ) {
            // special skip for file "template-param" .yaml/.json  (expected for top level environment dir only?)
            return;
        }
        if (!file.exists() || !file.canRead()) {
            LOG.warn("can not read file content '" + file + "' ... skip");
            return;
        }
        
        // read json/yaml, and convert to raw in-memory tree
        FxNode tmpContent = FxFileUtils.readTree(file);
        
        // recursive replace "relativeId", and "id" by prepending current pathId
        // FxReplaceNodeCopyVisitor.copyWithReplaceTo(dest, template, varReplacements);
        FxNodeCopyVisitor relativeIdTransformCopier = new FxNodeCopyVisitor() {
            @Override
            public FxNode visitObj(FxObjNode src, FxChildWriter out) {
                FxObjNode res = out.addObj(); 
                for(Iterator<Map.Entry<String, FxNode>> iter = src.fields(); iter.hasNext(); ) {
                    Entry<String, FxNode> srcFieldEntry = iter.next();
                    String fieldname = srcFieldEntry.getKey();
                    FxNode srcValue = srcFieldEntry.getValue();
                    
                    if ("relativeId".equals(fieldname)) {
                        String relativeId = FxNodeValueUtils.nodeToString(srcValue);
                        String id = pathId + relativeId;
                        // replace field name "relativeId" by "id" + prepend content value by pathId
                        // example:  { relativeId: "a/b" } => { id: "pathId/a/b" }
                        res.put("id", id);
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
                        res.put("id", id);
                    } else {
                        // recurse copy object field value
                        FxChildWriter outChildAdder = res.putBuilder(fieldname);
                        srcValue.accept(this, outChildAdder);
                    }
                }
                return res;
            }
        };

        tmpContent.accept(relativeIdTransformCopier, resultWriter);
    }

    /**
     * scan for "template-param.yaml" / ".json" files .. and load "params" field content as a map of named parameters
     * example:
     * template-param.yaml
     * <PRE>
     * params:
     *   key1: value1
     *   key2: value2
     * </PRE>
     * template-param.json
     * <PRE>
     * {
     *   "params": {
     *     key3: value3,
     *     key4: value4
     *   },
     *   
     *   "metaParams": {
     *     description: "temporary env for testing 'JIRA-123' feature",
     *     retention: "2016-01-31",
     *     creationDate: "2016-01-24",
     *     creationUser: "me"
     *   }
     * }
     * </PRE>
     * 
     * => concatenate and obtain params <PRE>{ key1:value1, key2:value2, key3:value3, key4:value4 }</PRE>
     */
    protected EnvTemplateInstanceParameters scanTemplateParamsFiles(File dir) {
        EnvTemplateInstanceParameters.Builder res = new EnvTemplateInstanceParameters.Builder();
        
        File[] files = dir.listFiles();
        if (files == null) return res.build(); // dir not found?
        for(File file : files) {
            String fileName = file.getName();
            if (fileName.startsWith(".")) continue;
            if (file.isFile() && fileName.startsWith(TEMPLATE_PARAM_BASEFILENAME)
                    && FxFileUtils.isSupportedFileExtension(file)
                    ) {
                // read json/yaml, and merge into parameter builder
                FxObjNode tmpContent = (FxObjNode) FxFileUtils.readTree(file);
                envTemplateInstanceParametersDTOMapper.parseMergeNode(res, tmpContent);
            }
        }

        return res.build();
    }
    
}
