package org.cmdb4j.core.command.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ PARAMETER, FIELD })
public @interface Param {

    /**
     * name of the parameter... when not set (as of jdk8... the java code method name is used)
     */
    String name() default "";

    /**
     * 
     */
    String[] aliases() default {};

    /**
     * description of this parameter
     */
    String description() default "";

    /**
     * Whether this parameter is required.
     */
    boolean required() default false;

    /**
     * 
     */
    String defaultValue();

}
