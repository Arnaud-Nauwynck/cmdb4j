package org.cmdb4j.core.tst;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

public final class TstResourceTypes2 {

    public final ResourceType jettyType;
    public final ResourceType apacheType;


    public TstResourceTypes2(ResourceTypeRepository typeRepo) {
        ResourceType webserverType = typeRepo.getOrCreateType("webserver");
        
        jettyType = typeRepo.getOrCreateType("jetty");
        if (jettyType.getSuperType() == null) {
            jettyType.registerSuperType(webserverType);
        }
        
        apacheType = typeRepo.getOrCreateType("apache");
        if (apacheType.getSuperType() == null) {
            apacheType.registerSuperType(webserverType);
        }
    }
    
}
