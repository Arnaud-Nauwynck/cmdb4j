package org.cmdb4j.core.command.impl;

import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.ResourceCommand.MethodResourceCommand;
import org.cmdb4j.core.command.annotation.Command;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

public class AnnotatedMethodToCommandInfoHelperTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected AnnotatedMethodToCommandInfoHelper sut = new AnnotatedMethodToCommandInfoHelper(resourceTypeRepository);
    
    protected static MutableInt countCmdNoParam = new MutableInt();
    protected static MutableInt countCmdInt = new MutableInt();
    protected static MutableInt countStaticCmd = new MutableInt();
    
    protected static class FooCommands {
        
        @Command(name="cmd_noParam", resourceType="Type1")
        public void cmdNoParam(CommandExecutionCtx ctx, Resource target) {
            countCmdNoParam.increment();
        }

        @Command(name="cmd_int", resourceType="Type1")
        public void cmdInt(CommandExecutionCtx ctx, Resource target, 
                @Param(required=false, defaultValue="0")  int param1) {
            countCmdInt.increment();
        }

        @Command(name="static_cmd", resourceType="Type1")
        public static void staticCmd(CommandExecutionCtx ctx, Resource target) {
            countStaticCmd.increment();
        }

    }
    
    @Test
    public void testScanObjectMethods() {
        // Prepare
        FooCommands fooCommands = new FooCommands();
        // Perform
        List<ResourceCommand> res = sut.scanObjectMethods(fooCommands, 
            (objMethod) -> new MethodResourceCommand(objMethod.commandInfo, objMethod.object, objMethod.method)
        );
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
        ResourceCommand cmd0 = res.get(0);
        ResourceCommand cmd1 = res.get(1);
        
        if (cmd0.getCommandInfo().getName().equals("cmd_int")) { //TODO.. need find by name
            ResourceCommand tmp = cmd0; cmd0 = cmd1; cmd1 = tmp;
        }
        Assert.assertEquals("cmd_noParam", cmd0.getCommandInfo().getName());
        Assert.assertEquals("cmd_int", cmd1.getCommandInfo().getName());
        
        CommandExecutionCtx ctx = null;
        Resource resource = null;
        
        // Prepare
        countCmdNoParam.setValue(0);
        // Perform
        cmd0.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countCmdNoParam.getValue());
        countCmdNoParam.setValue(0);

        // Prepare
        countCmdInt.setValue(0);
        // Perform
        cmd1.execute(ctx, resource, new Object[] { 123 });
        // Post-check
        Assert.assertEquals(1, countCmdInt.getValue());
        countCmdInt.setValue(0);
    }

    @Test
    public void testScanStaticMethods() {
        // Prepare
        // Perform
        List<ResourceCommand> res = sut.scanStaticMethods(FooCommands.class, 
            (objMethod) -> new MethodResourceCommand(objMethod.commandInfo, objMethod.object, objMethod.method)
        );
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(1, res.size());
        ResourceCommand staticCmd0 = res.get(0);
        Assert.assertEquals("static_cmd", staticCmd0.getCommandInfo().getName());
        
        CommandExecutionCtx ctx = null;
        Resource resource = null;
        
        // Prepare
        countStaticCmd.setValue(0);
        // Perform
        staticCmd0.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countStaticCmd.getValue());
        countStaticCmd.setValue(0);
    }

}
