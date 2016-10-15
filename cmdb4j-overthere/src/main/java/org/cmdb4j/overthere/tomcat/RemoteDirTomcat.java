package org.cmdb4j.overthere.tomcat;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import org.cmdb4j.overthere.BashParseUtils;
import org.cmdb4j.overthere.BashParseUtils.ShellVars;
import org.cmdb4j.overthere.jvm.JvmArgsUtils;
import org.cmdb4j.overthere.jvm.JvmArgsUtils.JvmArgs;
import org.cmdb4j.overthere.shells.sh.ShellArgumentTokenizerUtils;

import com.xebialabs.overthere.OperatingSystemFamily;
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
	
	public String fetchBinStartupFile() {
		String scriptExtension = connection.getHostOperatingSystem().getScriptExtension();
		OverthereFile file = connection.getFile(remoteDir + "/bin/startup" + scriptExtension);
		byte[] content = OverthereUtils.read(file);
		return new String(content);
	}

	public String fetchBinSetenvFile() {
		String scriptExtension = connection.getHostOperatingSystem().getScriptExtension();
		OverthereFile file = connection.getFile(remoteDir + "/bin/setenv" + scriptExtension);
		if (!file.exists()) {
			return null;
		}
		byte[] content = OverthereUtils.read(file);
		return new String(content);
	}
	
	public static class EnvVarsAndJvmArgs {
		public ShellVars shellVars = new ShellVars();
		public List<String> javaArgs = new ArrayList<>();
		public JvmArgs jvmArgs = new JvmArgs(); 
	}
	
	protected Function<String,String> fileLoader() {
		return x -> {
			OverthereFile file = connection.getFile(x);
			if (!file.exists() || !file.canRead()) {
				return null;
			}	
			byte[] content = OverthereUtils.read(file);
			return new String(content);
		};
	}
	
	public EnvVarsAndJvmArgs parseDetectEnvVarsAndJvmArgs(String startupFileContent, String setenvFileContent) {
		EnvVarsAndJvmArgs res = new EnvVarsAndJvmArgs();
		OperatingSystemFamily os = connection.getHostOperatingSystem();
		Function<String,String> fileLoader = fileLoader();
		String fileSep = os.getFileSeparator();
		String binDir = remoteDir + fileSep + "bin";
		switch(os) {
		case UNIX:
			// boolean seemsStdStartup = startupFileContent.endsWith("exec \"$PRGDIR\"/\"$EXECUTABLE\" start \"$@\"");
			// parse env vars
			BashParseUtils.heuristicDetectShellVarsFromScript(res.shellVars, startupFileContent, binDir, fileLoader);
			if (setenvFileContent != null) {
				BashParseUtils.heuristicDetectShellVarsFromScript(res.shellVars, setenvFileContent, binDir, fileLoader);
			}
			break;
		case WINDOWS:
			// really use Windows for tomcat on prod?
			// not supported yet
			break;
		case ZOS:
		default:
			// TODO ..
		}
		
		// parse jvm args (directly in java command or in CATALINA_OPTS, JAVA_OPTS= ...)
		String catalinaOpts = res.shellVars.getLocalOrEnvVar("CATALINA_OPTS");
		if (catalinaOpts != null) {
			List<String> argsList = ShellArgumentTokenizerUtils.tokenize(catalinaOpts);
			res.javaArgs.addAll(argsList);
		}
		String javaOpts = res.shellVars.getLocalOrEnvVar("JAVA_OPTS");
		if (javaOpts != null) {
			List<String> argsList = ShellArgumentTokenizerUtils.tokenize(javaOpts);
			res.javaArgs.addAll(argsList);
		}
		
		// detect std jvm args "-X*", and java args "-D*" 
		JvmArgsUtils.parseJvmArgs(res.jvmArgs, res.javaArgs);
		
		return res;
	}

	public int getPid() {
		int pid = 0;
		OverthereFile catalinaPidFile = connection.getFile(remoteDir + "/logs/catalina.pid");
		if (catalinaPidFile.exists()) {
			byte[] content = OverthereUtils.read(catalinaPidFile);
			try {
				pid = Integer.parseInt(new String(content));
			} catch(NumberFormatException ex) {
				pid = 0;
			}
			if (pid != 0) {
				// detect if stale pid file
				OperatingSystemFamily os = connection.getHostOperatingSystem();
				switch(os) {
				case UNIX:
					OverthereFile procFile = connection.getFile("/proc/" + pid);
					if (! procFile.exists()) {
						pid = 0; // detected stale pid file!  => remove it?
					}
				case WINDOWS:
				case ZOS:
				default:
					// TODO ..
				}
			}
		}
		return pid;
	}

}
