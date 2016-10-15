package org.cmdb4j.overthere;

import java.util.Map;
import java.util.function.Function;

import org.cmdb4j.overthere.BashParseUtils.ShellVars;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class BashParseUtilsTest {

	@Test
	public void testEvalVars() {
		Map<String,String> vars = ImmutableMap.of("VAR1", "VALUE1", "VAR2", "VALUE2");
		Assert.assertEquals("VALUE1", BashParseUtils.evalVars("${VAR1}", vars));
		Assert.assertEquals("XXVALUE1YY", BashParseUtils.evalVars("XX${VAR1}YY", vars));
		Assert.assertEquals("XXVALUE1", BashParseUtils.evalVars("XX${VAR1}", vars));
		Assert.assertEquals("VALUE1YY", BashParseUtils.evalVars("${VAR1}YY", vars));
		Assert.assertEquals("XXVALUE1YYVALUE2ZZ", BashParseUtils.evalVars("XX${VAR1}YY${VAR2}ZZ", vars));
		Assert.assertEquals("VALUE1VALUE2", BashParseUtils.evalVars("${VAR1}${VAR2}", vars));
		Assert.assertEquals("VALUE1 VALUE2", BashParseUtils.evalVars("${VAR1} ${VAR2}", vars));
		Assert.assertEquals("XX VALUE1 VALUE2 YY", BashParseUtils.evalVars("XX ${VAR1} ${VAR2} YY", vars));
	}

	@Test
	public void testEvalVars_noCurlyBrace() {
		Map<String,String> vars = ImmutableMap.of("VAR1", "VALUE1", "VAR2", "VALUE2");
		Assert.assertEquals("VALUE1", BashParseUtils.evalVars("$VAR1", vars));
		Assert.assertEquals("XXVALUE1YY", BashParseUtils.evalVars("XX$VAR1YY", vars));
		Assert.assertEquals("XXVALUE1", BashParseUtils.evalVars("XX$VAR1", vars));
		Assert.assertEquals("VALUE1YY", BashParseUtils.evalVars("$VAR1YY", vars));
		Assert.assertEquals("VALUE1 VALUE2", BashParseUtils.evalVars("$VAR1 $VAR2", vars));
		Assert.assertEquals("XX VALUE1 VALUE2 YY", BashParseUtils.evalVars("XX $VAR1 $VAR2 YY", vars));
		Assert.assertEquals("XXVALUE1YYVALUE2ZZ", BashParseUtils.evalVars("XX$VAR1YY$VAR2ZZ", vars));
	}
	
	@Test
	public void testEvalVars_leaveVarNotFound() {
		Map<String,String> vars = ImmutableMap.of("A", "1");
		Assert.assertEquals("$VAR", BashParseUtils.evalVars("$VAR", vars));
		Assert.assertEquals("XX$VARYY", BashParseUtils.evalVars("XX$VARYY", vars));
	}
	
	@Test
	public void testEvalVars_replaceLongestVar() {
		Map<String,String> vars = ImmutableMap.of("A", "1", "AB", "2");
		Assert.assertEquals("2", BashParseUtils.evalVars("$AB", vars));
	}
	
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_comment() {
		String script1 = "# commentVAR=VALUE\n";
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertTrue(res.envVars.isEmpty());
		Assert.assertTrue(res.localScriptVars.isEmpty());
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_exportVarAssign() {
		String script1 = "export VAR1=VALUE1\n";
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertNull(res.envVars.get("LOCALVAR1"));
		Assert.assertTrue(res.localScriptVars.isEmpty());
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_local() {
		String script1 = "LOCALVAR1=LOCALVALUE1\n";
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertNull(res.envVars.get("LOCALVAR1"));
		Assert.assertEquals("LOCALVALUE1", res.localScriptVars.get("LOCALVAR1"));
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_local_multiline() {
		String script1 = "LOCALVAR1=LOCALVALUE1\\\n"
				+ "   :LOCALVALUE2";
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertNull(res.envVars.get("LOCALVAR1"));
		Assert.assertEquals("LOCALVALUE1   :LOCALVALUE2", res.localScriptVars.get("LOCALVAR1")); //TODO should be trimed??
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_varExport() {
		String script1 = "VAR2=VALUE2; export VAR2\n";
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertEquals("VALUE2", res.envVars.get("VAR2"));
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_localVar_export() {
		String script1 = "VAR3=VALUE3\n"
				+ "export VAR3\n"
				;
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertEquals("VALUE3", res.envVars.get("VAR3"));
	}

	@Test
	public void testHeuristicDetectShellVarsFromScript_eval() {
		String script1 = "A=\"1\"\n"
				+ "B=\"${A}\"\n"
				;
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertEquals("1", res.localScriptVars.get("B"));
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_concatEval() {
		String script1 = "CONCATVAR4=\"VALUE4a\"\n"
				+ "CONCATVAR4=\"$CONCATVAR4:VALUE4b\"\n"
				+ "CONCATVAR4=\"${CONCATVAR4}:VALUE4c\"\n"
				;
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		Assert.assertEquals("VALUE4a:VALUE4b:VALUE4c", res.localScriptVars.get("CONCATVAR4"));
	}
	
	@Test
	public void testHeuristicDetectShellVarsFromScript_source() {
		String script1 = "source another-sentenv1.sh\n"
				+ ". src/test/data/another-sentenv2.sh\n"
				;
		Function<String,String> scriptLoader = x -> {
			if (x.equals("another-sentenv1.sh")) {
				return "export ANOTHER_SETENV1_VAR1=VALUE1\n";
			} else if (x.equals("src/test/data/another-sentenv2.sh")) {
				return "export ANOTHER_SETENV2_VAR1=VALUE1\n";
			} else {
				return null;
			}
		};
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1, "", scriptLoader);
		Assert.assertEquals("VALUE1", res.envVars.get("ANOTHER_SETENV1_VAR1"));
		Assert.assertEquals("VALUE1", res.envVars.get("ANOTHER_SETENV2_VAR1"));
	}
	

	@Test
	public void testHeuristicDetectShellVarsFromScript_multiple() {
		String script1 = "LOCALVAR1=LOCALVALUE1\n"
				+ "export VAR1=VALUE1\n"
				+ "VAR2=VALUE2; export VAR2\n"
				+ "VAR3=VALUE3\n"
				+ "export VAR3\n"
				+ "CONCATVAR4=\"VALUE4a\"\n"
				+ "CONCATVAR4=\"$CONCATVAR4:VALUE4b\"\n"
				+ "CONCATVAR4=\"${CONCATVAR4}:VALUE4c\"\n"
				+ "export CONCATVAR4\n"
				;
		ShellVars res = new ShellVars();
		BashParseUtils.heuristicDetectShellVarsFromScript(res, script1);
		
		Assert.assertNull(res.envVars.get("VAR"));
		Assert.assertNull(res.localScriptVars.get("VAR"));

		Assert.assertNull(res.envVars.get("LOCALVAR1"));
		Assert.assertEquals("LOCALVALUE1", res.localScriptVars.get("LOCALVAR1"));

		Assert.assertEquals("VALUE1", res.envVars.get("VAR1"));
		Assert.assertEquals("VALUE2", res.envVars.get("VAR2"));
		Assert.assertEquals("VALUE3", res.envVars.get("VAR3"));
		Assert.assertEquals("VALUE4a:VALUE4b:VALUE4c", res.envVars.get("CONCATVAR4"));
	}
	
	
}