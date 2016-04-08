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
import org.cmdb4j.core.command.annotation.Command;
import org.cmdb4j.core.command.annotation.Param;
import org.cmdb4j.core.command.annotation.ResourceExpr;
import org.cmdb4j.core.command.annotation.ResourceSideEffect;
import org.cmdb4j.core.command.commandinfo.CommandInfo;
import org.cmdb4j.core.command.commandinfo.ParamInfo;
import org.cmdb4j.core.command.commandinfo.ResourceExprInfo;
import org.cmdb4j.core.command.commandinfo.ResourceSideEffectInfo;
import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

/**
 * 
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
        public CommandInfo commandInfo;
        
        public ObjectMethod(Object object, Method method, CommandInfo commandInfo) {
            this.object = object;
            this.method = method;
            this.commandInfo = commandInfo;
        }        
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
                Command cmdAnnotation = methodDecl.getAnnotation(Command.class);
                if (cmdAnnotation == null) {
                    continue;
                }
                CommandInfo.Builder b = new CommandInfo.Builder();
                b.name(cmdAnnotation.name());
                ResourceType resourceType = resourceTypeRepository.getOrCreateType(cmdAnnotation.resourceType());
                b.targetResourceType(resourceType);
                b.category(cmdAnnotation.category());
                b.addPreConditions(annotationToResourceExprInfos(cmdAnnotation.preConditions()));
                b.addPostConditions(annotationToResourceExprInfos(cmdAnnotation.postConditions()));
                b.addSideEffects(annotationToSideEffectInfos(cmdAnnotation.sideEffects()));
                b.help(cmdAnnotation.help());
                
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
                    b.addParam(parameterToParamInfo(param));
                }
                
                CommandInfo commandInfo = new CommandInfo(b);
                ObjectMethod objMethod = new ObjectMethod(targetObj, methodDecl, commandInfo);

                ResourceCommand commandProvider = methodToCommandProvider.apply(objMethod);
                res.add(commandProvider);
            }
        }
    }

    /**
     * @return convert info from annotation <code>@Parameter</code> to ParamInfo
     */
    protected ParamInfo parameterToParamInfo(Parameter param) {
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
        return new ParamInfo(b);
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
