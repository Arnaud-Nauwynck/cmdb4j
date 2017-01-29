package org.cmdb4j.overthere.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.OverthereUtils;

/**
 * Helper class to analyse remote jks file
 *
 */
public class RemoteKeyStoreFileHelper {
	
	private static final Logger LOG = LoggerFactory.getLogger(RemoteKeyStoreFileHelper.class);
	
	private static final String X509 = "X.509";
	
	protected final OverthereConnection connection;
	protected final String remoteKeyStoreFile;
	protected final char[] keyStorePassword;
	
	// ------------------------------------------------------------------------
	
	public RemoteKeyStoreFileHelper(OverthereConnection connection, String remoteKeyStoreFile, char[] keyStorePassword) {
		this.connection = connection;
		this.remoteKeyStoreFile = remoteKeyStoreFile;
		this.keyStorePassword = keyStorePassword;
	}
	
	// ------------------------------------------------------------------------


	public KeyStore fetchKeyStore() {
		OverthereFile otFile = connection.getFile(remoteKeyStoreFile);
		byte[] content = OverthereUtils.read(otFile);
		KeyStore res;
		try {
			res = KeyStore.getInstance("jks");
		} catch (KeyStoreException ex) {
			throw new RuntimeException("Failed getInstance jks", ex);
		}
		try (ByteArrayInputStream bin = new ByteArrayInputStream(content)) {
			res.load(bin, keyStorePassword);
		} catch(IOException ex) {
			throw new RuntimeException("Failed to read jks", ex);
		} catch (CertificateException|NoSuchAlgorithmException ex) {
			throw new RuntimeException("Failed to read jks", ex);
		}
		return res;
	}

	public static class CertificateInfo {
		public String subject;
		public String issuer;
		public Date startDate;
		public Date endDate;
		public PublicKey publicKey;
		
		public CertificateInfo(String subject, String issuer, Date startDate, Date endDate, PublicKey publicKey) {
			this.subject = subject;
			this.issuer = issuer;
			this.startDate = startDate;
			this.endDate = endDate;
			this.publicKey = publicKey;
		}
		
	}
	
	public List<CertificateInfo> dumpCertificateInfos(KeyStore keyStore) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
		List<CertificateInfo> res = new ArrayList<>();
		for(Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
			String alias = aliases.nextElement();
			if (keyStore.isKeyEntry(alias)) {
				// boolean isCertEntry = keyStore.isCertificateEntry(alias);
				// Key key = keyStore.getKey(alias, null);
				Certificate cert = keyStore.getCertificate(alias);
				if (cert != null)	{
					if (X509.equals(cert.getType()))	{
						X509Certificate x509 = (X509Certificate) cert;
						String subject = x509.getSubjectX500Principal().toString();
						String issuer = x509.getIssuerX500Principal().toString();
						Date startDate = x509.getNotBefore();
						Date endDate = x509.getNotAfter();
						PublicKey publicKey = x509.getPublicKey();
						res.add(new CertificateInfo(subject, issuer, startDate, endDate, publicKey));
					} else	{
						LOG.warn("Ignore unhandled certificate type '" + cert.getType() + "'");
					}
				}
			}
		}
		return res;
	}
	
}
