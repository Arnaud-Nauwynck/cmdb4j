package org.cmdb4j.core.objs;

import java.util.HashMap;
import java.util.Map;

public class ConfClassRegistry {

    private Map<String,ConfClass> registeredConfClasses = new HashMap<String,ConfClass>();
    
    // ------------------------------------------------------------------------

    public ConfClassRegistry() {
    }

    // ------------------------------------------------------------------------

    public ConfClass getOrCreateConfClass(String confClassName) {
        ConfClass res = registeredConfClasses.get(confClassName);
        if (res == null) {
            res = new ConfClass(confClassName);
            registeredConfClasses.put(confClassName, res);
        }
        return res;
    }
    
    public ConfClass getConfClassOrNull(String confClassName) {
        return registeredConfClasses.get(confClassName);
    }

    public ConfClass getConfClassOrEx(String confClassName) {
        ConfClass res = registeredConfClasses.get(confClassName);
        if (res == null) {
            throw new IllegalArgumentException();
        }
        return res;
    }

}
