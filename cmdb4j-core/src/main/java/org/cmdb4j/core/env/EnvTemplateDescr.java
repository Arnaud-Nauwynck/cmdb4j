package org.cmdb4j.core.env;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.an.fxtree.model.FxNode;

/**
 * Description for a template env
 * 
 * <p> 
 * see also corresponding EnvTemplateDescrDTO
 */
public class EnvTemplateDescr {

	private String name;

	private String displayName;

	private String comment;

	private List<EnvTemplateParamDescr> paramDescriptions = new ArrayList<>();

	private Map<String, FxNode> extraProperties = new LinkedHashMap<>();

	private FxNode rawNode;

	// ------------------------------------------------------------------------

	public EnvTemplateDescr(String name, String displayName, String comment,
			List<EnvTemplateParamDescr> paramDescriptions, Map<String, FxNode> extraProperties, FxNode rawNode) {
		this.name = name;
		this.displayName = displayName;
		this.comment = comment;
		this.paramDescriptions = paramDescriptions;
		this.extraProperties = extraProperties;
		this.rawNode = rawNode;
	}

	// ------------------------------------------------------------------------

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getComment() {
		return comment;
	}

	public List<EnvTemplateParamDescr> getParamDescriptions() {
		return paramDescriptions;
	}

	public EnvTemplateParamDescr findParamDescriptionByName(String name) {
		for (EnvTemplateParamDescr e : paramDescriptions) {
			if (name.equals(e.getName())) {
				return e;
			}
		}
		return null;
	}

	public Map<String, FxNode> getExtraProperties() {
		return extraProperties;
	}

	public FxNode getRawNode() {
		return rawNode;
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "EnvTemplateDescr [" + name + "]";
	}

}
