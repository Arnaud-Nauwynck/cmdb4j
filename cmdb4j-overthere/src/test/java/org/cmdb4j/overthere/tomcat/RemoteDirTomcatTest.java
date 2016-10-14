package org.cmdb4j.overthere.tomcat;

import java.util.Map;

import org.cmdb4j.overthere.BashParseUtils;
import org.cmdb4j.overthere.tomcat.RemoteDirTomcat.EnvVarsAndJvmArgs;
import org.cmdb4j.overthere.tomcat.RemoteDirTomcat.TomcatConnectorSummary;
import org.cmdb4j.overthere.tomcat.RemoteDirTomcat.TomcatServerSummary;
import org.junit.Assert;
import org.junit.Test;

import com.xebialabs.overthere.local.LocalConnection;

public class RemoteDirTomcatTest {

	private static final String testTomcat8InstallDir = "src/test/data/tomcat8";
	
	protected RemoteDirTomcat sut = new RemoteDirTomcat(LocalConnection.getLocalConnection(), testTomcat8InstallDir);
	
	@Test
	public void testFetchConfServerXmfFile() {
		String serverXmlContent = sut.fetchConfServerXmlFile();
		Assert.assertNotNull(serverXmlContent);
	}
	
	@Test
	public void testParseSummaryConfServerXml_Tomcat8() {
		String serverXmlContent = sut.fetchConfServerXmlFile();
		TomcatServerSummary summary = sut.parseSummaryConfServerXml_Tomcat8(serverXmlContent);
		Assert.assertNotNull(summary);
		Assert.assertEquals(8005, summary.port);
		Assert.assertEquals(8080, summary.findHttpPort());
		Assert.assertEquals(8443, summary.findHttpsPort());
		TomcatConnectorSummary httpsConnector = summary.findHttpsConnector();
		Assert.assertEquals("test-keyAlias", httpsConnector.keyAlias);
		Assert.assertEquals(8009, summary.findAJPPort());
	}
	
	@Test
	public void fetchBinStartupFile() {
		String res = sut.fetchBinStartupFile();
		Assert.assertNotNull(res);
	}

	@Test
	public void fetchBinSetenvFile() {
		String res = sut.fetchBinSetenvFile();
		Assert.assertNotNull(res);
	}

	@Test
	public void testParseDetectEnvVarsAndJvmArgs() {
		String startupSh = sut.fetchBinStartupFile();
		String setenvSh = sut.fetchBinSetenvFile();
		EnvVarsAndJvmArgs res = sut.parseDetectEnvVarsAndJvmArgs(startupSh, setenvSh);
		Assert.assertNotNull(res);
		Map<String, String> locals = res.shellVars.localScriptVars;
		Assert.assertEquals("catalina.sh", locals.get("EXECUTABLE"));
		Assert.assertEquals("-agent:myagent.jar", locals.get("JVM_AGENT_ARGS"));
		Assert.assertEquals("-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000", locals.get("JVM_DEBUG_ARGS"));
		Assert.assertEquals("-Dcom.sun.management.jmxremote", locals.get("JAVA_JMX_ARGS"));
		String evalJavaArgs = BashParseUtils.evalVars("${JVM_AGENT_ARGS} ${JVM_DEBUG_ARGS} ${JAVA_JMX_ARGS}", res.shellVars);
		Assert.assertEquals(evalJavaArgs, locals.get("JAVA_OPTS"));
		Assert.assertEquals("-agent:myagent.jar -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Dcom.sun.management.jmxremote", evalJavaArgs);
	}
	
	
	
}
