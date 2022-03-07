package org.cmdb4j.core.env.input;


import fr.an.fxtree.impl.model.mem.FxSourceLoc;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class ResourceFileContentBytes {
	
    private final FxSourceLoc source;
    private final byte[] data;
    
}


