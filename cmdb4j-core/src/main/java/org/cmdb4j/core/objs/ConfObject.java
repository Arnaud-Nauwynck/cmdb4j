package org.cmdb4j.core.objs;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cmdb4j.core.hieraparams.HieraPath;

/**
 * CMDB Configuration Object 
 * ... as its name implies, there may be CMDB Object contributed at runtime from scheduler/agents/infra/applications/application-servers
 * 
 * ConfObject are persistent entities on the CMDB file store
 *
 */
public class ConfObject {

    private final HieraPath pathId;
    
    private final ConfClass confClss;
    
    private Map<ConfPropDef<?>,ConfProp<?>> properties = new HashMap<ConfPropDef<?>,ConfProp<?>>();

    private Set<String> tags = new HashSet<String>();
    
    // ------------------------------------------------------------------------

    public ConfObject(ConfClass confClss, HieraPath pathId, Map<ConfPropDef<?>,ConfProp<?>> props) {
        this.confClss = confClss;
        this.pathId = pathId;
        this.properties.putAll(props);
    }

    // ------------------------------------------------------------------------

    public ConfClass getConfClss() {
        return confClss;
    }
    
    public HieraPath getPathId() {
        return pathId;
    }

    public Map<ConfPropDef<?>, ConfProp<?>> getProperties() {
        return properties;
    }

    @SuppressWarnings("unchecked")
    public <T> ConfProp<T> getProperty(ConfPropDef<T> propDef) {
        return (ConfProp<T>) properties.get(propDef);
    }

    public <T> ConfProp<T> getProperty(String prop) {
        ConfPropDef<T> propDef = confClss.getPropertyDefOrEx(prop);
        return getProperty(propDef);
    }

//    public <T> void putProperty(ConfPropDef<?> propDef, ConfProp<T> prop) {
//        ConfProp<?> prev = properties.get(propDef);
//        if (prev != null) throw new IllegalStateException();
//        properties.put(propDef, prop);
//    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public boolean hasAllTag(Collection<String> elts) {
        boolean res = true;
        for(String elt : elts) {
            if (! tags.contains(elt)) {
                res = false;
                break;
            }
        }
        return res;
    }

    public boolean hasAnyTag(Collection<String> elts) {
        boolean res = false;
        for(String elt : elts) {
            if (tags.contains(elt)) {
                res = true;
                break;
            }
        }
        return res;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return pathId + " (" + confClss + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pathId == null) ? 0 : pathId.hashCode());
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
        ConfObject other = (ConfObject) obj;
        if (pathId == null) {
            if (other.pathId != null)
                return false;
        } else if (!pathId.equals(other.pathId))
            return false;
        return true;
    }

    
}
