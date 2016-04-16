package org.cmdb4j.core.command.impl;

import java.util.List;

import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.command.annotation.QueryResourceCommand;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.an.fxtree.impl.util.FxNodeCheckUtils;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

public class NameJsonValueParserTest {

    protected static class FooCommands {
        
        @QueryResourceCommand(name="cmd1", resourceType="Type1")
        public static void cmd1_NoParam(CommandExecutionCtx ctx, Resource target) {
        }
        @QueryResourceCommand(name="cmd2", resourceType="Type1")
        public static void cmd2_bool_int_long_float_double_text_tree(CommandExecutionCtx ctx, Resource target, 
                @Param(name="boolParam") boolean boolParam, 
                @Param(name="intParam") int intParam, 
                @Param(name="longParam") long longParam, 
                @Param(name="floatParam") float floatParam, 
                @Param(name="doubleParam") double doubleParam, 
                @Param(name="textParam") String textParam,
                @Param(name="treeParam") FxNode treeParam
                ) {
        }
        @QueryResourceCommand(name="cmd3", resourceType="Type1")
        public static void cmd3_bool_int_long_float_double_text_tree(CommandExecutionCtx ctx, Resource target, 
                @Param(name="boolParam", defaultValue="true") boolean boolParam, 
                @Param(name="intParam", defaultValue="12") int intParam, 
                @Param(name="longParam", defaultValue="23") long longParam, 
                @Param(name="floatParam", defaultValue="1.2") float floatParam, 
                @Param(name="doubleParam", defaultValue="2.3") double doubleParam, 
                @Param(name="textParam", defaultValue="\"text\"") String textParam,
                @Param(name="treeParam", defaultValue="{ \"id\": 34 }") String treeParam) {
        }
    }
    
    protected static ResourceTypeRepository resourceTypeRepository = new ResourceTypeRepository();
    protected static ResourceCommand cmd1;
    protected static ResourceCommand cmd2;
    protected static ResourceCommand cmd3;

    protected static ResourceCommandInfo cmd1Info;
    protected static ResourceCommandInfo cmd2Info;
    protected static ResourceCommandInfo cmd3Info;

    @BeforeClass
    public static void setup() {
        AnnotatedMethodToCommandInfoHelper methodToCommandHelper = new AnnotatedMethodToCommandInfoHelper(resourceTypeRepository);
        List<ResourceCommand> resourceCommands = methodToCommandHelper.scanStaticMethods(FooCommands.class);
        for(ResourceCommand rc : resourceCommands) {
            switch(rc.getCommandName()) {
            case "cmd1": cmd1 = rc; cmd1Info = cmd1.getCommandInfo(); break;
            case "cmd2": cmd2 = rc; cmd2Info = cmd2.getCommandInfo(); break;
            case "cmd3": cmd3 = rc; cmd3Info = cmd3.getCommandInfo(); break;
            default: throw new IllegalArgumentException();
            }
        }
    }
    
    protected NameJsonValueParser sut = new NameJsonValueParser();
    
    @Test
    public void testParseCommandParamRawNodes_empty() {
        // Prepare
        // Perform
        FxNode[] res = sut.parseCommandParamRawNodes(cmd1Info, "");
        // Post-check
        FxNodeCheckUtils.checkEquals(0, res.length);
    }

    @Test
    public void testParseCommandParamRawNodes_cmd2() {
        // Prepare
        String text = "boolParam=true intParam=123 longParam=234 floatParam=345.6 doubleParam=456.7 textParam=\"text\" treeParam={\"id\": 1 }";
        // Perform
        FxNode[] res = sut.parseCommandParamRawNodes(cmd2Info, text);
        // Post-check
        Assert.assertEquals(7, res.length);
        FxNodeCheckUtils.checkBoolEquals(true, res[0]);
        FxNodeCheckUtils.checkIntEquals(123, res[1]);
        FxNodeCheckUtils.checkIntEquals(234, res[2]); // use Long when value > MAX_INTEGER !
        FxNodeCheckUtils.checkDoubleEquals(345.6, res[3], 1e-6); // use double!
        FxNodeCheckUtils.checkDoubleEquals(456.7, res[4], 1e-9);
        FxNodeCheckUtils.checkTextEquals("text", res[5]);
        FxNodeCheckUtils.checkIntEquals(1, ((FxObjNode)res[6]).get("id"));

        // redo with different order!
        String text2 = "floatParam=345.6 doubleParam=456.7 boolParam=true intParam=123  textParam=\"text\" treeParam={\"id\": 1 } longParam=234";
        // Perform
        res = sut.parseCommandParamRawNodes(cmd2Info, text2);
        // Post-check
        Assert.assertEquals(7, res.length);
        FxNodeCheckUtils.checkBoolEquals(true, res[0]);
        FxNodeCheckUtils.checkIntEquals(123, res[1]);
        FxNodeCheckUtils.checkIntEquals(234, res[2]);
        FxNodeCheckUtils.checkDoubleEquals(345.6, res[3], 1e-6f);
        FxNodeCheckUtils.checkDoubleEquals(456.7, res[4], 1e-9);
        FxNodeCheckUtils.checkTextEquals("text", res[5]);
        FxNodeCheckUtils.checkIntEquals(1, ((FxObjNode)res[6]).get("id"));
    }
 
    @Test
    public void testParseCommandParamRawNodes_cmd2_missing_param() {
        // Prepare
        String text = "boolParam=true";
        // Perform
        try {
            sut.parseCommandParamRawNodes(cmd2Info, text);
            Assert.fail();
        } catch(IllegalArgumentException ex) {
            // Post-check
            Assert.assertEquals("param 'intParam' is required", ex.getMessage());
        }
    }
    
    @Test
    public void testParseCommandParamRawNodes_cmd3_default_empty() {
        // Prepare
        String text = ""; // use all defaults..
        // Perform
        FxNode[] res = sut.parseCommandParamRawNodes(cmd3Info, text);
        // Post-check
        Assert.assertEquals(7, res.length);
        FxNodeCheckUtils.checkBoolEquals(true, res[0]);
        FxNodeCheckUtils.checkIntEquals(12, res[1]);
        FxNodeCheckUtils.checkIntEquals(23, res[2]); // use Long when value > MAX_INTEGER !
        FxNodeCheckUtils.checkDoubleEquals(1.2, res[3], 1e-6); // use double!
        FxNodeCheckUtils.checkDoubleEquals(2.3, res[4], 1e-9);
        FxNodeCheckUtils.checkTextEquals("text", res[5]);
        FxNodeCheckUtils.checkIntEquals(34, ((FxObjNode)res[6]).get("id"));
    }
    
    @Test
    public void testParseCommandParamRawNodes_cmd3_default_override() {
        // Prepare
        String text = "intParam=1111"; // use all defaults..
        // Perform
        FxNode[] res = sut.parseCommandParamRawNodes(cmd3Info, text);
        // Post-check
        Assert.assertEquals(7, res.length);
        FxNodeCheckUtils.checkBoolEquals(true, res[0]);
        FxNodeCheckUtils.checkIntEquals(1111, res[1]);
        FxNodeCheckUtils.checkIntEquals(23, res[2]);
        FxNodeCheckUtils.checkDoubleEquals(1.2, res[3], 1e-6); // use double!
        FxNodeCheckUtils.checkDoubleEquals(2.3, res[4], 1e-9);
        FxNodeCheckUtils.checkTextEquals("text", res[5]);
        FxNodeCheckUtils.checkIntEquals(34, ((FxObjNode)res[6]).get("id"));
        
        // Prepare
        text = "longParam=234 intParam=123  floatParam=345.6 doubleParam=456.7 textParam=\"text\" treeParam={\"id\": 1 } boolParam=true ";
        // Perform
        res = sut.parseCommandParamRawNodes(cmd3Info, text);
        // Post-check
        Assert.assertEquals(7, res.length);
        FxNodeCheckUtils.checkBoolEquals(true, res[0]);
        FxNodeCheckUtils.checkIntEquals(123, res[1]);
        FxNodeCheckUtils.checkIntEquals(234, res[2]); // use Long when value > MAX_INTEGER !
        FxNodeCheckUtils.checkDoubleEquals(345.6, res[3], 1e-6); // use double!
        FxNodeCheckUtils.checkDoubleEquals(456.7, res[4], 1e-9);
        FxNodeCheckUtils.checkTextEquals("text", res[5]);
        FxNodeCheckUtils.checkIntEquals(1, ((FxObjNode)res[6]).get("id"));
    }
    
}
