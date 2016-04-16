package org.cmdb4j.core.command.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.cmdb4j.core.command.CommandExecutionCtx;
import org.cmdb4j.core.command.ResourceCommand;
import org.cmdb4j.core.command.ResourceCommand.MethodResourceCommand;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.command.annotation.QueryResourceCommand;
import org.cmdb4j.core.command.annotation.ResourceExpr;
import org.cmdb4j.core.command.annotation.ResourceSideEffect;
import org.cmdb4j.core.command.annotation.StmtResourceCommand;
import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.QueryResourceCommandInfo;
import org.cmdb4j.core.command.commandinfo.ResourceCommandInfo;
import org.cmdb4j.core.command.commandinfo.ResourceExprInfo;
import org.cmdb4j.core.command.commandinfo.ResourceSideEffectInfo;
import org.cmdb4j.core.command.commandinfo.StmtResourceCommandInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

/**
 * helper to scan <code>@QueryResourceCommand / @StmtResourceCommand</code> <BR/>
 * and create corresponding <code>ResourceCommand</code>  (default to <code>MethodResourceCommand</code>)
 */
public class AnnotatedMethodToCommandInfoHelper {

    private ResourceTypeRepository resourceTypeRepository;
    
    // ------------------------------------------------------------------------

    public AnnotatedMethodToCommandInfoHelper(ResourceTypeRepository resourceTypeRepository) {
        this.resourceTypeRepository = resourceTypeRepository;
    }

    // ------------------------------------------------------------------------

    public static class ObjectMethod {
        public Object object;
        public Method method;
        public ResourceCommandInfo commandInfo;
        
        public ObjectMethod(Object object, Method method, ResourceCommandInfo commandInfo) {
            this.object = object;
            this.method = method;
            this.commandInfo = commandInfo;
        }        
    }
    
    /**
     * scan instance method annoted with <code>@QueryResourceCommand / @StmtResourceCommand</code> 
     * on a targetObject, and create corresponding <code>MethodResourceCommand</code>
     * 
     * @param targetObj
     * @param methodToCommandProvider
     * @return
     */
    public List<ResourceCommand> scanObjectMethods(Object targetObj) {
        return scanObjectMethods(targetObj, (objMethod) -> new MethodResourceCommand(objMethod.commandInfo, objMethod.object, objMethod.method));
    }
    
    /**
     * scan instance method annoted with <code>@Command</code> on a targetObject, and create corresponding CommandWrapper for methods
     * 
     * @param targetObj
     * @param methodToCommandProvider
     * @return
     */
    public List<ResourceCommand> scanObjectMethods(Object targetObj, 
            Function<ObjectMethod,ResourceCommand> methodToCommandProvider) {
        List<ResourceCommand> res = new ArrayList<>();
        Class<?> clss = targetObj.getClass();
        scanDeclaredMethods(res, clss, targetObj, methodToCommandProvider);
        return res;
    }

    /**
     * same as scanObjectMethods() but for static method.<BR/>
     * scan instance method annoted with <code>@QueryResourceCommand / @StmtResourceCommand</code> 
     * and create corresponding <code>MethodResourceCommand</code>
     *  
     * @param clss
     * @param commandWrapper
     * @return
     */
    public List<ResourceCommand> scanStaticMethods(Class<?> clss) {
        return scanStaticMethods(clss, (objMethod) -> new MethodResourceCommand(objMethod.commandInfo, objMethod.object, objMethod.method));
    }
    
    /**
     * same as scanObjectMethods() but for static method
     *  
     * @param clss
     * @param commandWrapper
     * @return
     */
    public List<ResourceCommand> scanStaticMethods(Class<?> clss, 
            Function<ObjectMethod,ResourceCommand> methodToCommandProvider) {
        List<ResourceCommand> res = new ArrayList<>();
        scanDeclaredMethods(res, clss, null, methodToCommandProvider);
        return res;
    }

    public static ResourceCommand findFirstCommand(Collection<ResourceCommand> src, String name) {
        ResourceCommand res = null;
        for(ResourceCommand e : src) {
            if (name != null && !name.equals(e.getCommandName())) {
                continue;
            }
            res = e;
            break;
        }
        return res;
    }
    
    // ------------------------------------------------------------------------
    
    protected void scanDeclaredMethods(Collection<ResourceCommand> res, 
            Class<?> clss, Object targetObj, 
            Function<ObjectMethod,ResourceCommand> methodToCommandProvider) {
        for(Class<?> c = clss; c != Object.class; c = c.getSuperclass()) {
            Method[] methodDecls = clss.getDeclaredMethods();
            for(Method methodDecl : methodDecls) {
                boolean isStatic = Modifier.isStatic(methodDecl.getModifiers());
                if (isStatic != (targetObj == null)) {
                    continue;
                }
                ResourceCommandInfo commandInfo = null;
                if (commandInfo == null) {
                    StmtResourceCommand stmtAnnotation = methodDecl.getAnnotation(StmtResourceCommand.class);
                    if (stmtAnnotation != null) {
                        StmtResourceCommandInfo.Builder b = StmtResourceCommandInfo.builder();
                        buildBaseCommandInfo(b, methodDecl, stmtAnnotation);
                        
                        b.addPreConditions(annotationToResourceExprInfos(stmtAnnotation.preConditions()));
                        b.addPostConditions(annotationToResourceExprInfos(stmtAnnotation.postConditions()));
                        b.addSideEffects(annotationToSideEffectInfos(stmtAnnotation.sideEffects()));

                        commandInfo = b.build();
                    }
                }
                if (commandInfo == null) {
                    QueryResourceCommand queryAnnotation = methodDecl.getAnnotation(QueryResourceCommand.class);
                    if (queryAnnotation != null) {
                        QueryResourceCommandInfo.Builder b = QueryResourceCommandInfo.builder();
                        buildBaseCommandInfo(b, methodDecl, queryAnnotation);
                        
                        b.addConditions(annotationToResourceExprInfos(queryAnnotation.conditions()));
                        
                        commandInfo = b.build();
                    }
                }
                
                if (commandInfo == null) {
                    continue;
                }
                ObjectMethod objMethod = new ObjectMethod(targetObj, methodDecl, commandInfo);

                ResourceCommand commandProvider = methodToCommandProvider.apply(objMethod);
                res.add(commandProvider);
            }
        }
    }

    protected void buildBaseCommandInfo(ResourceCommandInfo.Builder b, Method methodDecl, StmtResourceCommand cmdAnnotation) {
        b.name(cmdAnnotation.name());
        ResourceType resourceType = resourceTypeRepository.getOrCreateType(cmdAnnotation.resourceType());
        b.targetResourceType(resourceType);
        b.category(cmdAnnotation.category());
        b.help(cmdAnnotation.help());
        buildParams(b, methodDecl);
    }

    protected void buildBaseCommandInfo(ResourceCommandInfo.Builder b, Method methodDecl, QueryResourceCommand cmdAnnotation) {
        b.name(cmdAnnotation.name());
        ResourceType resourceType = resourceTypeRepository.getOrCreateType(cmdAnnotation.resourceType());
        b.targetResourceType(resourceType);
        b.category(cmdAnnotation.category());
        b.help(cmdAnnotation.help());
        buildParams(b, methodDecl);
    }

    protected void buildParams(ResourceCommandInfo.Builder b, Method methodDecl) {
        Parameter[] parameters = methodDecl.getParameters();
        for(int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> paramType = param.getType();
            if (paramType.equals(CommandExecutionCtx.class)) {
                continue;
            }
            if (paramType.equals(Resource.class)) {
                continue;
            }
            b.addParam(parameterToParamInfo(param, i));
        }
    }

    /**
     * @return convert info from annotation <code>@Parameter</code> to ParamInfo
     */
    protected ParamInfo parameterToParamInfo(Parameter param, int index) {
        ParamInfo.Builder b = new ParamInfo.Builder();
        Class<?> paramType = param.getType();
        String name = param.getName();
        String description = null;
        boolean required = true; // paramType.isPrimitive();
        String defaultValue = null;

        Param annotationParam = param.getAnnotation(Param.class);
        if (annotationParam != null) {
            if (annotationParam.name() != null) {
                name = annotationParam.name();
            }
            String[] aliases = annotationParam.aliases();
            if (aliases != null) {
                for(String a : aliases) {
                    b.addAlias(a);
                }
            }
            required = annotationParam.required();
            defaultValue = annotationParam.defaultValue();
        }

        b.name(name);
        b.type(paramType );
        b.description(description);
        b.required(required);
        b.defaultValue(defaultValue);
        return b.build(index);
    }

    /**
     * @return convert info from annotation <code>@ResourceExpr</code> to ResourceExprInfo
     */
    private List<ResourceExprInfo> annotationToResourceExprInfos(ResourceExpr[] exprs) {
        List<ResourceExprInfo> res = new ArrayList<>();
        if (exprs != null) {
            for(ResourceExpr e : exprs) {
                res.add(new ResourceExprInfo(e.value()));
            }
        }
        return res;
    }

    /**
     * @return convert info from annotation <code>@ResourceSideEffect</code> to ResourceSideEffectInfo
     */
    private List<ResourceSideEffectInfo> annotationToSideEffectInfos(ResourceSideEffect[] src) {
        List<ResourceSideEffectInfo> res = new ArrayList<>();
        if (src != null) {
            for(ResourceSideEffect e : src) {
                res.add(new ResourceSideEffectInfo(e.value()));
            }
        }
        return res;
    }
    
}
