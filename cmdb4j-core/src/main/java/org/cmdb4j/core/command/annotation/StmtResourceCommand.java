package org.cmdb4j.core.command.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to describe a resource object Command = command with side-effect
 * 
 * <PRE>
 * class FooCommandProviderAdapter implements IResourceCommandProvider {
 * 
 *   public FooAdapter(Resource adaptee) { .. } 
 * 
 *   @Command(name="someStmt",
 *     resourceType="someType..."
 *     preConditions={ @ResourceExpr("someExpr..."),  @ResourceExpr("someExpr...") }
 *     postConditions={ @ResourceExpr("someExpr..."),  @ResourceExpr("someExpr...") }
 *     sideEffects={ @ResourceSideEffect("sideEffect..."),  ResourceSideEffect("sideEffect...") }
 *   )
 *   public FooCmdResult someStmt(
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
public @interface StmtResourceCommand {

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
    ResourceExpr[] preConditions() default {}; 

    /**
     * @return
     */
    ResourceExpr[] postConditions() default {}; 

    /**
     * @return
     */
    ResourceSideEffect[] sideEffects() default {}; 

    /**
     * help message for this command
     */
    String help() default "";

}
