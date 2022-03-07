package org.cmdb4j.core.env.prototype;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxObjNode;

/**
 * a Prototype for sharing
 * 
 * correspond to yaml:
 * <PRE>
 * - id: <<someId>>
 *   type: Prototype
 *   params:
 *     - name: <<param1>>
 *        default: <<default1>>
 *   template:
 *     ..
 * </PRE>
 *
 */
public class FxObjNodePrototype {

    public static class FxObjNodePrototypeParam {
        public final String name;
        public final FxNode defaultValue;
        public FxObjNodePrototypeParam(String name, FxNode defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
    }
    
    private final String name;
    private final ImmutableList<FxObjNodePrototypeParam> params;
    private final FxObjNode template;
    
    // ------------------------------------------------------------------------
    
    public FxObjNodePrototype(String name, ImmutableList<FxObjNodePrototypeParam> params, FxObjNode template) {
        this.name = name;
        this.params = params;
        this.template = template;
    }

    // ------------------------------------------------------------------------
    
    public String getName() {
        return name;
    }

    public ImmutableList<FxObjNodePrototypeParam> getParams() {
        return params;
    }

    public FxObjNode getTemplate() {
        return template;
    }
    
    
}