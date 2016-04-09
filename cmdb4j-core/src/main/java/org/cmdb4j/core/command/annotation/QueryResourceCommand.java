package org.cmdb4j.core.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe a resource object Query Command = command without any side-effect
 * 
 * <PRE>
 * class FooCommandProviderAdapter implements IResourceCommandProvider {
 * 
 *   public FooAdapter(Resource adaptee) { .. } 
 * 
 *   @QueryResourceCommand(name="someQuery",
 *     resourceType="someType..."
 *     conditions={ @ResourceExpr("someExpr..."),  @ResourceExpr("someExpr...") }
 *   )
 *   public FooCmdResult someCommand(
 *       CommandExecutionCtx ctx,
 *       Resource targetResource,
 *       @Param(name="arg1", description="..", required=true) FooCmdParam1 arg1, 
 *       @Param(name="arg2", description="..", required=false, defaultValue="..") FooCmdParam2 arg2) {
 *     .. 
 *   }
 *   
 * }
 * </PRE>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QueryResourceCommand {

    /**
     * name of ResourceType
     */
    String resourceType();
    
    /**
     * name of the command 
     */
    String name();

    /**
     * name of the command 
     */
    String[] aliases() default {};

    /**
     *
     */
    String category() default "";

    /**
     * @return
     */
    ResourceExpr[] conditions() default {}; 

    /**
     * help message for this command
     */
    String help() default "";

}
