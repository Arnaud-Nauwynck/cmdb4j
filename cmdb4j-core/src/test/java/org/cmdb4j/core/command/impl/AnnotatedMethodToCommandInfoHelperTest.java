package org.cmdb4j.core.command.impl;

import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.command.annotation.QueryResourceCommand;
import org.cmdb4j.core.command.annotation.StmtResourceCommand;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.Test;

public class AnnotatedMethodToCommandInfoHelperTest {

    protected ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected AnnotatedMethodToCommandInfoHelper sut = new AnnotatedMethodToCommandInfoHelper(resourceTypeRepository);
    
    protected static MutableInt countObjCmd1QueryNoParam = new MutableInt();
    protected static MutableInt countObjCmd2QueryInt = new MutableInt();
    protected static MutableInt countObjCmd3StmtNoParam = new MutableInt();
    protected static MutableInt countObjCmd4StmtInt = new MutableInt();
    protected static MutableInt countStaticQueryNoParam = new MutableInt();
    protected static MutableInt countStaticStmtNoParam = new MutableInt();
    
    protected static class FooCommands {
        
        @QueryResourceCommand(name="query_noParam", resourceType="Type1")
        public void cmd1QueryNoParam(CommandExecutionCtx ctx, Resource target) {
            countObjCmd1QueryNoParam.increment();
        }

        @QueryResourceCommand(name="query_int", resourceType="Type1")
        public void cmd2QueryInt(CommandExecutionCtx ctx, Resource target, 
                @Param(required=false, defaultValue="0")  int param1) {
            countObjCmd2QueryInt.increment();
        }

        @StmtResourceCommand(name="stmt_noParam", resourceType="Type1")
        public void cmd3StmtNoParam(CommandExecutionCtx ctx, Resource target) {
            countObjCmd3StmtNoParam.increment();
        }

        @StmtResourceCommand(name="stmt_int", resourceType="Type1")
        public void cmd4StmtInt(CommandExecutionCtx ctx, Resource target, 
                @Param(required=false, defaultValue="0")  int param1) {
            countObjCmd4StmtInt.increment();
        }
        
        @QueryResourceCommand(name="static_query1_noParam", resourceType="Type1")
        public static void staticQuery(CommandExecutionCtx ctx, Resource target) {
            countStaticQueryNoParam.increment();
        }

        @QueryResourceCommand(name="static_stmt1_noParam", resourceType="Type1")
        public static void staticStmt(CommandExecutionCtx ctx, Resource target) {
            countStaticStmtNoParam.increment();
        }
    }
    
    @Test
    public void testScanObjectMethods() {
        // Prepare
        FooCommands fooCommands = new FooCommands();
        // Perform
        List<ResourceCommand> res = sut.scanObjectMethods(fooCommands);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(4, res.size());
        ResourceCommand cmd1QueryNoParam = res.get(0);
        ResourceCommand cmd2QueryInt = res.get(1);
        ResourceCommand cmd3StmtNoParam = res.get(2);
        ResourceCommand cmd4StmtInt = res.get(3);
        
        if (cmd1QueryNoParam.getCommandInfo().getName().equals("query_int")) { //TODO.. need find by name
            ResourceCommand tmp = cmd1QueryNoParam; cmd1QueryNoParam = cmd2QueryInt; cmd2QueryInt = tmp;
        }
        Assert.assertEquals("query_noParam", cmd1QueryNoParam.getCommandInfo().getName());
        Assert.assertEquals("query_int", cmd2QueryInt.getCommandInfo().getName());
        
        CommandExecutionCtx ctx = null;
        Resource resource = null;
        
        // Prepare
        countObjCmd1QueryNoParam.setValue(0);
        // Perform
        cmd1QueryNoParam.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countObjCmd1QueryNoParam.getValue());
        countObjCmd1QueryNoParam.setValue(0);

        // Prepare
        countObjCmd2QueryInt.setValue(0);
        // Perform
        cmd2QueryInt.execute(ctx, resource, new Object[] { 123 });
        // Post-check
        Assert.assertEquals(1, countObjCmd2QueryInt.getValue());
        countObjCmd2QueryInt.setValue(0);
        
        // Prepare
        countObjCmd3StmtNoParam.setValue(0);
        // Perform
        cmd3StmtNoParam.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countObjCmd3StmtNoParam.getValue());
        countObjCmd3StmtNoParam.setValue(0);
        
        // Prepare
        countObjCmd4StmtInt.setValue(0);
        // Perform
        cmd4StmtInt.execute(ctx, resource, new Object[] { 123 });
        // Post-check
        Assert.assertEquals(1, countObjCmd4StmtInt.getValue());
        countObjCmd4StmtInt.setValue(0);
        
    }

    @Test
    public void testScanStaticMethods() {
        // Prepare
        // Perform
        List<ResourceCommand> res = sut.scanStaticMethods(FooCommands.class);
        // Post-check
        Assert.assertNotNull(res);
        Assert.assertEquals(2, res.size());
        ResourceCommand staticQuery1 = res.get(0);
        Assert.assertEquals("static_query1_noParam", staticQuery1.getCommandInfo().getName());
        ResourceCommand staticStmt1 = res.get(1);
        Assert.assertEquals("static_stmt1_noParam", staticStmt1.getCommandInfo().getName());
        
        CommandExecutionCtx ctx = null;
        Resource resource = null;
        
        // Prepare
        countStaticQueryNoParam.setValue(0);
        // Perform
        staticQuery1.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countStaticQueryNoParam.getValue());
        countStaticQueryNoParam.setValue(0);

        // Prepare
        countStaticStmtNoParam.setValue(0);
        // Perform
        staticStmt1.execute(ctx, resource, new Object[0]);
        // Post-check
        Assert.assertEquals(1, countStaticStmtNoParam.getValue());
        countStaticStmtNoParam.setValue(0);
    }

}
