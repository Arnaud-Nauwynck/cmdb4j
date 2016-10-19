package org.cmdb4j.overthere.docker;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

public class DockerContainerOverthereConnection extends BaseOverthereConnection {
	
	protected OverthereConnection dockerHostConnection;
	protected String dockerContainerId;
	
	// ------------------------------------------------------------------------

	public DockerContainerOverthereConnection(OverthereConnection dockerHostConnection, String dockerContainerId) {
		super("docker", buildConnectionOptions(dockerHostConnection, dockerContainerId), buildAddressPortMapper(), true);
		this.dockerHostConnection = dockerHostConnection;
		this.dockerContainerId = dockerContainerId;
	}

	protected static ConnectionOptions buildConnectionOptions(OverthereConnection dockerHostConnection, String dockerContainerId) {
		ConnectionOptions res = new ConnectionOptions();
        res.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX); // TODO soon Docker on Windows ..
	    // TODO
		return res;
	}
	
	protected static AddressPortMapper buildAddressPortMapper() {
		AddressPortMapper res = new DefaultAddressPortMapper();
		// NOT IMPlEMENTED / NOT USED YET
		return res;
	}
	
	@Override
	public void doClose() {
		if (dockerHostConnection != null) {
			try {
				dockerHostConnection.close();
			} catch(Exception ex) {
				// best effort, ignore
			}
			dockerHostConnection = null;
		}
	}

	// ------------------------------------------------------------------------

	@Override
    public OverthereProcess startProcess(CmdLine commandLine) {
		CmdLine hostCmdLine = new CmdLine();
		hostCmdLine.addArgument("docker"); // "/usr/bin/docker"
		hostCmdLine.addArgument("exec");
		hostCmdLine.addArgument(dockerContainerId);
		hostCmdLine.add(commandLine.getArguments());
		return dockerHostConnection.startProcess(hostCmdLine);
    }
	
    protected String fileSep() {
    	return getHostOperatingSystem().getFileSeparator();
    }

	public OverthereFile getFile(String child) {
		return new DockerContainerOverthereFile(this, child);
	}

	public OverthereFile getFile(OverthereFile parent, String child) {
		DockerContainerOverthereFile file2 = (DockerContainerOverthereFile) parent;
		String path = constructPath(file2, child); 
		return new DockerContainerOverthereFile(this, path);
	}

	@Override
	protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        checkParentFile(parent);
        return getFile(parent, name);
    }

    protected void checkParentFile(final OverthereFile parent) {
        if (!(parent instanceof DockerContainerOverthereFile)) {
            throw new IllegalStateException("parent is not a file on an DockerContainerOverthereConnection");
        }
        if (parent.getConnection() != this) {
            throw new IllegalStateException("parent is not a file in this connection");
        }
    }

    protected static String constructPath(DockerContainerOverthereFile parent, final String child) {
        return parent.getPath() + parent.getConnection().fileSep() + child;
    }

    // ------------------------------------------------------------------------
	
	public String toString() {
		return "DockerContainer[" + dockerContainerId + " in " + dockerHostConnection + "]"; 
	}

	public String toStringInfo() {
		return dockerContainerId + " in " + dockerHostConnection; 
	}
	
}
