package org.cmdb4j.overthere.docker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.cmdb4j.overthere.utils.OverthereProcessUtils;
import org.cmdb4j.overthere.utils.ProcessOutErrResult;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.spi.BaseOverthereFile;

public class DockerContainerOverthereFile extends BaseOverthereFile<DockerContainerOverthereConnection> {

	// cf super.. protected DockerContainerOverthereConnection connection;
	
	private String path;
	
	// ------------------------------------------------------------------------
	
	protected DockerContainerOverthereFile() {
        super();
    }

    protected DockerContainerOverthereFile(DockerContainerOverthereConnection connection, String path) {
        super(connection);
        this.path = path;
    }

	// ------------------------------------------------------------------------
    
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public OverthereFile getParentFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		ProcessOutErrResult tmpres = OverthereProcessUtils.execGetOutErr(connection, "docker", "execute", connection.dockerContainerId, "/bin/ls", "-l", "'" + path + "'");
		if (tmpres.getExitCode() != 0) {
			throw new RuntimeException("Failed to getFile within docker container, using command \"docker execute " + connection.dockerContainerId + " /bin/ls -l '" + path + "' \"");
		}
		return tmpres.getStdout().length > 0; //TODO
	}

	@Override
	public boolean canRead() {
//		ProcessOutErrResult tmpres = OverthereProcessUtils.execGetOutErr(connection, "docker", "execute", connection.dockerContainerId, "/bin/ls -l", path);
//		if (tmpres.getExitCode() != 0) {
//			throw new RuntimeException("Failed to getFile within docker container, using command \"docker execute " + connection.dockerContainerId + " /bin/cat " + path + "\"");
//		}
		return true; // TODO read from "-(r)wx------" 
	}

	@Override
	public boolean canWrite() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canExecute() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long lastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long length() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public InputStream getInputStream() {
		// may use only a OverthereProcess "/bin/cat" ... not fetching data here
		ProcessOutErrResult tmpres = OverthereProcessUtils.execGetOutErr(connection, "docker", "execute", connection.dockerContainerId, "/bin/cat", path);
		if (tmpres.getExitCode() != 0) {
			throw new RuntimeException("Failed to getFile within docker container, using command \"docker execute " + connection.dockerContainerId + " /bin/cat " + path + "\"");
		}
		return new ByteArrayInputStream(tmpres.getStdout());
	}

	@Override
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExecutable(boolean executable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<OverthereFile> listFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void mkdir() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mkdirs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renameTo(OverthereFile dest) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "DockerContainerOverthereFile[" + path + " on " + ((connection != null)? connection.toStringInfo() : "null") + "]";
	}

}
