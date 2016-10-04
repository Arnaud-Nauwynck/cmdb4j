package org.cmdb4j.core.ext.patterns.tokentemplate;

import java.io.Serializable;

public final class TemplatizedTokenKey implements Serializable, Comparable<TemplatizedTokenKey> {

	/** */
	private static final long serialVersionUID = 1L;
	
	private final int tokenType;
	private final Object templatizedValue;
	
	// ------------------------------------------------------------------------
	
	public TemplatizedTokenKey(int tokenType, Object value) {
		super();
		this.tokenType = tokenType;
		this.templatizedValue = value;
	}

	// ------------------------------------------------------------------------

	public int getTokenType() {
		return tokenType;
	}

	public Object getTemplatizedValue() {
		return templatizedValue;
	}

	// ------------------------------------------------------------------------
	
	public int compareTo(TemplatizedTokenKey other) {
		int res = 0;
		if (tokenType != other.tokenType) {
			res = (tokenType < other.tokenType)? -1 : +1;
		}
		if (res == 0 && templatizedValue instanceof Comparable) {
			res = ((Comparable) templatizedValue).compareTo(other);
		}
		
		return res;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = tokenType;
		result = prime * result + ((templatizedValue == null) ? 0 : templatizedValue.hashCode());
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
		TemplatizedTokenKey other = (TemplatizedTokenKey) obj;
		if (tokenType != other.tokenType)
			return false;
		if (templatizedValue == null) {
			if (other.templatizedValue != null)
				return false;
		} else if (!templatizedValue.equals(other.templatizedValue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[" + LogLexer.tokenNames[tokenType] + ", " + templatizedValue + "]";
	}
	
	protected String templatizedValueAsString() {
		return (templatizedVale != null)? templatizedValue.toString() : "";
	}
	
	public String dumpAsString() {
		switch(tokenType) {
		case LogLexer.IDENT: 
			return templatizedValueAsString();
		case LogLexer.TEXT: 
			return // "text:'" + 
					templatizedValueAsString();
			
		case LogLexer.NL:
			return "\n";
		case LogLexer.WS:
			return " ";
		case LogLexer.STRING_LITERAL1: case LogLexer.STRING_LITERAL2: 
			return "\"?\"";
		case LogLexer.STRING_LITERAL_LONG1: case LogLexer.STRING_LITERAL_LONG2:
			return "\"\"\"?\"\"\"";
			
		case LogLexer.PUNCTUATION:
			return templatizedValueAsString();
		case LogLexer.ASSIGN_OP: 
			return templatizedValueAsString();

		case LogLexer.INT: 
			return "?int";

		case LogLexer.DECIMAL: case LogLexer.DOUBLE: 
			return "?double";
		
		case LogLexer.DATE:
			return "?date";

		case LogLexer.PATH:
			return "?path";

		case LogLexer.ENG_DAY:
			return "?day";
		case LogLexer.MONTH:
			return "?month";
		case LogLexer.ENG_TZ:
			return "?tz";

		case LogLexer.OPEN_BRACE:
			return "(";
		case LogLexer.CLOSE_BRACE: 
			return ")";
		case LogLexer.OPEN_CURLY_BRACE: 
			return "{";
		case LogLexer.CLOSE_CURLY_BRACE: 
			return "}";
		case LogLexer.OPEN_SQUARE_BRACKET: 
			return "[";
		case LogLexer.CLOSE_SQUARE_BRACKET: 
			return "]";
		
		case LogLexer.OTHER:
			return (templatizedValue != null)? "?other" + templatizedValueAsString() : "?other";

		default:
			// Should not occur
			return "??unknown";
		}
				
	}
	
}

                        