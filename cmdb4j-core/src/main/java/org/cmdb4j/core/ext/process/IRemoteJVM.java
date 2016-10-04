package org.cmdb4j.core.ext.process;

public interface IRemoteJVM extends IPidStatusProvider {

    // cf IPidStatusProvider .. public int getPid();
    
    public String takeThreadDump();
    
}
