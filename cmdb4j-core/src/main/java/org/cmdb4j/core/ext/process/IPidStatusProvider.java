package org.cmdb4j.core.ext.process;

import org.cmdb4j.core.ext.process.server.ServerStatusDTO;

public interface IPidStatusProvider {

    public boolean isSupportPid();
    public int getPid();
    
    public boolean isSupportServerStatus();
    public void getServerStatus(ServerStatusDTO res);

}
