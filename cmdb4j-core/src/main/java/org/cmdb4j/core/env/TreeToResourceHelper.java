package org.cmdb4j.core.env;

import java.util.LinkedHashMap;
import java.util.Map;

import org.cmdb4j.core.model.Resource;
import org.cmdb4j.core.model.ResourceId;
import org.cmdb4j.core.model.reflect.ResourceType;
import org.cmdb4j.core.model.reflect.ResourceTypeRepository;

import fr.an.fxtree.impl.helper.FxObjNodeWithIdAndTypeTreeScanner;
import fr.an.fxtree.impl.helper.FxReplaceNodeCopyVisitor;
import fr.an.fxtree.impl.model.mem.FxMemRootDocument;
import fr.an.fxtree.impl.stdfunc.FxPhaseRecursiveEvalFunc;
import fr.an.fxtree.impl.stdfunc.FxStdFuncs;
import fr.an.fxtree.model.FxNode;
import fr.an.fxtree.model.func.FxEvalContext;
import fr.an.fxtree.model.func.FxNodeFuncRegistry;

public class TreeToResourceHelper {

    // ------------------------------------------------------------------------

    public TreeToResourceHelper() {
    }

    // ------------------------------------------------------------------------

    public FxNode preprocessNode(String envName, FxNode rawNode, EnvTemplateInstanceParameters templateParams) {
        FxNodeFuncRegistry funcRegistry = FxStdFuncs.stdFuncRegistry();
        FxEvalContext ctx = new FxEvalContext(null, funcRegistry);

        // step 1: replace template parameters
        FxNode templateWithReplacedParamNode;
        if (templateParams == null) {
            templateWithReplacedParamNode = rawNode;
        } else {
            FxMemRootDocument templateReplDoc = new FxMemRootDocument();
            FxReplaceNodeCopyVisitor.copyWithReplaceTo(templateReplDoc.contentWriter(), rawNode, 
                templateParams.getTemplateParameters());
            templateWithReplacedParamNode = templateReplDoc.getContent();
        }

        // step 2: preprocess eval : recursive replace all "@eval-function" by their invocation result
        FxPhaseRecursiveEvalFunc phase0Func = new FxPhaseRecursiveEvalFunc("phase0", funcRegistry);
        FxMemRootDocument processedDoc = new FxMemRootDocument();
        ctx.putVariable("env", envName);
        phase0Func.eval(processedDoc.contentWriter(), ctx, templateWithReplacedParamNode);

        FxNode res = processedDoc.getContent();
        return res;
    }


    /**
     * recursive scan all nodes with <PRE>{id="", type="" ...}</PRE>
     */
    public static Map<ResourceId, Resource> recursiveScanResourceElts(FxNode rootNode, ResourceTypeRepository typeRepository) {
        Map<ResourceId, Resource> res = new LinkedHashMap<>();
        FxObjNodeWithIdAndTypeTreeScanner.scanConsumeFxNodesWithIdTypeObj(rootNode, (id, typeName, objNode) -> {
            ResourceId resourceId = ResourceId.valueOf(id);
            ResourceType type = typeRepository.getOrCreateType(typeName);
            Resource elt = new Resource(resourceId, type, objNode);
            res.put(resourceId, elt);
        });
        return res;
    }

}
