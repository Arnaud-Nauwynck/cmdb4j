package org.cmdb4j.server.model;


import java.util.Properties;

import javax.annotation.PostConstruct;

import org.cmdb4j.core.store.IServerCredentialsStore;
import org.cmdb4j.server.util.UserHomePropertiesLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


/**
 * 
 */
@Component
@Slf4j
public class ServerCredentialsStore implements IServerCredentialsStore {

    @Value("${passwordPropertiesFile}")
    private String passwordPropertiesFile;
    
    private Properties cachedPasswords;
    
    // ------------------------------------------------------------------------

    public ServerCredentialsStore() {
    }

    @PostConstruct
    public void onInit() {
        log.info("loading credentials file:" + passwordPropertiesFile);
        this.cachedPasswords = UserHomePropertiesLoader.doLoadProperties(passwordPropertiesFile);
    }
    
    // ------------------------------------------------------------------------

    public Properties getProperties() {
        return cachedPasswords;
    }

    @Override
    public String getServerUserById(String serverId) {
        String res = (String) getProperties().get(serverId + ".user");
        return res;
    }

    @Override
    public String getServerPasswordById(String serverId) {
        String res = (String) getProperties().get(serverId + ".password");
        return res;
    }
}
