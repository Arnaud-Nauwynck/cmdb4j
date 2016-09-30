package org.cmdb4j.core.ext.tokentemplate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TemplatizedTokenKeyPath implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;
	
	private final TemplatizedTokenKey[] pathElements;

	// ----------------------------------------------------------------------------------------------------------------

	public TemplatizedTokenKeyPath(List<TemplatizedTokenKey> pathElements) {
		this.pathElements = pathElements.toArray(new TemplatizedTokenKey[pathElements.size()]);
	}

	// ----------------------------------------------------------------------------------------------------------------

	public int size() {
		return pathElements.length;
	}
	
	public TemplatizedTokenKey getPathElements(int nth) {
		return pathElements[nth];
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pathElements);
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
		TemplatizedTokenKeyPath other = (TemplatizedTokenKeyPath) obj;
		if (!Arrays.equals(pathElements, other.pathElements))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pathElements.length; i++) {
			sb.append(pathElements[i].dumpAsString());
			if (i + 1 < pathElements.length) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
}
