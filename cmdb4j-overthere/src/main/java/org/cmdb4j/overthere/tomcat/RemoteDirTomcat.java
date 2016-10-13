package org.cmdb4j.overthere.tomcat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.OverthereUtils;

/**
 * discovery/admin helper for a remote accessible Tomcat directory
 *
 */
public class RemoteDirTomcat {

	protected final OverthereConnection connection;
	protected final String remoteDir;
	
	// ------------------------------------------------------------------------
	
	public RemoteDirTomcat(OverthereConnection connection, String remoteDir) {
		this.connection = connection;
		this.remoteDir = remoteDir;
	}
	
	// ------------------------------------------------------------------------

	public String fetchConfServerXmlFile() {
		OverthereFile serverXmlFile = connection.getFile(remoteDir + "/conf/server.xml");
		byte[] content = OverthereUtils.read(serverXmlFile);
		return new String(content);
	}
	
	@XmlRootElement(name="Server")
	public static class TomcatServerSummary {
		@XmlAttribute(name="port") public int port; // default: port="8005" shutdown="SHUTDOWN"
		@XmlElement(name="Service") public TomcatServiceSummary service; // "catalina"
		
		public int findHttpPort() {
			TomcatConnectorSummary connector = service.findConnector(x->x.equals("HTTP/1.1"), null);
			return connector != null? connector.port : 0;
		}
		public int findHttpsPort() {
			TomcatConnectorSummary connector = findHttpsConnector();
			return connector != null? connector.port : 0;
		}
		public TomcatConnectorSummary findHttpsConnector() {
			return service.findConnector(x->x.equals("org.apache.coyote.http11.Http11NioProtocol"), "https");
		}
		public int findAJPPort() {
			TomcatConnectorSummary connector = service.findConnector(x->x.startsWith("AJP/"), null);
			return connector != null? connector.port : 0;
		}
	}
	
	public static class TomcatServiceSummary {
		@XmlElement(name="Connector") public List<TomcatConnectorSummary> connectors = new ArrayList<>();
		
		public TomcatConnectorSummary findConnector(Predicate<String> protocolPred, String scheme) {
			return connectors.stream().filter(x -> (protocolPred ==null || protocolPred.test(x.protocol))
					&& (scheme == null || scheme.equals(x.scheme))
					).findFirst().orElse(null);
		}
	}
	
	public static class TomcatConnectorSummary {
		@XmlAttribute(name="port") public int port; //default: http=8080, https=8443, AJP=8009
		@XmlAttribute(name="protocol") public String protocol;
		@XmlAttribute(name="scheme") public String scheme;
		@XmlAttribute(name="redirectPort") public int redirectPort;
		@XmlAttribute(name="SSLEnabled") public boolean SSLEnabled;
		@XmlAttribute(name="secure") public boolean secure;
		@XmlAttribute(name="keyAlias") public String keyAlias;
		@XmlAttribute(name="keystoreFile") public String keystoreFile;
		@XmlAttribute(name="keystorePassword") public String keystorePassword;
		@XmlAttribute(name="sslProtocol") public String sslProtocol;
		@XmlAttribute(name="sslEnabledProtocols") public String sslEnabledProtocols;
	}
	
	/**
	 * see https://tomcat.apache.org/tomcat-8.0-doc/ssl-howto.html#Edit_the_Tomcat_Configuration_File
	 */
	public TomcatServerSummary parseSummaryConfServerXml_Tomcat8(String content) {
		try {
			JAXBContext context = JAXBContext.newInstance(TomcatServerSummary.class);
		    Unmarshaller unmarshaller = context.createUnmarshaller();
		    TomcatServerSummary res = (TomcatServerSummary) unmarshaller.unmarshal(new StreamSource(new StringReader(content)));
		    return res;
		} catch(Exception ex) {
			throw new RuntimeException("Failed to parse server.xml content", ex);
		}
	}
	
}
