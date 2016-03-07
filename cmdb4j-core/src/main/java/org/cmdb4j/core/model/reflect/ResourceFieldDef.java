package org.cmdb4j.core.model.reflect;

import org.cmdb4j.core.util.StdTextValueParsers;
import org.cmdb4j.core.util.TextValueParser;

public class ResourceFieldDef {

    private final ResourceType ownerType;
    private final String name;
    
    private final boolean readOnlyProp;

    private final Class<?> valueType; 
    private final TextValueParser<?> valueParser;
    
    private final boolean allowNull;
    
    private final String defaultValueExpr;
    private final Object defaultResolvedValue;
    
    
    
    
    // ------------------------------------------------------------------------

    /** called from owner ConfClass.putProperty() */
    ResourceFieldDef(ResourceType ownerClass, String propertyName, Builder builder) {
        super();
        this.ownerType = ownerClass;
        this.name = propertyName;
        this.readOnlyProp = builder.isReadOnlyProp();
        this.valueType = builder.getValueType();
        this.valueParser = builder.getValueParser();
        this.allowNull = builder.isAllowNull();
        this.defaultValueExpr = builder.getDefaultValueExpr();
        this.defaultResolvedValue = builder.getDefaultResolvedValue();
    }

    // ------------------------------------------------------------------------
    
    public ResourceType getOwnerClass() {
        return ownerType;
    }

    public String getPropertyName() {
        return name;
    }

    public boolean isReadOnlyProp() {
        return readOnlyProp;
    }

    public Class<?> getPropertyType() {
        return valueType;
    }
    
    public TextValueParser<?> getValueParser() {
        return valueParser;
    }

    public boolean isAllowNull() {
        return allowNull;
    }
    
    public String getDefaultValueExpr() {
        return defaultValueExpr;
    }
    
    public Object getDefaultResolvedValue() {
        return defaultResolvedValue;
    }
    
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return name 
                + " (" + valueType + ")"
                + ((!readOnlyProp)? " read-write" : "") 
                ;
    }
    
    // ------------------------------------------------------------------------

    /**
     * Builder-like design-patter
     */
    public static class Builder {
    
        private Class<?> valueType; 
        private TextValueParser<?> valueParser;
        
        private boolean readOnlyProp;
        private boolean allowNull;
        
        private String defaultValueExpr;
        private Object defaultResolvedValue;
        
        // ------------------------------------------------------------------------

        public Builder(Class<?> valueType) {
            this(valueType, StdTextValueParsers.stdParserFor(valueType));
        }
        
        public Builder(Class<?> valueType, TextValueParser<?> valueParser) {
            this.valueType = valueType;
            this.valueParser = valueParser;
        }
        
        // ------------------------------------------------------------------------

        // build() => cf ResourceType.putField(String,ResourceFieldDef)
        
        
        public Class<?> getValueType() {
            return valueType;
        }
        
        public TextValueParser<?> getValueParser() {
            return valueParser;
        }

        public boolean isReadOnlyProp() {
            return readOnlyProp;
        }
        
        public Builder withReadOnlyProp(boolean readOnlyProp) {
            this.readOnlyProp = readOnlyProp;
            return this;
        }

        public boolean isAllowNull() {
            return allowNull;
        }
        
        public Builder withAllowNull(boolean allowNull) {
            this.allowNull = allowNull;
            return this;
        }

        public String getDefaultValueExpr() {
            return defaultValueExpr;
        }
        
        public Builder withDefaultValueExpr(String defaultValueExpr) {
            this.defaultValueExpr = defaultValueExpr;
            return this;
        }

        public Object getDefaultResolvedValue() {
            return defaultResolvedValue;
        }

        public Builder withDefaultResolvedValue(Object defaultResolvedValue) {
            this.defaultResolvedValue = defaultResolvedValue;
            return this;
        }
        
        
    }
    
}
