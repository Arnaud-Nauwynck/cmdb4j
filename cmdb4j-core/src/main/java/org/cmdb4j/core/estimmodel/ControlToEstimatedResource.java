package org.cmdb4j.core.estimmodel;

import org.cmdb4j.core.model.Resource;

/**
 * Mapping for Estimated Resource State in estimated repository, and Resource in control repository
 * <PRE>
 * 
 *  +------------------------------------+
 *  | ControlState ResourceRepository    |
 *  |  = Map<Id,Resource>                |                        +------------------------+
 *  +------------------------------------+                        | ResourceTypeRepository |
 *             /\                                                 +------------------------+
 *             \/                                                    |
 *             |                                                     |
 *              \     +------------+                                  \  1 +-----------------+
 *                --> | Resource   |    <------------                   -> | AdapterManager  |  ......>
 *                    +------------+                 \                     +-----------------+          \
 *   example controlled resource:                     \                       |   (type->itfId->alternative->AdapterFactory)
 *     { id: "DEV1/host1/tomcat1", type="Tomcat",     |                       |                           \
 *       host: "DEV1/host1",                          |                        \ *    +----------------+   \
 *       tomcatBase: '/home/user/tomcat1',            |                          ->   | AdapterFactory |    .
 *       httpPort: 8080,                              |                               +----------------+    .
 *       state: "running",                            |                                                     .
 *                                                    |                                                     .
 *       "@control-constaints": {                     |                                                     .
 *       	"mem-constraint1": {                      |                                                     .
 *       		"jmx.mem.free > 10_000_000"           |  +--------------------------------+                 .
 *       	}                                         |  | ResourceStateEstimatorRegistry |                 .
 *       }	                                          |  +--------------------------------+                 .
 *     }                                              |           /\                                        .
 *                                                    |           \/                                        .
 *              /\                                     \          |                                 .. getAdapter(resource,
 *              ||          Constraints                 \     +----------------------------+            Itf(ResourceStateEstimatorProvider))
 *       .... partially compare...                        --- | ControlToEstimatedResource |                .
 *              ||          Violations                    --- +----------------------------+                .
 *              \/                                      /              /\          /\                       .
 *                                                     /               \/          \/                       .
 *  +------------------------------------+            |                |            |                       \/
 *  | EstimatedState ResourceRepository  |            |    +----------------------+   new +-------------------------------+
 *  |  = Map<Id,Resource>                |            |    |ResourceStateEstimator| <...  |ResourceStateEstimatorProvider |
 *  +------------------------------------+            |    +----------------------+       +-------------------------------+
 *             /\                                     |          |                  |
 *             \/                                     |          |                  \/
 *             |                                     /           |   +-------------------------+ 
 *              \     +------------+                /            |   | ResourceFieldEstimation |
 *                --> | Resource   |     <---------              |   +-------------------------+
 *                    +------------+                             |        |
 *   example estimated resource state:                           \/      \/
 *     { id: "DEV1/host1/tomcat1", type="Tomcat",               +--------------------------+
 *       host: "DEV1/host1",                                    + ResourceFieldEstimator   |
 *       port: "8080",                                          +--------------------------+
 *       tomcatBase: '/home/user/tomcat1',
 *       state: "running",
 *       
 *       pid: 12345,
 *       uptime: '2016-01-01T22:55:10',
 *       jmx: {
 *       	mem: {
 *       		used: 500000, max: 10000000
 *       	}
 *       },
 *       
 *       "@field-estimations": {
 *          state: {
 *       		state: 'ok', lastEstimatedDate: ..,
 *          	sources: {
 *          		"http-ping-2-status-infer": { .. },
 *          		"pid-2-status-infer": { .. }
 *          	}
 *          },
 *          pid: {
 *          	state: 'ok', lastEstimatedDate: ..,
 *          	sources: {
 *          		"logs-catalina.pid": { .. }
 *          	}
 *          },
 *          uptime: { .. }
 *       	port: { 
 *       		state: 'ok', lastEstimatedDate: ..,
 *       		sources: {
 *       			"discovery-server.xml" : { ... }
 *       		}
 *          }
 *       },
 *       "@estimators-data": {
 *       	...
 *       }
 *     }
 *    
 *    
 *    
 * </PRE>
 */
public class ControlToEstimatedResource {

	protected Resource targetControlResource;
	
	protected Resource targetEstimatedStateResource;
	
	protected ResourceStateEstimatorRegistry estimatorContributionsRegistry;

	// --------------------------------------------------------------------------------------------
	
	public ControlToEstimatedResource(Resource targetControlResource, Resource targetEstimatedStateResource,
			ResourceStateEstimatorRegistry estimatorContributionsRegistry) {
		this.targetControlResource = targetControlResource;
		this.targetEstimatedStateResource = targetEstimatedStateResource;
		this.estimatorContributionsRegistry = estimatorContributionsRegistry;
	}

	// --------------------------------------------------------------------------------------------
	
	public Resource getTargetControlResource() {
		return targetControlResource;
	}

	public void setTargetControlResource(Resource targetControlResource) {
		this.targetControlResource = targetControlResource;
	}

	public ResourceStateEstimatorRegistry getEstimatorContributionsRegistry() {
		return estimatorContributionsRegistry;
	}

	public void setEstimatorContributionsRegistry(ResourceStateEstimatorRegistry estimatorContributionsRegistry) {
		this.estimatorContributionsRegistry = estimatorContributionsRegistry;
	}

	// --------------------------------------------------------------------------------------------

	@Override
	public String toString() {
		return "ControlToEstimatedResource [" 
				+ ((targetControlResource != null)? targetControlResource.getId() : "-")
				+ "]";
	}

	
	
	
}
