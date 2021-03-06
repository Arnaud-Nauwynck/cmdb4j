package org.cmdb4j.core.command.impl;

import java.io.File;

import org.cmdb4j.core.command.CommandCtx;
import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommandParamsExecutable;
import org.cmdb4j.core.command.ResourceCommandRegistry;
import org.cmdb4j.core.command.ResourceCommandsMock;
import org.cmdb4j.core.command.commandinfo.ResourceCommandParamInfo;
import org.cmdb4j.core.env.EnvDirsResourceRepositories;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.impl.util.FxNodeCheckUtils;

public class ResourceCommandParserTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected File baseEnvsDir = new File("src/test/envsDir");
    protected EnvDirsResourceRepositories envsRepo = new EnvDirsResourceRepositories(baseEnvsDir, resourceTypeRepository, FxStdFuncs.stdFuncRegistry());

    private ResourceTypeRepository resourceTypeRepo = new ResourceTypeRepository();

    protected ResourceCommandRegistry resourceTypeToCmdRegistry = 
            ResourceCommandsMock.createMock(resourceTypeRepo);
    
    protected ResourceCommandParser sut = new ResourceCommandParser(envsRepo, resourceTypeToCmdRegistry);
    
    @Test
    public void testParse() {
        // Prepare
        String resourceId = "DEV1/localhost/tomcat1";
        String cmdLine = resourceId + " start";
        // Perform
        ResourceCommandParamsExecutable res = sut.parse(cmdLine);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(resourceId, res.getResource().getId().toString());
        Assert.assertEquals("start", res.getResourceCommand().getCommandName());
        Assert.assertEquals(0, res.getRawParamNodes().length);
        
        CommandCtx cmdCtx = new CommandCtx("test");
        CommandExecutionCtx cmdExecCtx = new CommandExecutionCtx(cmdCtx);
        Object resExec = res.execute(cmdExecCtx);
        
        // Prepare
        cmdLine = resourceId + " start_sleep waitMillis=10000";
        // Perform
        res = sut.parse(cmdLine);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(resourceId, res.getResource().getId().toString());
        Assert.assertEquals("start_sleep", res.getResourceCommand().getCommandName());
        Assert.assertEquals(1, res.getRawParamNodes().length);
        FxNodeCheckUtils.checkIntEquals(10000, res.getRawParamNodes()[0]);
        
        // Prepare
        cmdLine = resourceId + " start_sleep_default waitMillis=10000";
        // Perform
        res = sut.parse(cmdLine);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(resourceId, res.getResource().getId().toString());
        Assert.assertEquals("start_sleep_default", res.getResourceCommand().getCommandName());
        Assert.assertEquals(1, res.getRawParamNodes().length);
        FxNodeCheckUtils.checkIntEquals(10000, res.getRawParamNodes()[0]);
        
        // Prepare
        cmdLine = resourceId + " start_sleep_default";
        // Perform
        res = sut.parse(cmdLine);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(resourceId, res.getResource().getId().toString());
        Assert.assertEquals("start_sleep_default", res.getResourceCommand().getCommandName());
        ResourceCommandParamInfo defaultWaitMillis = res.getResourceCommand().getCommandInfo().getParamAt(0);
        Assert.assertEquals(1, res.getRawParamNodes().length);
        FxNodeCheckUtils.checkIntEquals(Integer.parseInt(defaultWaitMillis.getDefaultValue()), res.getRawParamNodes()[0]);
    }
}
