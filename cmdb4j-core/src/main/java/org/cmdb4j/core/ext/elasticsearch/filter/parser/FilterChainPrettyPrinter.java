package org.cmdb4j.core.ext.elasticsearch.filter.parser;

import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilterVisitor;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.EqualsFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.PatternFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.TemplatePatternSearchHitFilter;

public class FilterChainPrettyPrinter implements SearchHitFilterVisitor {

	@Override
	public void caseFieldEq(EqualsFieldSearchHitFilter obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseFieldPattern(PatternFieldSearchHitFilter obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseFieldTemplate(TemplatePatternSearchHitFilter obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void caseOther(SearchHitFilter obj) {
		// TODO Auto-generated method stub
		
	}

}
