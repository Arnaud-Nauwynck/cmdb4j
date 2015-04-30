package org.cmdb4j.overthere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

public class LocalHostOverthereTest {

    
    private static final Logger LOG = LoggerFactory.getLogger(LocalHostOverthereTest.class);

    @Test
    public void testLocalhostSsh_startProcess() throws Exception {
        // Prepare
        String currentUser = System.getProperty("user.name");
        String userHome = System.getProperty("user.home");
        
        ConnectionOptions options = new ConnectionOptions();
        options.set(ConnectionOptions.ADDRESS, "localhost");
        options.set(ConnectionOptions.USERNAME, currentUser);
        options.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.SFTP);
        options.set(SshConnectionBuilder.PRIVATE_KEY_FILE, userHome + "/.ssh/id_rsa");
        
        options.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        
        // Perform
        OverthereConnection connection = Overthere.getConnection("ssh", options);
        try {
            doStartProcess(connection);
        } finally {
            connection.close();
        }
        
        // Post-check
    }

    @Test
    public void testLocal_startProcess() throws Exception {
        // Prepare
        // Perform
        LocalConnection connection = new LocalConnection(LocalConnection.LOCAL_PROTOCOL, new ConnectionOptions());
        try {
            doStartProcess(connection);
        } finally {
            connection.close();
        }
        
        // Post-check
    }
    
    private void doStartProcess(OverthereConnection connection) throws IOException, InterruptedException {
        OverthereProcess overthereProcess = connection.startProcess(CmdLine.build("cat", "/etc/motd"));
        
        List<String> lines = new ArrayList<String>();
        BufferedReader processOutReader = new BufferedReader(new InputStreamReader(overthereProcess.getStdout()));
        try {
            String line;
            while((line = processOutReader.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            processOutReader.close();
        }
        
        int exitCode = overthereProcess.waitFor();
        LOG.debug("Exit code: " + exitCode);
    }
    
}
