package org.cmdb4j.core.ext.elasticsearch.filter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class SearchHitFilters {

	/** private to force all static */
	private SearchHitFilters() {}

	// ------------------------------------------------------------------------

	public static class EqualsFieldSearchHitFilter extends SearchHitFilter {
		private String field;
		private Object value;
		
		public EqualsFieldSearchHitFilter(@JsonProperty("field") String field, @JsonProperty("value") Object value) {
			this.field = field;
			this.value = value;
		}
		
		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			Map<String, Object> sourceAsMap = obj.sourceAsMap();
			if (sourceAsMap == null) {
				return SearchHitFilterDecision.UNKNOWN; // REJECT?
			}
			Object fieldObjValue = sourceAsMap.get(field);
			if (fieldObjValue == null) {
				return SearchHitFilterDecision.UNKNOWN; // REJECT?
			}
			boolean eq = fieldObjValue.equals(value);
			return eq? SearchHitFilterDecision.ACCEPT : SearchHitFilterDecision.REJECT;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EqualsFieldSearchHitFilter other = (EqualsFieldSearchHitFilter) obj;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "EqualsField[" + field + " == " + value + "]";
		}
		
	}
	
	// ------------------------------------------------------------------------

	public static class PatternFieldSearchHitFilter extends SearchHitFilter {
		private String field;
		private Pattern pattern;
		
		public PatternFieldSearchHitFilter(@JsonProperty("field") String field, @JsonProperty("pattern") Pattern pattern) {
			this.field = field;
			this.pattern = pattern;
		}
		
		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public void setPattern(Pattern pattern) {
			this.pattern = pattern;
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			Map<String, Object> sourceAsMap = obj.sourceAsMap();
			if (sourceAsMap == null) {
				return SearchHitFilterDecision.UNKNOWN; // REJECT?
			}
			Object fieldObjValue = sourceAsMap.get(field);
			if (fieldObjValue == null) {
				return SearchHitFilterDecision.UNKNOWN; // REJECT?
			}
			String fieldObjText = fieldObjValue.toString();  
			boolean match = pattern.matcher(fieldObjText).matches();
			return match? SearchHitFilterDecision.ACCEPT : SearchHitFilterDecision.REJECT;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((field == null) ? 0 : field.hashCode());
			result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PatternFieldSearchHitFilter other = (PatternFieldSearchHitFilter) obj;
			if (field == null) {
				if (other.field != null)
					return false;
			} else if (!field.equals(other.field))
				return false;
			if (pattern == null) {
				if (other.pattern != null)
					return false;
			} else if (!pattern.pattern().equals(other.pattern.pattern()))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "PatternField[" + field + " matches? '" + pattern.pattern() + "']";
		}

	}
	
	// ------------------------------------------------------------------------
	
	public static class ChainSearchHitFilterElement {
		boolean include;
		SearchHitFilter filter;
		
		public ChainSearchHitFilterElement(@JsonProperty("include") boolean include, @JsonProperty("filter") SearchHitFilter filter) {
			this.include = include;
			this.filter = filter;
		}
		
		public boolean isInclude() {
			return include;
		}

		public void setInclude(boolean include) {
			this.include = include;
		}

		public SearchHitFilter getFilter() {
			return filter;
		}
		public void setFilter(SearchHitFilter filter) {
			this.filter = filter;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((filter == null) ? 0 : filter.hashCode());
			result = prime * result + (include ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChainSearchHitFilterElement other = (ChainSearchHitFilterElement) obj;
			if (filter == null) {
				if (other.filter != null)
					return false;
			} else if (!filter.equals(other.filter))
				return false;
			if (include != other.include)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return (include? "include" : "exclude") + " " + filter;
		}
		
	}
	
	public static class ChainSearchHitFilter extends SearchHitFilter {
		
		private ChainSearchHitFilterElement[] elts;
		
		public ChainSearchHitFilter(@JsonProperty("elts") ChainSearchHitFilterElement[] elts) {
			this.elts = elts;
		}
		
		public ChainSearchHitFilter(@JsonProperty("elts") List<ChainSearchHitFilterElement> elts) {
			this.elts = elts.toArray(new ChainSearchHitFilterElement[elts.size()]);
		}
		
		public ChainSearchHitFilterElement[] getElts() {
			return elts;
		}

		public void setElts(ChainSearchHitFilterElement[] elts) {
			this.elts = elts;
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				SearchHitFilterDecision tmpres = elts[i].filter.accept(obj);
				if (tmpres != SearchHitFilterDecision.UNKNOWN) {
					return elts[i].include? tmpres : tmpres.inverse();
				}
			}
			return SearchHitFilterDecision.UNKNOWN;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(elts);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChainSearchHitFilter other = (ChainSearchHitFilter) obj;
			if (!Arrays.equals(elts, other.elts))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("FilterChain[\n");
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				sb.append(elts[i]);
				if (i + 1 < len) {
					sb.append("\n");
				}
			}
			sb.append("\n]");
			return sb.toString();
		}

	}
	
	// ------------------------------------------------------------------------
	

	protected static abstract class AbstractUnderlyingSearchHitFilter extends SearchHitFilter {
		
		protected SearchHitFilter underlying;
		
		protected AbstractUnderlyingSearchHitFilter(SearchHitFilter underlying) {
			this.underlying = underlying;
		}
		
		public SearchHitFilter getUnderlying() {
			return underlying;
		}

		public void setUnderlying(SearchHitFilter underlying) {
			this.underlying = underlying;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((underlying == null) ? 0 : underlying.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractUnderlyingSearchHitFilter other = (AbstractUnderlyingSearchHitFilter) obj;
			if (underlying == null) {
				if (other.underlying != null)
					return false;
			} else if (!underlying.equals(other.underlying))
				return false;
			return true;
		}

		
	}
	
	// ------------------------------------------------------------------------
	
	public static class InverseSearchHitFilter extends AbstractUnderlyingSearchHitFilter {

		public InverseSearchHitFilter(@JsonProperty("underlying") SearchHitFilter underlying) {
			super(underlying);
		}
		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			SearchHitFilterDecision tmpres = underlying.accept(obj);
			return tmpres.inverse(); 
		}
		
		@Override
		public String toString() {
			return "Inverse[" + underlying + "]";
		}
		
	}

	// ------------------------------------------------------------------------

	public static class StrictAcceptSearchHitFilter extends AbstractUnderlyingSearchHitFilter {

		public StrictAcceptSearchHitFilter(@JsonProperty("underlying") SearchHitFilter underlying) {
			super(underlying);
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			SearchHitFilterDecision tmpres = underlying.accept(obj);
			return tmpres == SearchHitFilterDecision.ACCEPT? SearchHitFilterDecision.ACCEPT : SearchHitFilterDecision.REJECT; 
		}
		
		@Override
		public String toString() {
			return "StrictAccept[" + underlying + "]";
		}

	}
	
	// ------------------------------------------------------------------------
	
	public static class StrictRejectSearchHitFilter extends AbstractUnderlyingSearchHitFilter {

		public StrictRejectSearchHitFilter(@JsonProperty("underlying") SearchHitFilter underlying) {
			super(underlying);
		}
		
		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			SearchHitFilterDecision tmpres = underlying.accept(obj);
			return tmpres == SearchHitFilterDecision.REJECT? SearchHitFilterDecision.ACCEPT : SearchHitFilterDecision.REJECT; 
		}
		
		@Override
		public String toString() {
			return "StrictReject[" + underlying + "]";
		}
	}

	// ------------------------------------------------------------------------

	public static class AndSearchHitFilter extends SearchHitFilter {
		
		private SearchHitFilter[] elts;
		
		public AndSearchHitFilter(@JsonProperty("elts") SearchHitFilter[] elts) {
			this.elts = elts;
		}
		
		public AndSearchHitFilter(@JsonProperty("elts") List<SearchHitFilter> elts) {
			this.elts = elts.toArray(new SearchHitFilter[elts.size()]);
		}
		
		public SearchHitFilter[] getElts() {
			return elts;
		}

		public void setElts(SearchHitFilter[] elts) {
			this.elts = elts;
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			SearchHitFilterDecision res = SearchHitFilterDecision.ACCEPT;
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				SearchHitFilterDecision tmpres = elts[i].accept(obj);
				if (tmpres == SearchHitFilterDecision.REJECT) {
					return SearchHitFilterDecision.REJECT;
				}
				res = res.and(tmpres);
			}
			return res;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(elts);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AndSearchHitFilter other = (AndSearchHitFilter) obj;
			if (!Arrays.equals(elts, other.elts))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("AndFilter[\n");
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				sb.append(elts[i]);
				if (i + 1 < len) {
					sb.append("\n");
				}
			}
			sb.append("\n]");
			return sb.toString();
		}

	}
	

	// ------------------------------------------------------------------------

	public static class OrSearchHitFilter extends SearchHitFilter {
		
		private SearchHitFilter[] elts;
		
		public OrSearchHitFilter(@JsonProperty("elts") SearchHitFilter[] elts) {
			this.elts = elts;
		}
		
		public OrSearchHitFilter(@JsonProperty("elts") List<SearchHitFilter> elts) {
			this.elts = elts.toArray(new SearchHitFilter[elts.size()]);
		}
		
		public SearchHitFilter[] getElts() {
			return elts;
		}

		public void setElts(SearchHitFilter[] elts) {
			this.elts = elts;
		}

		@Override
		public SearchHitFilterDecision accept(SearchHit obj) {
			SearchHitFilterDecision res = SearchHitFilterDecision.REJECT;
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				SearchHitFilterDecision tmpres = elts[i].accept(obj);
				if (tmpres == SearchHitFilterDecision.ACCEPT) {
					return SearchHitFilterDecision.ACCEPT;
				}
				res = res.or(tmpres);
			}
			return res;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(elts);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OrSearchHitFilter other = (OrSearchHitFilter) obj;
			if (!Arrays.equals(elts, other.elts))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("OrFilter[\n");
			final int len = elts.length;
			for(int i = 0; i < len; i++) {
				sb.append(elts[i]);
				if (i + 1 < len) {
					sb.append("\n");
				}
			}
			sb.append("\n]");
			return sb.toString();
		}

	}
}
