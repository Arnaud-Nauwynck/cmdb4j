package org.cmdb4j.core.shell;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * text for the command 
     */
    String value();

//    /**
//     *
//     */
//    String category() default "";

    /**
     * @return
     */
    ResourceExpr[] preConditions(); 

    /**
     * @return
     */
    ResourceExpr[] postConditions(); 

//    /**
//     * @return
//     */
//    ResourceSideEffect[] sideEffects(); 

    /**
     * help message for this command
     */
    String help() default "";

}
