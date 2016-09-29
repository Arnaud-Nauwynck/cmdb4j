package org.cmdb4j.core.hieraparams;

public final class PropsHieraId {

	public final String envName, confPath, fullConfPath;

	// ------------------------------------------------------------------------

	public PropsHieraId(String envName, String confPath, String fullConfPath) {
		this.envName = envName;
		this.confPath = confPath;
		this.fullConfPath = fullConfPath;
	}

	// ------------------------------------------------------------------------

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((confPath == null) ? 0 : confPath.hashCode());
		result = prime * result + ((envName == null) ? 0 : envName.hashCode());
		result = prime * result + ((fullConfPath == null) ? 0 : fullConfPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropsHieraId other = (PropsHieraId) obj;
		if (confPath == null) {
			if (other.confPath != null)
				return false;
		} else if (!confPath.equals(other.confPath))
			return false;
		if (envName == null) {
			if (other.envName != null)
				return false;
		} else if (!envName.equals(other.envName))
			return false;
		if (fullConfPath == null) {
			if (other.fullConfPath != null)
				return false;
		} else if (!fullConfPath.equals(other.fullConfPath))
			return false;
		return true;
	}

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