package org.cmdb4j.core.store;

public interface IServerCredentialsStore {

    public String getServerUserById(String serverId);
    
    public String getServerPasswordById(String serverId);
    

}
