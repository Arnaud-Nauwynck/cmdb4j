package org.cmdb4j.core.env.input;


import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import fr.an.fxtree.model.FxNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ResourceFileContent {
	
    private final FxSourceLoc source;
    private final FxNode data;
    
}


