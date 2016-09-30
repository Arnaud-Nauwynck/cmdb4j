package org.cmdb4j.core.ext.elasticsearch.filter;

import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.EqualsFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.PatternFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.TemplatePatternSearchHitFilter;

public interface SearchHitFilterVisitor {

	public void caseFieldEq(EqualsFieldSearchHitFilter obj);
	public void caseFieldPattern(PatternFieldSearchHitFilter obj);
	public void caseFieldTemplate(TemplatePatternSearchHitFilter obj);
	
	
//	public void caseChain(ChainSearchHitFilter obj);
//	public void caseInverse(InverseSearchHitFilter obj);
	// ...
	public void caseOther(SearchHitFilter obj);
	
	
}
