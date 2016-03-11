package org.cmdb4j.core.tst;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

public final class TstResourceTypes1 {

    public final ResourceType serverType;
    public final ResourceType webserverType;
    public final ResourceType tomcatType;

    public final ResourceType dirFileServerType;
    public final ResourceType dirType;
    public final ResourceType fileType;

    public TstResourceTypes1(ResourceTypeRepository typeRepo) {
        serverType = typeRepo.getOrCreateType("server");
        
        webserverType = typeRepo.getOrCreateType("webserver");
        if (webserverType.getSuperType() == null) {
            webserverType.registerSuperType(serverType);
        }
        
        tomcatType = typeRepo.getOrCreateType("tomcat");
        if (tomcatType.getSuperType() == null) {
            tomcatType.registerSuperType(webserverType);
        }
        
        dirFileServerType = typeRepo.getOrCreateType("dirFileServer");
        if (dirFileServerType.getSuperType() == null) {
            dirFileServerType.registerSuperType(serverType);
        }

        fileType = typeRepo.getOrCreateType("file");
        dirType = typeRepo.getOrCreateType("dir");
        if (dirType.getSuperType() == null) {
            dirType.registerSuperType(fileType);
        }

    }
    
}
