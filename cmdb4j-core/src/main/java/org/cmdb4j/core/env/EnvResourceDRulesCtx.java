package org.cmdb4j.core.env;

import org.cmdb4j.core.env.CmdbEnv.ResourceBuilder;
import org.kie.api.runtime.KieSession;

import lombok.Getter;

/**
 * TODO ARN
 *
 */
public class EnvResourceDRulesCtx {

	@Getter
	private CmdbEnv cmdbEnv;
	
	@Getter
	private KieSession ksession;

	@Getter
	private ResourceBuilder resourceBuilder;
	
	public EnvResourceDRulesCtx(CmdbEnv cmdbEnv, KieSession ksession, ResourceBuilder resourceBuilder) {
		super();
		this.cmdbEnv = cmdbEnv;
		this.ksession = ksession;
		this.resourceBuilder = resourceBuilder;
	}
	
	
}
