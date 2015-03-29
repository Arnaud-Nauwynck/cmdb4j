package org.cmdb4j.core.objs;

import org.cmdb4j.core.util.StdTextValueParsers;
import org.cmdb4j.core.util.TextValueParser;

public class ConfPropDef<T> {

    private final ConfClass ownerClass;
    private final String propertyName;
    
    private final boolean readOnlyProp;

    private final Class<?> propertyType; 
    private final TextValueParser<T> valueParser;
    
    private final boolean allowNull;
    
    private final String defaultValueExpr;
    private final T defaultResolvedValue;
    
    
    
    
    // ------------------------------------------------------------------------

    /** called from owner ConfClass.putProperty() */
    /*pp*/ ConfPropDef(ConfClass ownerClass, String propertyName, Builder<T> builder) {
        super();
        this.ownerClass = ownerClass;
        this.propertyName = propertyName;
        this.readOnlyProp = builder.isReadOnlyProp();
        this.propertyType = builder.getPropertyType();
        this.valueParser = builder.getValueParser();
        this.allowNull = builder.isAllowNull();
        this.defaultValueExpr = builder.getDefaultValueExpr();
        this.defaultResolvedValue = builder.getDefaultResolvedValue();
    }

    // ------------------------------------------------------------------------
    
    public ConfClass getOwnerClass() {
        return ownerClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean isReadOnlyProp() {
        return readOnlyProp;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }
    
    public TextValueParser<T> getValueParser() {
        return valueParser;
    }

    public boolean isAllowNull() {
        return allowNull;
    }
    
    public String getDefaultValueExpr() {
        return defaultValueExpr;
    }
    
    public T getDefaultResolvedValue() {
        return defaultResolvedValue;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return propertyName 
                + " (" + propertyType + ")"
                + ((!readOnlyProp)? " read-write" : "") 
                ;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Builder-like design-patter ... simplify API usage + chicken & egg pb creating ConfClass<->ConfPropDef
     */
    public static class Builder<T> {
    
        private Class<?> propertyType; 
        private TextValueParser<T> valueParser;
        
        private boolean readOnlyProp;
        private boolean allowNull;
        
        private String defaultValueExpr;
        private T defaultResolvedValue;
        
        // ------------------------------------------------------------------------

        public Builder(Class<T> propertyType) {
            this(propertyType, StdTextValueParsers.stdParserFor(propertyType));
        }
        
        public Builder(Class<T> propertyType, TextValueParser<T> valueParser) {
            this.propertyType = propertyType;
            this.valueParser = valueParser;
        }
        
        // ------------------------------------------------------------------------

        // build() => cf ConfClass.putProperty(String,ConfPropDef)
        
        
        public Class<?> getPropertyType() {
            return propertyType;
        }
        
        public TextValueParser<T> getValueParser() {
            return valueParser;
        }

        public boolean isReadOnlyProp() {
            return readOnlyProp;
        }
        
        public Builder<T> withReadOnlyProp(boolean readOnlyProp) {
            this.readOnlyProp = readOnlyProp;
            return this;
        }

        public boolean isAllowNull() {
            return allowNull;
        }
        
        public Builder<T> withAllowNull(boolean allowNull) {
            this.allowNull = allowNull;
            return this;
        }

        public String getDefaultValueExpr() {
            return defaultValueExpr;
        }
        
        public Builder<T> withDefaultValueExpr(String defaultValueExpr) {
            this.defaultValueExpr = defaultValueExpr;
            return this;
        }

        public T getDefaultResolvedValue() {
            return defaultResolvedValue;
        }

        public Builder<T> withDefaultResolvedValue(T defaultResolvedValue) {
            this.defaultResolvedValue = defaultResolvedValue;
            return this;
        }
        
        
    }
    
}
