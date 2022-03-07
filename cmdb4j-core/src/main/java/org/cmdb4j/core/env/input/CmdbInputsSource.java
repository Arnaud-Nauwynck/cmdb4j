package org.cmdb4j.core.env.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.cmdb4j.core.dtomapper.env.EnvTemplateDescrDTOMapper;
import org.cmdb4j.core.env.template.EnvTemplateDescr;
import org.cmdb4j.core.env.template.EnvTemplateInstanceParameters;
import org.cmdb4j.core.env.template.EnvTemplateInstanceParametersMapper;
import org.cmdb4j.core.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import fr.an.fxtree.impl.helper.CacheFxKeyNodeStore;
import fr.an.fxtree.impl.helper.FxKeyNodeFileStore;
import fr.an.fxtree.impl.helper.IFxKeyNodeStore;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

/**
 * Source reader helper for yaml|json environment input dirs
 * 
 * read files from 
 * <PRE><<inputDir>> / <<envName>> / ** / *.(json|yaml)</PRE>
 * 
 * The idea is to have like in puppet hiera, a list of input directories... 1 repository per team
 */
public class CmdbInputsSource {

    private static final Logger LOG = LoggerFactory.getLogger(CmdbInputsSource.class);
    
    public static final String DEFAULT_DIRNAME = "Default";
    public static final String TEMPLATES_DIRNAME = "Templates";
    public static final String CLOUD_DIRNAME = "cloud"; 

    private File baseCmdbsDir;
    
    private ImmutableList<InputDirSource> inputDirSources;

    private File defaultCheckoutDir;

    private EnvTemplateDescrDTOMapper envTemplateDescrDTOMapper = new EnvTemplateDescrDTOMapper();

    private EnvTemplateInstanceParametersMapper templateParamsMapper = new EnvTemplateInstanceParametersMapper();
    
    private ImmutableSet<String> _cacheListEnvTemplates;
    
    private Map<String,EnvTemplateDescr> _cacheEnvTemplateDescr = Collections.synchronizedMap(new HashMap<>());
    
    // ------------------------------------------------------------------------
    
    public CmdbInputsSource(File baseCmdbsDir, 
            EnvsInputDirsConfig envsInputDirsConfig
            ) {
        this.baseCmdbsDir = baseCmdbsDir;
        ImmutableList.Builder<InputDirSource> inputDirSourcesBuilder = ImmutableList.builder(); 
        File foundDefDir = null;
        for(InputDirConfig dirConfig : envsInputDirsConfig.getInputDirs()) {
            String dirConfigName = dirConfig.getName();
            String checkoutDir = dirConfig.getCheckoutDir();
            File checkoutDirFile = (checkoutDir != null && !checkoutDir.isEmpty())? 
                    new File(checkoutDir) : new File(baseCmdbsDir, dirConfigName);
            if (! checkoutDirFile.exists()) {
                LOG.error("inputDir not found: '" + checkoutDirFile.getAbsolutePath() + "' for " + dirConfigName);
                continue; // TODO shoudld throw
            }
            if (! checkoutDirFile.isDirectory()) {
                LOG.error("inputDir not a directory: '" + checkoutDirFile + "' for " + dirConfigName);
                continue; // TODO shoudld throw
            }
            
            String scmUsername = dirConfig.getScmUserName();
            String scmUserCredential = dirConfig.getScmUserCredential();

            InputDirSource dirSource = new InputDirSource(
            		dirConfig, checkoutDirFile, scmUsername, scmUserCredential);
            inputDirSourcesBuilder.add(dirSource);

            if (dirConfigName.equalsIgnoreCase("default")
                    || dirConfigName.equalsIgnoreCase("cmdb_defaults")) {
                foundDefDir = dirSource.getCheckoutDir();
            }
        }
        this.defaultCheckoutDir = foundDefDir;
        this.inputDirSources = inputDirSourcesBuilder.build();

        if (foundDefDir == null && envsInputDirsConfig.getInputDirs().size() == 1) {
            foundDefDir = inputDirSources.get(0).getCheckoutDir();
        }
        if (foundDefDir == null) {
            LOG.error("defaultCheckoutDir not found, cf envs-input.yaml': " + envsInputDirsConfig);
        }
    }
    
    public static CmdbInputsSource newSimple(File dir) {
        InputDirConfig inputDirCfg = new InputDirConfig("default", dir.getAbsolutePath(), // 
        		null, null, null, // url, username, password 
        		null, null, // dept, team 
        		null, null); // includes, excludes
        EnvsInputDirsConfig cfg = new EnvsInputDirsConfig(ImmutableList.of(inputDirCfg));
        return new CmdbInputsSource(dir, cfg);
    }

    public String toStringInfo() {
        String res = "baseCmdbsDir: '" + baseCmdbsDir + "', defaultDir: '" + defaultCheckoutDir + "', inputs: " + inputDirSources.size() + " elt(s) :\n";
        for (InputDirSource inputDirSource : inputDirSources) {
            res += " " + inputDirSource.getConfig().getName() + " '" + inputDirSource.getCheckoutDir() + "'\n";
        }
        return res;
    }
    
    // ------------------------------------------------------------------------
    
    public void purge() {
        _cacheListEnvTemplates = null;
        _cacheEnvTemplateDescr.clear();
    }

    // ok when getting only 1 file the shared "default" checkout dir
    // otherwise should scan files or use dynamic(redis) keyValue store get/put .. 
    public File getDefaultCheckoutDir() {
        return defaultCheckoutDir;
    }
    
    public ImmutableList<InputDirSource> getInputDirSources() {
        return inputDirSources;
    }
    
    public IFxKeyNodeStore buildFxKeyStoreFor(String storeKeyPath) {
        IFxKeyNodeStore res = new CacheFxKeyNodeStore(
                new FxKeyNodeFileStore(new File(defaultCheckoutDir, storeKeyPath)),
                60000L);
        return res;
    }
    
    public void updateScm() {
        Map<String,Exception> exs = new HashMap<>();
        for(InputDirSource dirSource : inputDirSources) {
            try {
                dirSource.updateScm();
            } catch(Exception ex) {
                exs.put(dirSource.getConfig().getName(), ex);
            }
        }
        if (! exs.isEmpty()) {
            throw new RuntimeException("Failed updateScm: " + exs);
        }
    }

    public static boolean isReservedEnvDirName(String name) {
        return DEFAULT_DIRNAME.equalsIgnoreCase(name)
                || TEMPLATES_DIRNAME.equalsIgnoreCase(name)
                || CLOUD_DIRNAME.equalsIgnoreCase(name);
    }
        
    public Set<String> listEnvs() {
        Set<String> res = new LinkedHashSet<>();
        for(InputDirSource dirSource : inputDirSources) {
            res.addAll(dirSource.listEnvs());
        }
        return res;
    }

    public List<ResourceFileContent> scanEnvResourceFiles(String envName) {
        List<ResourceFileContent> res = new ArrayList<>();
        for(InputDirSource dirSource : inputDirSources) {
            res.addAll(dirSource.scanEnvResourceFxFiles(envName));
        }
        return res;
    }

    // Management of Env Templates
    // ------------------------------------------------------------------------

    public ImmutableSet<String> listEnvTemplates() {
        ImmutableSet<String> res = _cacheListEnvTemplates;
        if (res == null) {
            res = doListEnvTemplates();
            _cacheListEnvTemplates = res;
        }
        return res;
    }

    protected ImmutableSet<String> doListEnvTemplates() {
        Set<String> res = new LinkedHashSet<>();
        for(InputDirSource dirSource : inputDirSources) {
            res.addAll(dirSource.listEnvTemplates());
        }
        return ImmutableSet.copyOf(res);
    }

    
    public List<EnvTemplateDescr> listEnvTemplateDescrs() {
        List<EnvTemplateDescr> res = new ArrayList<>();
        ImmutableSet<String> templateNames = listEnvTemplates();
        for (String e : templateNames) {
            try {
                EnvTemplateDescr resElt = getEnvTemplateDescr(e);
                if (resElt != null) {
                    res.add(resElt);
                }
            } catch (Exception ex) {
                LOG.error("Failed listEnvTemplateDescrs() on env:" + e + " ..ignore, no rethrow! ", ex);
            }
        }
        return res;
    }
    
    public EnvTemplateDescr getEnvTemplateDescr(String templateName) {
        EnvTemplateDescr res = _cacheEnvTemplateDescr.get(templateName);
        if (res == null) {
            res = doGetEnvTemplateDescr(templateName);
            _cacheEnvTemplateDescr.put(templateName, res);
        }
        return res;
    }
    
    protected EnvTemplateDescr doGetEnvTemplateDescr(String templateName) {
        FxNode templateDescrNode = null;
        List<ResourceFileContent> contents = new ArrayList<>();
        for(InputDirSource dirSource : inputDirSources) {
            templateDescrNode = dirSource.findEnvTemplateDescr(templateName, contents);
            
            if (templateDescrNode != null) {
                break; // otherwise merge..
            }
        }
        if (templateDescrNode == null) {
            throw new RuntimeException("template descr file not found for template env: '" + templateName + "'");
        }

        EnvTemplateDescr res = envTemplateDescrDTOMapper.fromFxTree(templateName, templateDescrNode, contents);
        return res;
    }

    
    // ------------------------------------------------------------------------
    

    /**
     * read for "template-param.(yaml|json)" files .. and load "params" field content as a map of named parameters
     * example:
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
    public EnvTemplateInstanceParameters readEnvTemplateInstanceParams(String envName) {
        EnvTemplateInstanceParameters.Builder res = new EnvTemplateInstanceParameters.Builder(envName);
        for(InputDirSource dirSource : inputDirSources) {
            FxObjNode templateParamNode = dirSource.findTemplateInstanceParams(envName); 
            if (templateParamNode != null) {
                templateParamsMapper.parseMergeNode(res, templateParamNode);
            }
        }
        return res.build();
    }
    
    public ResourceFileContentBytes readDefaultFileContent(String path) {
        byte[] data = null;
        FxSourceLoc loc = null;
        String defaultPath = "Default/" + path;
        File file = new File(defaultCheckoutDir, defaultPath);
        if (file.exists()) {
            data = IOUtils.readAllBytes(file);
            loc = new FxSourceLoc("cmdb_defaults", defaultPath);
        } else {
            for(InputDirSource dirSource : inputDirSources) {
                File dir = dirSource.getCheckoutDir();
                file = new File(dir, defaultPath);
                if (file.exists()) {
                    data = IOUtils.readAllBytes(file);
                    loc = new FxSourceLoc(dirSource.getConfig().getName(), defaultPath);
                }
            }
        }
        return new ResourceFileContentBytes(loc, data);
    }

    public File resolveDefaultFilePath(String path) {
    	File res = null;
        String defaultPath = "Default/" + path;
        File file = new File(defaultCheckoutDir, defaultPath);
        if (file.exists()) {
            res = file;
        } else {
            for(InputDirSource dirSource : inputDirSources) {
                File dir = dirSource.getCheckoutDir();
                file = new File(dir, defaultPath);
                if (file.exists()) {
                    res = file;
                    break;
                }
            }
        }
        return res;
    }

    
    public List<ResourceFileContent> scanDefaultResourceFxFiles(String defaultSubDir, Predicate<String> fileNamePredicate) {
        List<ResourceFileContent> res = new ArrayList<>();
        for(InputDirSource dirSource : inputDirSources) {
            res.addAll(dirSource.scanDefaultResourceFxFiles(defaultSubDir, fileNamePredicate));
        }
        return res;
    }

    public List<ResourceFileContentBytes> scanDeployableInfosYaml() {
        List<ResourceFileContentBytes> res = new ArrayList<>();
        for(InputDirSource dirSource : inputDirSources) {
            res.addAll(dirSource.scanDeployableInfosYaml());
        }
        return res;
    }
    
    public InputDirSource findInputDirSourceByName(String repoName) {
        for (InputDirSource dirSource : inputDirSources) {
            if (dirSource.getConfig().getName().equals(repoName)) {
                return dirSource;
            }
        }
        return null;
    }

    public InputDirSource getInputDirSourceByName(String repoName) {
        InputDirSource res = findInputDirSourceByName(repoName);
        if (res == null) {
            throw new IllegalArgumentException("dirSource not found '" + repoName + "'");
        }
        return res;
    }

}
