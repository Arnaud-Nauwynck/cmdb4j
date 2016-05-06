package org.cmdb4j.core.tst;

import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

public final class TomcatJvmProcessResourceTypesMock {

    public final ResourceType processType;
    public final ResourceType jvmProcessType;
    public final ResourceType tomcatType;
    
    public TomcatJvmProcessResourceTypesMock(ResourceTypeRepository typeRepo) {
        this.processType = typeRepo.getOrCreateType("Process");
        this.jvmProcessType = typeRepo.getOrCreateType("JvmProcess");
        jvmProcessType.registerSuperType(processType);
        this.tomcatType = typeRepo.getOrCreateType("Tomcat");
        tomcatType.registerSuperType(jvmProcessType);
    }
    
}
