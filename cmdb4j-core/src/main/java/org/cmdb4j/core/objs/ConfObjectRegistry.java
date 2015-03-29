package org.cmdb4j.core.objs;

import java.util.HashMap;
import java.util.Map;

import org.cmdb4j.core.hieraparams.HieraPath;

public class ConfObjectRegistry {

    private Map<HieraPath,ConfObject> confObjects = new HashMap<HieraPath,ConfObject>();
    
    // ------------------------------------------------------------------------

    public ConfObjectRegistry() {
    }

    // ------------------------------------------------------------------------
    
    public ConfObject getObjectOrNull(HieraPath id) {
        return confObjects.get(id);
    }
    
    public ConfObject getObjectOrEx(HieraPath id) {
        ConfObject res = confObjects.get(id);
        if (res == null) {
            throw new IllegalArgumentException();
        }
        return res;
    }

    public void put(HieraPath id, ConfObject obj) {
        confObjects.put(id, obj);
    }

}
