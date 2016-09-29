package org.cmdb4j.core.hieraparams;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.cmdb4j.core.util.props.Props;

public class PropsHiera {

	private final Function<PropsHieraId, PropsHieraId> id2ParentIdFunc;
	private final Function<String, Props> paramsLoader;

	private Map<PropsHieraId, PropsHieraCtx> cache = new HashMap<>();

	private Map<String, Props> paramsCache = new HashMap<>();

	// ------------------------------------------------------------------------

	public PropsHiera(Function<PropsHieraId, PropsHieraId> id2ParentIdFunc, Function<String, Props> paramsLoader,
			Map<String, Props> initParamsCache) {
		this.id2ParentIdFunc = id2ParentIdFunc;
		this.paramsLoader = paramsLoader;
		if (initParamsCache != null) {
			this.paramsCache.putAll(initParamsCache);
		}
	}

	// ------------------------------------------------------------------------

	public PropsHieraId parentIdOf(PropsHieraId id) {
		return id2ParentIdFunc.apply(id);
	}

	public PropsHieraCtx getHieraCtx(PropsHieraId id) {
		if (id == null) {
			return null;
		}
		PropsHieraCtx res = cache.get(id);
		if (res == null) {
			PropsHieraId parentId = parentIdOf(id);

			// ** recursive call **
			PropsHieraCtx parentCtx = (parentId != null) ? getHieraCtx(parentId) : null;

			String envConfPath = id.envName + "/" + id.confPath;
			Props override = paramsCache.get(envConfPath);
			if (override == null && !paramsCache.containsKey(envConfPath)) {
				override = paramsLoader.apply(envConfPath);
				paramsCache.put(envConfPath, override);
			}
			if (override == null) {
				override = new Props(); // ??
			}
			res = new PropsHieraCtx(this, id, parentCtx, override);
			cache.put(id, res);
		}
		return res;
	}

	public PropsHieraCtx getParentHieraCtx(PropsHieraCtx ctx) {
		if (ctx == null) {
			return null;
		}
		PropsHieraId parentId = parentIdOf(ctx.getId());
		return (parentId != null) ? getHieraCtx(parentId) : null;
	}

}
