package org.cmdb4j.core.command.impl;

import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.command.annotation.QueryResourceCommand;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

public class ResourceCommandRegistryTest {

    protected static class MockTomcatCommands {
        
        @QueryResourceCommand(name="start", resourceType="Tomcat")
        public static void tc_start(CommandExecutionCtx ctx, Resource target) {
            // do nothing
        }

        @QueryResourceCommand(name="stop", resourceType="Tomcat")
        public static void tc_stop(CommandExecutionCtx ctx, Resource target) {
            // do nothing
        }
        
        @QueryResourceCommand(name="start_sleep_default", resourceType="Tomcat")
        public static void tc_start_sleep_default(CommandExecutionCtx ctx, Resource target, 
                @Param(name="waitMillis", required=false, defaultValue="15000") int waitMillis) {
            // do nothing
        }
        
        @QueryResourceCommand(name="start_sleep", resourceType="Tomcat")
        public static void tc_start_sleep(CommandExecutionCtx ctx, Resource target, 
                @Param(name="waitMillis", required=true) int waitMillis) {
            // do nothing
        }
        
    }
    
        
    public static ResourceCommandRegistry createMock(ResourceTypeRepository resourceTypeRepo) {
        ResourceCommandRegistry res = new ResourceCommandRegistry();
        AnnotatedMethodToCommandInfoHelper cmdScanner = new AnnotatedMethodToCommandInfoHelper(resourceTypeRepo);
        res.addCommands(cmdScanner.scanStaticMethods(MockTomcatCommands.class));
        return res;
    }
}
