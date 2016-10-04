package org.cmdb4j.core.ext.process;


public interface IStartable extends IPidStatusProvider {

    public boolean isSupportStartStop();
    
    public void start(CommandResultDTO res);
    public void stop(CommandResultDTO res);

}
