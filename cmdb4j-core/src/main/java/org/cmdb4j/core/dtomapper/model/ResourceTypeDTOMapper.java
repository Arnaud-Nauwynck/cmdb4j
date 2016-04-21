package org.cmdb4j.core.dtomapper.model;

import org.cmdb4j.core.model.reflect.ResourceType;

public class ResourceTypeDTOMapper {

    public static String toName(ResourceType src) {
        return (src != null)? src.getName() : null;
    }
}
