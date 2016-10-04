package org.cmdb4j.core.ext.elasticsearch.filter;

import java.util.regex.Pattern;

import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.AndSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.ChainSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.ChainSearchHitFilterElement;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.EqualsFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.InverseSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.OrSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.PatternFieldSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.StrictAcceptSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.StrictRejectSearchHitFilter;
import org.cmdb4j.core.ext.elasticsearch.filter.SearchHitFilters.TemplatePatternSearchHitFilter;
import org.cmdb4j.core.ext.patterns.tokentemplate.TemplatizedTokenKeyPath;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="type")
@JsonSubTypes({
	@JsonSubTypes.Type(value=EqualsFieldSearchHitFilter.class, name="fieldEq"),
    @JsonSubTypes.Type(value=PatternFieldSearchHitFilter.class, name="fieldMatch"),
    @JsonSubTypes.Type(value=TemplatePatternSearchHitFilter.class, name="fieldTemplatePattern"),
    @JsonSubTypes.Type(value=ChainSearchHitFilter.class, name="chainFilters"),
    @JsonSubTypes.Type(value=InverseSearchHitFilter.class, name="inverse"),
    @JsonSubTypes.Type(value=StrictAcceptSearchHitFilter.class, name="strictAccept"),
    @JsonSubTypes.Type(value=StrictRejectSearchHitFilter.class, name="strictReject"),
    @JsonSubTypes.Type(value=AndSearchHitFilter.class, name="and"),
    @JsonSubTypes.Type(value=OrSearchHitFilter.class, name="or"),
    
}) 
public abstract class SearchHitFilter {

	public abstract SearchHitFilterDecision accept(SearchHit obj);

	public abstract void visit(SearchHitFilterVisitor visitor);
	
	// ------------------------------------------------------------------------
	
	public static SearchHitFilter fieldEq(String field, Object value) {
		return new EqualsFieldSearchHitFilter(field, value);
	}
	public static SearchHitFilter fieldMatch(String field, Pattern pattern) {
		return new PatternFieldSearchHitFilter(field, pattern);
	}
	public static SearchHitFilter fieldTemplatePattern(String field, String value) {
		return new TemplatePatternSearchHitFilter(field, value);
	}
	public static SearchHitFilter chainFilters(ChainSearchHitFilterElement... chainElts) {
		return new ChainSearchHitFilter(chainElts);
	}
	public static ChainSearchHitFilter chainFilters(boolean include0, SearchHitFilter filter0, boolean include1, SearchHitFilter filter1) {
		return new ChainSearchHitFilter(new ChainSearchHitFilterElement[]{
				new ChainSearchHitFilterElement(include0, filter0),
				new ChainSearchHitFilterElement(include1, filter1)
		});
	}
	public static SearchHitFilter inverse(SearchHitFilter underlying) {
		return new InverseSearchHitFilter(underlying);
	}
	public static SearchHitFilter strictAccept(SearchHitFilter underlying) {
		return new StrictAcceptSearchHitFilter(underlying);
	}
	public static SearchHitFilter strictReject(SearchHitFilter underlying) {
		return new StrictRejectSearchHitFilter(underlying);
	}
	public static SearchHitFilter and(SearchHitFilter... elts) {
		return new AndSearchHitFilter(elts);
	}
	public static SearchHitFilter or(SearchHitFilter... elts) {
		return new OrSearchHitFilter(elts);
	}
	
}
