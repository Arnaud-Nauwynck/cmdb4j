package org.cmdb4j.core.ext.elasticsearch.filter.parser;

import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.ChainSearchHitFilter;
import org.junit.Assert;
import org.junit.Test;

public class FilterChainASTFacadeTest {

	@Test
	public void testParse() {
		String text = "when f1 = 'abc' then reject";
		ChainSearchHitFilter res = FilterChainASTFacade.parse(text);
		ChainSearchHitFilter expected = SearchHitFilter.chainFilters(false, 
				SearchHitFilter.fieldEq("f1", "abc"));
		Assert.assertEquals(expected, res);
	}

	@Test
	public void testParse2() {
		String text = "when f1 = 'abc' then reject\n"
				+ "when f2 ~ 'abc 123' then reject";
		ChainSearchHitFilter res = FilterChainASTFacade.parse(text);
		ChainSearchHitFilter expected = SearchHitFilter.chainFilters(
				false, SearchHitFilter.fieldEq("f1", "abc"),
				false, SearchHitFilter.fieldTemplatePattern("f2", "abc 123"));
		Assert.assertEquals(expected, res);
	}

}
