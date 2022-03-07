package org.cmdb4j.core.store;

public interface IEnvValueDecrypter {

	String PREFIX_CMDB = "cmdb-";

	public String decryptValueForEnv(String value, String envName);

}
