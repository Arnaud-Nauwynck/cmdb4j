package org.cmdb4j.core.command.commandinfo;

import java.io.Serializable;

import org.cmdb4j.core.model.reflect.ResourceTypeId;
import org.cmdb4j.core.util.CmdbAssertUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * =Pair<resourceTypeId,commandName>
 */
public class ResourceCommandId implements Serializable {

    /** internal for java.io.Serializable */
    private static final long serialVersionUID = 1L;
    
    private final ResourceTypeId resourceTypeId;

    private final String commandName;

    // ------------------------------------------------------------------------
    
    public ResourceCommandId(ResourceTypeId resourceTypeId, String commandName) {
        CmdbAssertUtils.checkNotNull(resourceTypeId, commandName);
        this.resourceTypeId = resourceTypeId;
        this.commandName = commandName;
    }

    @JsonCreator
    public static ResourceCommandId of(@JsonProperty("resourceTypeId") ResourceTypeId resourceTypeId, @JsonProperty("commandName") String commandName) {
        return new ResourceCommandId(resourceTypeId, commandName);
    }

    // ------------------------------------------------------------------------

    public ResourceTypeId getResourceTypeId() {
        return resourceTypeId;
    }
    
    public String getCommandName() {
        return commandName;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commandName == null) ? 0 : commandName.hashCode());
        result = prime * result + ((resourceTypeId == null) ? 0 : resourceTypeId.hashCode());
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
        ResourceCommandId other = (ResourceCommandId) obj;
        if (commandName == null) {
            if (other.commandName != null)
                return false;
        } else if (!commandName.equals(other.commandName))
            return false;
        if (resourceTypeId == null) {
            if (other.resourceTypeId != null)
                return false;
        } else if (!resourceTypeId.equals(other.resourceTypeId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return resourceTypeId + "." + commandName;
    }
    
}
