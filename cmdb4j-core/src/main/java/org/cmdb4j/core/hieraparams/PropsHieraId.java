package org.cmdb4j.core.hieraparams;

import lombok.Value;

@Value
public final class PropsHieraId {

	public final String envName, confPath, fullConfPath;


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(envName + "/" + confPath);
		if (confPath != null && !confPath.equals(fullConfPath)) {
			if (fullConfPath.startsWith(confPath)) {
				sb.append("(/" + fullConfPath.substring(confPath.length() + 1) + ")");
			} else {
				sb.append("(?? " + fullConfPath + ")");
			}
		}
		return sb.toString();
	}

}