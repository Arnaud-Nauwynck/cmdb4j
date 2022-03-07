package org.cmdb4j.core.env.prototype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdb4j.core.env.input.CmdbInputsSource;
import org.cmdb4j.core.env.input.ResourceFileContent;
import org.cmdb4j.core.env.prototype.FxObjNodePrototype.FxObjNodePrototypeParam;

import com.google.common.collect.ImmutableList;

import fr.an.fxtree.impl.helper.FxDefaultTreeVisitor;
import fr.an.fxtree.impl.helper.FxNodeCopyVisitor;
import fr.an.fxtree.impl.helper.FxObjValueHelper;
import fr.an.fxtree.impl.helper.FxReplaceNodeCopyVisitor;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.stdfunc.FxMergeFunc;
import fr.an.fxtree.model.FxArrayNode;
import fr.an.fxtree.model.FxChildWriter;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.FxNullNode;
import fr.an.fxtree.model.FxObjNode;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * registry of FxObjNodePrototype by name
 * 
 * protype act as pre-processor for enriching FxNode objects (so resources) 
 */
@Slf4j
public class FxObjNodePrototypeRegistry {

    private static final String CST_Prototype = "Prototype";
            
    private Map<String,FxObjNodePrototype> entries = new HashMap<>();
    
    // ------------------------------------------------------------------------

    public FxObjNodePrototypeRegistry() {
    }

    // ------------------------------------------------------------------------

//    // scan all files "<dirInputs.dirConfig>/Default/prototypes/**/prototypes-*.yaml"
//    Predicate<String> fileNamesPred = (String f) -> f.startsWith("prototypes-");
//    List<ResourceFileContent> prototypeFileContents = envsInputSource.scanDefaultResourceFxFiles("prototypes", fileNamesPred);

    public FxObjNodePrototypeRegistry(CmdbInputsSource envsInputSource) {
		// TODO ARN
	}

	public void registerPrototypes(List<ResourceFileContent> fileContents) {
    	for(val fileContent: fileContents) {
    		parseResourcePrototypes(entries, fileContent)    		;
    	}
    }
    
    public FxObjNodePrototype findById(String id) {
        return entries.get(id);
    }
    
    public FxNode copyPreprocessPrototypes(FxNode src) {
        FxNodeCopyVisitor transformCopier = new FxNodeCopyVisitor() {
            @Override
            public FxNode visitObj(FxObjNode src, FxChildWriter out) {
                // detect if contains field "prototype" (or "prototypes" ?)
                FxNode prototypeNode = src.get("prototype");
                if (prototypeNode != null) {
                    String prototypeId = prototypeNode.textValue();
                    FxObjNodePrototype proto = findById(prototypeId);
                    if (proto != null) {
                        try {
                            return processObjNodeWithPrototype(proto, src, out);
                        } catch(Exception ex) {
                            log.error("Failed to preprocess prototype id '" + prototypeId + "' for obj " + src + " ..ignore!", ex);
                            return src; //?
                        }
                    } else {
                        log.error("prototype id '" + prototypeId + "' not found for pre-processing obj " + src + " ..ignore!");
                        return super.visitObj(src, out);
                    }
                } else {
                    return super.visitObj(src, out);
                }
            }
            
            protected FxNode processObjNodeWithPrototype(FxObjNodePrototype proto, FxObjNode src, FxChildWriter out) {
                FxObjNode res = (FxObjNode) FxNodeCopyVisitor.copyTo(out, src);
                // TODO.. may remove field "prototype"
                
                FxObjNode template = proto.getTemplate();
                // resolve param values to replace, optionnaly use param defaults
                Map<String,FxNode> paramReplacements = new HashMap<>();
                Map<String, FxNode> srcFields = src.fieldsMap();
                for (FxObjNodePrototypeParam param : proto.getParams()) {
                    FxNode paramValue = srcFields.get(param.name);
                    if (paramValue == null) {
                        // TODO also search by param alias .. 
                        paramValue = param.defaultValue;
                    }
                    if (paramValue != null) {
                        paramReplacements.put(param.name, paramValue);
                    } else {
                        log.error("prototype " + proto.getName() + " param '" + param.name + "' not found in source node " + src + " to apply prototype pre-processing");
                    }
                }
                
                // replace (recurse) value in param value
                for (FxObjNodePrototypeParam param : proto.getParams()) {
                    FxNode paramValue = paramReplacements.get(param.name);
                    if (paramValue != null) {
                        FxNode newParamValue = FxReplaceNodeCopyVisitor.copyWithReplace(paramValue, paramReplacements);
                        paramReplacements.put(param.name, newParamValue);
                    } else {
                        // log.error("prototype " + proto.getName() + " param '" + param.name + "' is null");
                    }
                }
                
                // replace params values in template copy
                FxObjNode templateRepl = (FxObjNode) 
                        FxReplaceNodeCopyVisitor.copyWithReplace(template, paramReplacements);
                
                // merge copy missing fields from replaced prototype template
                FxMergeFunc.recursiveMergeAddMissing(res, templateRepl.fieldsMap());
                
                return res;
            }
        };
        FxMemRootDocument doc = new FxMemRootDocument(src.getSourceLoc());
        FxChildWriter writer = doc.contentWriter();
        src.accept(transformCopier, writer);
        return doc.getContent();
    }

    
    // ------------------------------------------------------------------------
    
    protected void parseResourcePrototypes(Map<String,FxObjNodePrototype> res, ResourceFileContent file) {
        FxNode fileData = file.getData();

        // recursive parse FxObj containing type:"Prototype"
        fileData.accept(new FxDefaultTreeVisitor() {
            @Override
            public void visitObj(FxObjNode node) {
                FxNode idNode = node.get("id");
                FxNode typeNode = node.get("type");
                FxNode paramsNode = node.get("params");
                FxNode templateNode = node.get("template");
                if (idNode != null && idNode.isTextual() 
                        && typeNode != null && typeNode.isTextual()
                        && typeNode.textValue().equals(CST_Prototype)
                        ) {
                    String id = idNode.textValue();
                    if (templateNode != null && templateNode.isObject()) {
                        try {
                            List<FxObjNodePrototypeParam> params = parsePrototypeParams(id, paramsNode);
                            FxObjNodePrototype elt = new FxObjNodePrototype(id, ImmutableList.copyOf(params), (FxObjNode) templateNode);
                            res.put(id, elt);
                        } catch(Exception ex) {
                            log.error("Failed to parse prototype '" + id + "' ..ignore!", ex);
                        }
                        // no recurse visit
                    } else {
                        log.error("prototype '" + id + "' has no 'template:' ..ignore!");
                        // no recurse visit
                    }
                } else {
                    super.visitObj(node);
                }
            }
        });
    }
    
    protected List<FxObjNodePrototypeParam> parsePrototypeParams(final String id, FxNode paramsNode) {
        List<FxObjNodePrototypeParam> res = new ArrayList<>();
        if (paramsNode instanceof FxArrayNode) {
            FxArrayNode paramsArray = (FxArrayNode) paramsNode;
            for(FxNode paramNode : paramsArray.children()) {
                if (paramNode.isTextual()) {
                    String name = paramNode.textValue();
                    res.add(new FxObjNodePrototypeParam(name, null));
                } else if (paramNode.isObject()) {
                    FxObjValueHelper paramObj = new FxObjValueHelper((FxObjNode) paramNode);
                    String name = paramObj.getStringOrThrow("name");
                    FxNode defaultValue = paramObj.getOrNull("default");
                    if (defaultValue instanceof FxNullNode) {
                        defaultValue = null;
                    }                    
                    res.add(new FxObjNodePrototypeParam(name, defaultValue));
                } else {
                    log.error("prototype '" + id + "' has unrecognized param node '" + paramNode + "'");
                    continue;
                }
            }
        } else if (paramsNode == null) {
            // ok, ignore missing field "params:"
        } else if (paramsNode.isNull()) {
            // ok, ignore empty field "params:" 
        } else {
            log.error("prototype '" + id + "' params: not set to an array! .. ignore");
        }
        return res;
    }

	public void purgeReload() {
		// TODO ARN
		
	}

}
