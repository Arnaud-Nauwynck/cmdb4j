package org.cmdb4j.core.env.input;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.fxtree.format.FxFileUtils;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

public class InputDirSource {

    private static final Logger LOG = LoggerFactory.getLogger(InputDirSource.class);

    public static final String DEFAULT_DIRNAME = "Default";
    public static final String TEMPLATES_DIRNAME = "Templates";
    public static final String CLOUD_DIRNAME = "cloud"; 

    // filter for env file names (cf also test on yaml|json) 
    private static final Predicate<String> ENV_FILENAME_PREDICATE = (fileName) -> fileName.startsWith("env");
    
    private static final String ENV_TEMPLATE_DESCR_BASEFILENAME = "template-descr";
    private static final String TEMPLATE_PARAM_BASEFILENAME = "template-param";

    private InputDirConfig config;
    
    private File checkoutDir;
    
    private String scmUsername;
    private String scmUserPassword;
    
    // ------------------------------------------------------------------------
    
    public InputDirSource(InputDirConfig config, File checkoutDir, String scmUsername, String scmUserPassword) {
        this.config = config;
        this.checkoutDir = checkoutDir;
        this.scmUsername = scmUsername;
        this.scmUserPassword = scmUserPassword;
    }

    // ------------------------------------------------------------------------

    public InputDirConfig getConfig() {
        return config;
    }
    
    public File getCheckoutDir() {
        return checkoutDir;
    }
    
    public void updateScm() {
        final String scmUrl = config.getScmUrl();
        if (scmUrl == null) {
            return;
        }
        File gitDir = new File(checkoutDir, ".git");
        if (! gitDir.exists() || ! gitDir.isDirectory()) {
            LOG.warn("not a valid git clone repository: " + checkoutDir + " .. expecting git clone " + scmUrl);
            return;
        }
        // perform "git pull" with user/pass..
        LOG.info("perform git pull for repo " + scmUrl + " in dir: " + checkoutDir);
        Git git;
        try {
            git = Git.open(checkoutDir);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to open git repo '" + checkoutDir + "'", ex);
        }
        PullCommand pc = git.pull();
        pc.setRebase(true);
        pc.setStrategy(MergeStrategy.RESOLVE);
        pc.setCredentialsProvider(new UsernamePasswordCredentialsProvider(scmUsername, scmUserPassword));

        PullResult pullResult;
        try {
            pullResult = pc.call();
        } catch (Exception ex) {
            String msg = "Failed git pull from repo " + scmUrl + " : " + ex.getMessage();
            LOG.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
        if (! pullResult.isSuccessful()) {
            LOG.error("Failed git pull from repo " + scmUrl + " : " + pullResult); 
        }
    }

    
    public Set<String> listEnvs() {
        Set<String> res = new LinkedHashSet<>();
        File[] files = checkoutDir.listFiles();
        if (files != null && files.length > 0) {
            for(File file : files) {
                String name = file.getName();
                if (! file.isDirectory() || name.startsWith(".") 
                        || CmdbInputsSource.isReservedEnvDirName(name)) {
                    continue;
                }
                if (! config.acceptTextIncludesExcludes(name)) {
                    continue;
                }
                res.add(name);
            }
        }
        return res;
    }
    

    public List<ResourceFileContent> scanEnvResourceFxFiles(String envName) {
        List<ResourceFileContent> res = new ArrayList<>();

        File inputEnvDir = new File(checkoutDir, envName);
        if (! inputEnvDir.exists() || ! inputEnvDir.isDirectory()) {
            return res;
        }
        if (! config.acceptTextIncludesExcludes(envName)) {
            return res;
        }
        
        recursiveScanResourceFxFiles(res, inputEnvDir, envName + "/", ENV_FILENAME_PREDICATE);

        return res;
    }

    public List<ResourceFileContent> scanDefaultResourceFxFiles(String defaultSubDir, Predicate<String> fileNamePredicate) {
        List<ResourceFileContent> res = new ArrayList<>();
        String subDir = "Default/" + defaultSubDir;
        File inputEnvDir = new File(checkoutDir, subDir);
        if (! inputEnvDir.exists() || ! inputEnvDir.isDirectory()) {
            return res;
        }
        recursiveScanResourceFxFiles(res, inputEnvDir, subDir, fileNamePredicate);
        return res;
    }

    
    protected void recursiveScanResourceFxFiles(List<ResourceFileContent> res, 
            File currDir, String currPathId, Predicate<String> fileNamePredicate
            ) {
        File[] files = currDir.listFiles();
        if (files == null) {
        	return; // dir not found?
        }
        for (File childFile : files) {
            String fileName = childFile.getName();
            if (fileName.startsWith(".")) {
                continue;
            }
            String childPathId = ((currPathId != null && !currPathId.isEmpty())? 
                    currPathId + ((!currPathId.endsWith("/"))? "/" : "") 
                    : "" ) + fileName;
            if (childFile.isDirectory()) {
                // recurse in sub dir
                recursiveScanResourceFxFiles(res, childFile, childPathId, fileNamePredicate);
            } else {
                // process file
                if (FxFileUtils.isSupportedFileExtension(childFile)
                        && fileNamePredicate.test(fileName)) {
                    if (!childFile.canRead()) {
                        LOG.warn("can not read file content '" + childFile + "' ... skip");
                        continue;
                    }

                    FxSourceLoc loc = new FxSourceLoc(config.getName(), childPathId);
                    FxMemRootDocument doc = new FxMemRootDocument(loc);

                    // parse json/yaml file
                    FxNode fileContent;
                    try {
                        // fileContent = FxFileUtils.readTree(childFile);
                        fileContent = FxFileUtils.readTree(doc.contentWriter(), childFile, loc);
                    } catch(Exception ex) {
                        LOG.error("Failed to parse yaml/json file '" + childFile + "' ex:" + ex.getMessage() + " => ignore file !!! ");
                        continue;
                    }

                    res.add(new ResourceFileContent(loc, fileContent));
                }// else ignore other non-env files
            }
        }        
    }

    public Set<String> listEnvTemplates() {
        Set<String> res = new HashSet<>();
        File dir = new File(checkoutDir, CmdbInputsSource.TEMPLATES_DIRNAME);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for(File file : files) {
                String name = file.getName();
                if (! file.isDirectory() || name.startsWith(".") 
                        || CmdbInputsSource.isReservedEnvDirName(name)) {
                    continue;
                }
                if (! config.acceptTextIncludesExcludes(name)) { // useless for template?
                    continue;
                }
                res.add(name);
            }
        }
        return res;
    }

    public FxNode findEnvTemplateDescr(String templateName, List<ResourceFileContent> contents) {
        String templatePath = TEMPLATES_DIRNAME + "/" + templateName;
        
        File templateDir = new File(checkoutDir, templatePath);
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            return null;
        }
//            if (! dirConfig.acceptTextIncludesExcludes(templatePath)) {
//                continue;
//            }
        // read file "<<inputDirs>>/Templates/<<templateName>>/env-template-descr.yaml"
        FxNode templateDescrNode = FxFileUtils.readFirstFileWithSupportedExtension(templateDir, ENV_TEMPLATE_DESCR_BASEFILENAME);
        
        // scan files "<<inputDirs>>/Templates/<<templateName>>/**/env*.(yaml|json)"
        recursiveScanResourceFxFiles(contents, templateDir, templatePath + "/", ENV_FILENAME_PREDICATE);
        
        return templateDescrNode;
    }

    public FxObjNode findTemplateInstanceParams(String envName) {
        File templatesDir = new File(checkoutDir, TEMPLATES_DIRNAME);
        if (! templatesDir.exists() || ! templatesDir.isDirectory()) {
            return null;
        }
        File templateEnvDir = new File(templatesDir, envName);
        if (! templateEnvDir.exists() || ! templateEnvDir.isDirectory()) {
            return null;
        }
        if (! config.acceptTextIncludesExcludes(envName)) {
            return null;
        }
        String fileName = TEMPLATE_PARAM_BASEFILENAME + ".yaml";
        File file = new File(templateEnvDir, fileName);
        if (! file.exists() || ! file.isFile()) {
            return null;
        }
        // ok, scan */Templates/<<env>>/template-param.(yaml|json)
        // and merge into parameter builder
        FxSourceLoc source = new FxSourceLoc("cmdb_default/Templates", fileName);
        FxObjNode res = (FxObjNode) FxFileUtils.readTree(file, source);
        return res;
    }
    
    public List<ResourceFileContentBytes> scanDeployableInfosYaml() {
    	List<ResourceFileContentBytes> res = new ArrayList<>();
    	File[] files = checkoutDir.listFiles((dir,name) -> 
    		name.startsWith("deployable-info-") && name.endsWith(".yaml"));
    	if (files != null) {
	    	for(File file : files) {
	    		byte[] data;
				try {
					data = FileUtils.readFileToByteArray(file);
				} catch (IOException ex) {
					LOG.error("Failed to read file " + file, ex);
					continue;
				}
	    		FxSourceLoc loc = new FxSourceLoc(checkoutDir.getName(), file.getName());
	    		res.add(new ResourceFileContentBytes(loc, data));
            }
        }
    	return res;
    }
    
}
