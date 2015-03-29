package org.cmdb4j.core.objs;

public abstract class ConfProp<T> {

    // implicit... protected final ConfObject ownerObject;
    protected final ConfPropDef<T> propDef;
    
    // ------------------------------------------------------------------------

    protected ConfProp(// ConfObject ownerObject, 
            ConfPropDef<T> propDef) {
        this.propDef = propDef;
    }
    
    // ------------------------------------------------------------------------
    
    public ConfPropDef<T> getPropDef() {
        return propDef;
    }

    public abstract T getValue();

    
    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return propDef.getPropertyName() + ": " + getValue();
    }


    public static class ReadonlyValueConfProp<T> extends ConfProp<T> {

        private final T value;
        
        public ReadonlyValueConfProp(ConfPropDef<T> propDef, T value) {
            super(propDef);
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return propDef.getPropertyName() + ": " + value;
        }
    }

    // ------------------------------------------------------------------------
    
    /**
     * a property value... that can change dependending of other HieraParam values (?) 
     * @param <T>
     */
    public static class ResolvedValueConfProp<T> extends ConfProp<T> {

        private String valueParamExpr;
        private T value;
        
        public ResolvedValueConfProp(ConfPropDef<T> propDef, String valueParamExpr, T value) {
            super(propDef);
            this.valueParamExpr = valueParamExpr;
            this.value = value;
        }
        
        public String getValueParamExpr() {
            return valueParamExpr;
        }
        
        public T getValue() {
            return value;
        }

        public void setResolvedValue(String valueParamExpr, T value) {
            this.valueParamExpr = valueParamExpr;
            this.value = value;
        }
     
        @Override
        public String toString() {
            return propDef.getPropertyName() + ": " + value
                    + "(from " + valueParamExpr + ")"
                    ;
        }
    }

    // ------------------------------------------------------------------------
    
    // ??? Should not exists ... cf CmdbObject for runtime objects / runtime properties ...
    public static class RuntimeValueProp<T> extends ConfProp<T> {

        private T value;
        
        public RuntimeValueProp(ConfPropDef<T> propDef) {
            super(propDef);
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
        
    }
    
}
