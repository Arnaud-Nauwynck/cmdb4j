package org.cmdb4j.core.model;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourceIdTest {

    @Test
    public void testJson() throws Exception {
        ObjectMapper jacksonMapper = new ObjectMapper();
        ResourceId resourceId = ResourceId.valueOf("a", "b");
        JsonNode json = jacksonMapper.valueToTree(resourceId);
        ResourceId resourceId2 = jacksonMapper.treeToValue(json, ResourceId.class);
        Assert.assertEquals(resourceId2, resourceId);
    }
}
