package org.cmdb4j.overthere.tomcat;

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
		TomcatServerSummary summary = sut.parseSummaryConfServerXml_Tomcat8(serverXmlContent);
		Assert.assertNotNull(summary);
		Assert.assertEquals(8005, summary.port);
		Assert.assertEquals(8080, summary.findHttpPort());
		Assert.assertEquals(8443, summary.findHttpsPort());
		TomcatConnectorSummary httpsConnector = summary.findHttpsConnector();
		Assert.assertEquals("test-keyAlias", httpsConnector.keyAlias);
		Assert.assertEquals(8009, summary.findAJPPort());
	}
	
}
