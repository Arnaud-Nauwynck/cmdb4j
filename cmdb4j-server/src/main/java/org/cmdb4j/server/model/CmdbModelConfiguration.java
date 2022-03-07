package org.cmdb4j.server.model;


import java.io.File;

import org.cmdb4j.core.env.CmdbEnvRepository;
import org.cmdb4j.core.env.DynStoreService;
import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.EnvsInputDirsConfig;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.cmdb4j.core.store.IEnvValueDecrypter;
import org.cmdb4j.core.store.IServerCredentialsStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.an.fxtree.format.json.FxJsonUtils;
import fr.an.fxtree.format.yaml.FxYamlUtils;
import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;
import lombok.val;

@Configuration
public class CmdbModelConfiguration {

    @Value("${baseWorkDir}")
    private File baseWorkDir;

    @Value("${envsInputYamlFile}")
    private File envsInputYamlFile;

    
    @Bean
    public CmdbEnvRepository endDirsResourceRepositories( //
    		IServerCredentialsStore credentialsStore //
    		) {
        FxSourceLoc loc = new FxSourceLoc("settings", envsInputYamlFile.getAbsolutePath());
        FxNode envsInputTree = FxYamlUtils.readTree(envsInputYamlFile, loc);
        EnvsInputDirsConfig envsInputDirsConfig = FxJsonUtils.treeToValue(EnvsInputDirsConfig.class, envsInputTree);
        val envsInputSource = new CmdbInputsSource(baseWorkDir, envsInputDirsConfig);

        DynStoreService dynStoreService = null; // TODO ARN
        
        ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();  // TODO ARN
        
        FxNodeFuncRegistry funcRegistry = FxStdFuncs.stdFuncRegistry();  // TODO ARN
        
        IEnvValueDecrypter envValueDecrypter = null; // TODO ARN
        
        return new CmdbEnvRepository(envsInputSource, //
                dynStoreService, resourceTypeRepository, funcRegistry, //
                envValueDecrypter);
    }
    
}
