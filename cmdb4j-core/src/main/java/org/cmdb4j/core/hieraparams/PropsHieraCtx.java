package org.cmdb4j.core.hieraparams;

import org.cmdb4j.core.util.props.Props;

public class PropsHieraCtx {

	private final PropsHiera hiera;
	private final PropsHieraId id;

	private Props props;

	// ------------------------------------------------------------------------

	public PropsHieraCtx(PropsHiera hiera, PropsHieraId id, PropsHieraCtx parentCtx, Props overrideProps) {
		this.hiera = hiera;
		this.id = id;
		this.props = parentCtx != null ? Props.newWithParent(parentCtx.getProps(), overrideProps) : overrideProps;
	}

	// ------------------------------------------------------------------------

	public PropsHiera getHiera() {
		return hiera;
	}

	public PropsHieraId getId() {
		return id;
	}

	public Props getProps() {
		return props;
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "PropsHieraCtx";
	}

}
