package org.cmdb4j.core.model.reflect;

import org.cmdb4j.core.util.StdTextValueParsers;
import org.cmdb4j.core.util.TextValueParser;

public class ResourceFieldDef {

    private final ResourceType ownerType;
    private final String name;
    
    private final String description;
    
    private final boolean readOnlyProp;

    private final Class<?> valueType;
    private final TextValueParser<?> valueParser;
    
    private final boolean allowNull;
    
    private final String defaultValueExpr;
    private final Object defaultResolvedValue;
    
    
    
    
    // ------------------------------------------------------------------------

    /** called from owner ResourceType() */
    /*pp*/ ResourceFieldDef(ResourceType ownerClass, String propertyName, Builder builder) {
        this.ownerType = ownerClass;
        this.name = propertyName;
        this.description = builder.getDescription();
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
    
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public boolean isReadOnlyProp() {
        return readOnlyProp;
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

        private String description;

        private boolean readOnlyProp;
        private boolean allowNull;
        
        private String defaultValueExpr;
        private Object defaultResolvedValue;
        
        // ------------------------------------------------------------------------

        public Builder() {
        }

        // ------------------------------------------------------------------------

        // build() => cf ResourceType.putField(String,ResourceFieldDef.Builder)

        public Builder valueType(Class<?> p) {
            this.valueType = p;
            if (valueParser == null) {
                valueParser = StdTextValueParsers.stdParserFor(valueType);
            }
            return this;
        }
        
        public Builder valueParser(TextValueParser<?> valueParser) {
            this.valueParser = valueParser;
            return this;
        }

        public Class<?> getValueType() {
            return valueType;
        }
        
        public TextValueParser<?> getValueParser() {
            return valueParser;
        }
        
        public String getDescription() {
            return description;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public boolean isReadOnlyProp() {
            return readOnlyProp;
        }
        
        public Builder readOnlyProp(boolean readOnlyProp) {
            this.readOnlyProp = readOnlyProp;
            return this;
        }

        public boolean isAllowNull() {
            return allowNull;
        }
        
        public Builder allowNull(boolean allowNull) {
            this.allowNull = allowNull;
            return this;
        }

        public String getDefaultValueExpr() {
            return defaultValueExpr;
        }
        
        public Builder defaultValueExpr(String defaultValueExpr) {
            this.defaultValueExpr = defaultValueExpr;
            return this;
        }

        public Object getDefaultResolvedValue() {
            return defaultResolvedValue;
        }

        public Builder defaultResolvedValue(Object defaultResolvedValue) {
            this.defaultResolvedValue = defaultResolvedValue;
            return this;
        }
        
        
    }
    
}
