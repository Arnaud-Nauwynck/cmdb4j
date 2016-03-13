package org.cmdb4j.core.estimmodel;

import fr.an.dynadapter.alt.ItfId;

public interface IResourceStateEstimatorProvider<T> {

	@SuppressWarnings("rawtypes")
	public static final ItfId<IResourceStateEstimatorProvider> ITF_ID = 
			ItfId.of(IResourceStateEstimatorProvider.class);

	
	public T createResourceEstimator(ControlToEstimatedResource target);

	public void disposeResourceEstimator(ControlToEstimatedResource target, T data);

	public void startResourceEstimator(ControlToEstimatedResource target, T data);

	public void stopResourceEstimator(ControlToEstimatedResource target, T data);

	public void refreshResourceEstimator(ControlToEstimatedResource target, T data);

}
