package org.cmdb4j.core.ext.elasticsearch.filter;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchHitFilterTest {

	ObjectMapper mapper = new ObjectMapper();
	
	@Test
	public void testMapJson() throws Exception {
		// Prepare
		SearchHitFilter f1 = SearchHitFilter.fieldEq("name", "test");
		SearchHitFilter f2 = SearchHitFilter.fieldMatch("name", Pattern.compile("test.*"));
		SearchHitFilter f3 = SearchHitFilter.chainFilters(true, f1, false, f2);
		SearchHitFilter f4 = SearchHitFilter.inverse(f1);
		SearchHitFilter f5 = SearchHitFilter.strictAccept(f1);
		SearchHitFilter f6 = SearchHitFilter.strictReject(f1);
		SearchHitFilter f7 = SearchHitFilter.and(f1, f2);
		SearchHitFilter f8 = SearchHitFilter.or(f1, f2);
		
		SearchHitFilter[] filters = new SearchHitFilter[] {
			f1, f2, f3, f4, f5, f6, f7, f8
		};
		int filtersCount = filters.length;
		// Perform
		JsonNode[] filterJsonTrees = new JsonNode[filtersCount];
		SearchHitFilter[] filterClones = new SearchHitFilter[filtersCount];
		for(int i = 0; i < filtersCount; i++) {
			filterJsonTrees[i] = mapper.valueToTree(filters[i]);
			System.out.println("f[" + i + "]= " + filterJsonTrees[i]);
			filterClones[i] = mapper.treeToValue(filterJsonTrees[i], SearchHitFilter.class);
		}
		// Post-check
		for(int i = 0; i < filtersCount; i++) {
			Assert.assertEquals(filters[i], filterClones[i]);
		}
	}
	
	
	@Test
	public void testMapJson2() throws Exception {
	}

}
