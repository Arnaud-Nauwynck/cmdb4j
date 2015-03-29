package org.cmdb4j.core.objs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.objs.ConfPropDef.Builder;

/**
 * 
 */
public class ConfClass {

    private final String name;

    private Map<String,ConfPropDef<?>> propertyDefs = new HashMap<String,ConfPropDef<?>>();
    
    // TODO .. add eclipse-like AdaptorFactory  (replacement for type hierarchy: superClass and implements)
    
    // TODO defaultObjectTags  (=> to put default tags on objects, per class)
    
    // ------------------------------------------------------------------------
    
    public ConfClass(String name) {
        this.name = name;
    }
    
    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }
    
    public Map<String, ConfPropDef<?>> getPropertyDefs() {
        return propertyDefs;
    }

    @SuppressWarnings("unchecked")
    public Collection<ConfPropDef<Object>> getPropertyDefValues() {
        return (Collection<ConfPropDef<Object>>) (Collection<?>) propertyDefs.values();
    }

    @SuppressWarnings("unchecked")
    public <T> ConfPropDef<T> getPropertyDefOrEx(String prop) {
        ConfPropDef<T> res = (ConfPropDef<T>) propertyDefs.get(prop);
        if (res == null) {
            throw new RuntimeException("property not found: " + prop);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public <T> ConfPropDef<T> getPropertyDefOrNull(String prop) {
        return (ConfPropDef<T>) propertyDefs.get(prop);
    }

    public <T> ConfPropDef<T> registerPropertyDef(String propName, ConfPropDef.Builder<T> propDefBuilder) {
        ConfPropDef<?> prev = propertyDefs.get(propName);
        if (prev != null) throw new IllegalStateException();
        ConfPropDef<T> res = new ConfPropDef<T>(this, propName, propDefBuilder);
        propertyDefs.put(propName, res);
        return res;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ConfClass other = (ConfClass) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    
    
}
