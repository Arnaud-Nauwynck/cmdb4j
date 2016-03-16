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

import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxNodeValueUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.util.FxUtils;
import fr.an.fxtree.model.FxArrayNode;
import fr.an.fxtree.model.FxChildWriter;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

/**
 * repository of EnvResourceTreeRepository(ies), base on file system Json / Yaml pre-processed files.<br/>
 * <p/>
 * 
 * The list of environement is dynamic, obtained by scanning all sub-dir of the top level base directory,<br/>
 * with 3 special directories for handling "Default", "Templates" definition environment, and "cloud" template instance.
 * 
 * <H2>file system layout for multi-environements</H2>
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
public class EnvDirsResourceTreeRepository {
    
    private static final Logger LOG = LoggerFactory.getLogger(EnvDirsResourceTreeRepository.class);
    

    private static final String DEFAULT_DIRNAME = "Default";
    private static final String TEMPLATES_DIRNAME = "Templates";
    private static final String TEMPLATE_PARAM_BASEFILENAME = "template-param.";
    private static final String TEMPLATE_PARAM_DECL_BASEFILENAME = "template-param-decl.";
    private static final String ENV_BASEFILENAME = "env";
    
    private static final Predicate<String> DEFAULT_DIRNAME_ENV_ACCEPT = name -> ! (
            name.equals(DEFAULT_DIRNAME) || name.equals(TEMPLATES_DIRNAME) || name.startsWith("test-"));
    
    private File baseEnvsDir;
    
    private String cloudDirname = "cloud";
    
    
    private Predicate<String> dirnameEnvAccept = DEFAULT_DIRNAME_ENV_ACCEPT;
    
    protected ResourceTypeRepository resourceTypeRepository;

    private List<String> _cacheListEnvs;

    private Map<String, EnvResourceTreeRepository> _cacheEnv2Repo = Collections.synchronizedMap(new HashMap<>());

    // ---------------------------------------------------------------------- --

    public EnvDirsResourceTreeRepository(File baseEnvsDir, ResourceTypeRepository resourceTypeRepository) {
        this.baseEnvsDir = baseEnvsDir;
        this.resourceTypeRepository = resourceTypeRepository;
    }

    // ---------------------------------------------------------------------- --

    public void purge() {
        _cacheListEnvs = null;
        _cacheEnv2Repo.clear();
    }

    public List<String> listEnvs() {
        List<String> res = _cacheListEnvs;
        if (res == null) {
            res = new ArrayList<>();
            listEnvNamesInDir(res, baseEnvsDir, "");
            File cloudDir = new File(baseEnvsDir, cloudDirname);
            if (cloudDir.exists()) {
                listEnvNamesInDir(res, cloudDir, cloudDirname + "/");
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
            if (!file.isDirectory() || name.startsWith(".") || name.equals(cloudDirname)) {
                continue;
            }
            if (dirnameEnvAccept != null && ! dirnameEnvAccept.test(name)) {
                continue;
            }
            res.add(prefix + name);
        }
    }

    public EnvResourceTreeRepository getEnvTreeRepo(String envName) {
        EnvResourceTreeRepository res = _cacheEnv2Repo.get(envName);
        if (res == null) {
            if (! envName.startsWith(cloudDirname + "/")) {
                res = parseStdEnvResourcesTree(envName);
            } else {
                res = parseCloudEnvResourcesTree(envName);
            }
            _cacheEnv2Repo.put(envName, res);
        }
        return res;
    }

    protected EnvResourceTreeRepository parseStdEnvResourcesTree(String envName) {
        FxMemRootDocument rawEnvDoc = new FxMemRootDocument();
        FxArrayNode rootNode = rawEnvDoc.contentWriter().addArray();
        FxChildWriter rawNodesWriter = rootNode.insertBuilder();
        File envDir = new File(baseEnvsDir, envName);
        
        // recursive scan dir/files and replace json/yaml fields "relativeId" and "id"
        recursiveScanAndConcatenateRelativeFiles(rawNodesWriter, envDir, envName, envName + "/");
        
        return new EnvResourceTreeRepository(resourceTypeRepository, envName, null, rootNode);
    }

    protected EnvResourceTreeRepository parseCloudEnvResourcesTree(String envName) {
        FxMemRootDocument rawEnvDoc = new FxMemRootDocument();
        FxArrayNode rootNode = rawEnvDoc.contentWriter().addArray();
        FxChildWriter rawNodesWriter = rootNode.insertBuilder();
        File envDir = new File(baseEnvsDir, envName);
        
        EnvTemplateInstanceParameters templateParams = scanTemplateParamsFiles(envDir);
        
        String sourceTemplateEnvName = templateParams.getTemplateSourceEnvName();
        File sourceTemplateEnvDir = new File(baseEnvsDir, TEMPLATES_DIRNAME + "/" + sourceTemplateEnvName);
        
        // recursive scan dir/files and replace json/yaml fields "relativeId" and "id"
        recursiveScanAndConcatenateRelativeFiles(rawNodesWriter, sourceTemplateEnvDir, envName, envName + "/");
                
        return new EnvResourceTreeRepository(resourceTypeRepository, envName, templateParams, rootNode);
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
                int indexExtension = fileName.lastIndexOf('.');
                String baseFilename = (indexExtension != -1)? fileName.substring(0, indexExtension) : fileName;
                String fileExtension = (indexExtension != -1)? fileName.substring(indexExtension+1, fileName.length()) : "";
                String childPathId = currPathId + ((baseFilename.equals(ENV_BASEFILENAME))? "" : baseFilename + "/");

                if (FxFileUtils.isSupportedFileExtension(fileExtension)) {
                    // parse json/yaml file + replace relativeId + concatenate results to resultWriter
                    processFxTreeFile(resultWriter, file, envName, childPathId);
                } else if (fileName.endsWith(".properties")) {
                    throw FxUtils.notImplYet();
                } else {
                    // ignore unrecognised file suffix
                    LOG.debug("unrecognized file suffix .. ignore " + file);
                }
            }
        }
        
    }

    protected void processFxTreeFile(FxChildWriter resultWriter, File file, String envName, String pathId) {
        if (file.getName().startsWith(TEMPLATE_PARAM_BASEFILENAME)
                || file.getName().startsWith(TEMPLATE_PARAM_DECL_BASEFILENAME)
                ) {
            // special skip for file "template-param" .yaml/.json  (expected for top level environment dir only?)
            return;
        }
        if (!file.exists() || !file.canRead()) {
            LOG.warn("can not read file content '" + file + "' ... skip");
            return;
        }
        
        // read json/yaml, and convert to raw in-memory tree
        FxMemRootDocument tmpDoc = new FxMemRootDocument();
        FxFileUtils.readTree(tmpDoc.contentWriter(), file);
        FxNode tmpContent = tmpDoc.getContent();
        
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
                    // && FxFileUtils.isSupportedFileExtension(fileExtension)
                    ) {
                processTemplateParamFile(res, file);
            }
        }

        return res.build();
    }

    protected void processTemplateParamFile(EnvTemplateInstanceParameters.Builder result, File file) {
        // read json/yaml, and convert to raw in-memory tree
        FxMemRootDocument doc = new FxMemRootDocument();
        FxFileUtils.readTree(doc.contentWriter(), file);
        FxObjNode tmpContent = (FxObjNode) doc.getContent();
        
        String templateSourceEnvName = FxNodeValueUtils.getString(tmpContent, "templateSourceEnvName");
        if (templateSourceEnvName != null) {
            result.templateSourceEnvName(templateSourceEnvName);
        }
        
        // extract "params" and "metaparams", concatenate to result
        FxObjNode paramsNode = FxNodeValueUtils.getObjOrThrow(tmpContent, "params");
        result.putAllTemplateParameters(paramsNode.fieldsHashMapCopy());

        FxObjNode metaParamsNode = FxNodeValueUtils.getObjOrNull(tmpContent, "metaparams");
        if (metaParamsNode != null) {
            result.putAllMetaParameters(metaParamsNode.fieldsHashMapCopy());
        }

    }

}
