package org.cmdb4j.core.ext.patterns.tokentemplate;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

public class TemplatizedTokenKeyFacade {

	public static TemplatizedTokenKeyPath templatizedTokenKeyPath(String text) {
		ANTLRInputStream lineStream = new ANTLRInputStream(text);
		LogLexer lexer = new LogLexer(lineStream);
		List<? extends Token> tokens = lexer.getAllTokens();
		List<TemplatizedTokenKey> tmp = new ArrayList<TemplatizedTokenKey>(tokens.size());
		for(Token token : tokens) {
			tmp.add(TemplatizedTokenKeyFacade.extractTokenTemplateAndValue(token, null));
		}
		lexer.reset();
		return new TemplatizedTokenKeyPath(tmp);
	}
	
	public static TemplatizedTokenKey extractTokenTemplateAndValue(Token token, Object[] valueHolder) {
		int tokenType = token.getType();
		String text = token.getText();
		Object value = text;
		Object templatizedValue = null;
		switch(tokenType) {
		case LogLexer.IDENT: 
			value = text;
			templatizedValue = text;
			break;
		case LogLexer.TEXT: 
			value = text; 
			templatizedValue = text;
			break;
		case LogLexer.NL:
			templatizedValue = null;
			break;
		case LogLexer.WS:
			templatizedValue = null;
			break;
		case LogLexer.STRING_LITERAL1: case LogLexer.STRING_LITERAL2: 
			templatizedValue = null;
			break;
		case LogLexer.STRING_LITERAL_LONG1: case LogLexer.STRING_LITERAL_LONG2:
			templatizedValue = null;
			break;
			
			
		case LogLexer.PUNCTUATION:
		case LogLexer.ASSIGN_OP: 
			templatizedValue = text;
			break;

		case LogLexer.INT: 
			value = Integer.parseInt(text);
			templatizedValue = null;
			break;
		case LogLexer.DECIMAL: case LogLexer.DOUBLE: 
			value = Double.parseDouble(text);
			templatizedValue = null;
			break;
		
		case LogLexer.DATE:
			value = null; // TODO...
			templatizedValue = null;
			break;

			// TODO
//		case LogLexer.ENG_DAY: // ( 'Mon' | 'Tue' | 'Wed' | 'Thu' | 'Fri' | 'Sat' | 'Sun' );
//			value = text;
//			break;
//		case LogLexer.MONTH:
//			value = text;
//			break;
//		case LogLexer.ENG_TZ:
//			value = text;
//			break;
			
		case LogLexer.OPEN_BRACE: 
		case LogLexer.CLOSE_BRACE: 
		case LogLexer.OPEN_CURLY_BRACE: 
		case LogLexer.CLOSE_CURLY_BRACE: 
		case LogLexer.OPEN_SQUARE_BRACKET: 
		case LogLexer.CLOSE_SQUARE_BRACKET: 
			value = text;
			templatizedValue = text;
			// TODO enter mode for matching close brace/bracket..
			break;
		
		case LogLexer.OTHER:
			value = null; // TODO...
			templatizedValue = null;
			break;

			// TODO
//		case LogLexer.PATH:
//			value = text;
//			templatizedValue = "ex-path";
//			break;
			
		default:
			// Should not occur
			value = null; // TODO...
			templatizedValue = null;
			break;
		}
		if (valueHolder != null) {
			valueHolder[0] = value;
		}
		
		TemplatizedTokenKey childKey = new TemplatizedTokenKey(tokenType, templatizedValue);
		return childKey;
	}

}
