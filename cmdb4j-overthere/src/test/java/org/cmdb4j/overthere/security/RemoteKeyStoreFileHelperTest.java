package org.cmdb4j.overthere.security;

import java.security.KeyStore;
import java.util.List;

import org.cmdb4j.overthere.security.RemoteKeyStoreFileHelper.CertificateInfo;
import org.junit.Assert;
import org.junit.Test;

import com.xebialabs.overthere.local.LocalConnection;

public class RemoteKeyStoreFileHelperTest {

	protected String testKeystore1File = "src/test/data/security/keystore1.jks";
	protected char[] password = "changeit".toCharArray(); 
	protected RemoteKeyStoreFileHelper sut = new RemoteKeyStoreFileHelper(LocalConnection.getLocalConnection(), testKeystore1File, password);

	@Test
	public void testDumpJks() throws Exception {
		KeyStore ks = sut.fetchKeyStore();
		List<CertificateInfo> certInfos = sut.dumpCertificateInfos(ks);
		Assert.assertEquals(1, certInfos.size());
		CertificateInfo ci0 = certInfos.get(0);
		Assert.assertEquals("Sat Apr 29 23:42:24 CEST 2017", ci0.endDate.toString());
	}
}
